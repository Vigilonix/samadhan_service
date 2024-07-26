package com.vigilonix.jaanch.service;

import com.dt.beyond.aop.Timed;
import com.dt.beyond.auth.SsoAuthValidator;
import com.dt.beyond.config.*;
import com.dt.beyond.dao.*;
import com.dt.beyond.enums.*;
import com.dt.beyond.exception.ValidationRuntimeException;
import com.dt.beyond.helper.*;
import com.dt.beyond.model.*;
import com.dt.beyond.pojo.*;
import com.dt.beyond.request.*;
import com.dt.beyond.transformer.*;
import com.dt.beyond.validator.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.vigilonix.jaanch.transformer.UserResponseTransformer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static com.dt.beyond.config.Constant.DEFAULT_STATE;
import static com.dt.beyond.config.Constant.SEPARATOR;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {

    public static final String DEVICE_TOKEN_UPDATE_REQUEST_BY_USER_WITH_TOKEN = "device token update request by user {} with token {}";
    public static final String BOT = "bot";
    public static final String PASSWORD = "password";
    public static final List<State> DEACTIVATED_STATES = Arrays.asList(State.DELETED, State.DEACTIVATE);
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final long QC_LOCK_BUFFER = 5 * 60 * 1000;
    private final UserDao userDao;
    private final UserDaoImpl userDaoImpl;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder;
    private final ValidationService<UserRequest> userRequestValidationService;
    private final ValidationService<UserMediaMapping> mediaMappingValidationService;
    private final SsoAuthValidator authValidator;
    private final UserMediaTransformer userMediaTransformer;
    private final AuthTokenTransformer authTokenTransformer;
    private final RequestMediaToUserMediaTransformer requestMediaToUserMediaTransformer;
    private final UserReferralDao userReferralDao;
    private final UserSubscriptionDao userSubscriptionDao;
    private final UserResponseTransformer userResponseTransformer;
    private final UserSocialMediaTransformer userSocialMediaTransformer;
    private final SocialMediaTokenService socialMediaTokenService;
    private final UserBoostBufferConfig userBoostBufferConfig;
    private final TokenFactory tokenFactory;
    private final CardDao cardDao;
    private final ValidationService<AuthRequest> clientValidator;
    private final TomTomGeoServiceHelper tomTomGeoServiceHelper;
    private final ObjectMapper objectMapper;
    private final SocialMediaConfig socialMediaConfig;
    private final AppConfig appConfig;
    private final ActionEventPropagator actionEventPropagator;
    private final MatchLimitationsConfig matchLimitationsConfig;
    private final DistanceCalculator distanceCalculator;
    private final SubscriptionService subscriptionService;
    private final SocialMediaImageMigrator socialMediaImageMigrator;
    private final ChangeDetector changeDetector;

    private static String toHexString(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String calculateHMAC(String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), HMAC_SHA256);
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(secretKeySpec);
        return toHexString(mac.doFinal(data.getBytes()));
    }

    public LoginResponse login(AuthRequest authRequest) {
        clientValidator.validate(authRequest);
        User user = null;
        int statusCode = HttpStatus.SC_OK;
        if (StringUtils.isNotEmpty(authRequest.getAuthToken())) {
            AuthSsoResponse authValidateResponse = authValidator
                    .validate(authRequest.getAuthToken(), authRequest.getSsoProvider());
            if (authRequest.getSsoProvider() != null && authValidateResponse.getUserId() != null) {
                user = userDao.findBySsoIdAndSsoProvider(authValidateResponse.getUserId(),
                        authRequest.getSsoProvider());
            }
            if (user == null && StringUtils.isNotEmpty(authValidateResponse.getEmail())) {
                user = userDao.findByEmail(authValidateResponse.getEmail());
            }
            if (user == null) {
                UserRequest userRequest = authValidator.getUserResponse(authRequest.getAuthToken(),
                        authRequest.getSsoProvider(), authValidateResponse);
                user = signUp(userRequest);

                try {
                    UserMediaMapping userMediaMapping = user.getMediaMappings().iterator().next();
                    String selfHostedMediaUri = socialMediaImageMigrator.getSelfHostedImage(userMediaMapping.getUri(), user);
                    userMediaMapping.setUri(selfHostedMediaUri);
                    user.setMediaMappings(Sets.newHashSet(userMediaMapping));
                    save(user);
                } catch (Exception e) {
                    log.error("failed to migrate user media but letting user sign in user {}", user, e);
                }

                statusCode = HttpStatus.SC_CREATED;
            }
        } else {
            user = userDao.findByUsername(authRequest.getUsername());
            if (user != null && !encoder.matches(authRequest.getPassword(), user.getSecret())) {
                user = null;
            }
        }
        if (user == null) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_GRANT));
        }
        if (DEACTIVATED_STATES.contains(user.getState())) {
            user.setState(State.QC_PENDING);
            userDao.save(user);
        }
        if (isCompleteDetailsMissingForMatching(user)) {
            statusCode = HttpStatus.SC_ACCEPTED;
        }
        OAuthToken oAuthToken = tokenService.save(authRequest, user);
        return new LoginResponse(authTokenTransformer.transform(oAuthToken), statusCode);
    }

    private boolean isCompleteDetailsMissingForMatching(User user) {
        log.debug("user for isCompletedTest {}", user);
        return StringUtils.isEmpty(user.getName()) ||
                user.getDobInMillis() == null ||
                StringUtils.isEmpty(user.getGender()) ||
                (StringUtils.isEmpty(user.getJobTitle()) && (user.getQcJobTitle() == null || StringUtils.isNotEmpty(user.getQcJobTitle().getJobTitle())))
                || CollectionUtils.isEmpty(user.getInterestedGenderMappings())
                || CollectionUtils.isEmpty(user.getMediaMappings());
    }

    private User signUp(UserRequest userRequest) {
        userRequestValidationService.validate(userRequest);
        Set<UserMediaMapping> userMediaMapping = userRequest.getMediaRequests().stream()
                .map(requestMediaToUserMediaTransformer::transform)
                .collect(Collectors.toSet());

        User user = User.builder()
                .name(StringUtils.isNotEmpty(userRequest.getName()) ? WordUtils.capitalizeFully(userRequest.getName()) : userRequest.getQcName().getName())
                .qcName(QcName.builder()
                        .name(WordUtils.capitalizeFully(StringUtils.isNotEmpty(userRequest.getName()) ? userRequest.getName() : userRequest.getQcName().getName()))
                        .qcState(QcState.builder()
                                .state(State.QC_PENDING)
                                .build())
                        .build())
                .email(userRequest.getEmail())
                .secret(StringUtils.isNotEmpty(userRequest.getPassword()) ? encoder.encode(userRequest.getPassword()) : null)
                .username(userRequest.getUsername())
                .country(userRequest.getCountry())
                .dobInMillis(userRequest.getDob())
                .qcDobInMillis(userRequest.getQcDobInMillis() == null || userRequest.getQcDobInMillis().getDobInMillis()==null
                        ? null : QcDobInMillis.builder()
                        .dobInMillis(userRequest.getQcDobInMillis().getDobInMillis())
                        .qcState(QcState.builder()
                                .state(State.QC_PENDING)
                                .build())
                        .build())
//                .latitude(userRequest.getLatitude())
//                .longitude(userRequest.getLongitude())
                .qcDescription(userRequest.getQcDescription() == null || StringUtils.isEmpty(userRequest.getQcDescription().getDescription())
                        ? null : QcDescription.builder()
                        .description(userRequest.getQcDescription().getDescription())
                        .qcState(QcState.builder()
                                .state(State.QC_PENDING)
                                .build())
                        .build())
                .description(userRequest.getDescription())
                .qcDescription(QcDescription.builder()
                        .description(userRequest.getDescription())
                        .build())
                .education(userRequest.getEducation())
                .qcEducation(userRequest.getQcEducation() == null || StringUtils.isEmpty(userRequest.getQcEducation().getEducation()) ? null : QcEducation.builder()
                        .education(userRequest.getQcEducation().getEducation())
                        .qcState(QcState.builder()
                                .state(State.QC_PENDING)
                                .build())
                        .build())
                .gender(userRequest.getGender())
                .qcGender(userRequest.getQcGender() == null || StringUtils.isEmpty(userRequest.getQcGender().getGender())
                        ? null : QcGender.builder()
                                    .gender(userRequest.getGender())
                                    .qcState(QcState.builder()
                                        .state(State.QC_PENDING)
                                        .build())
                                    .build()
                )
                .createdOn(System.currentTimeMillis())
                .modifiedOn(System.currentTimeMillis())
                .lastLive(System.currentTimeMillis())
                .ssoId(userRequest.getSsoId())
                .ssoProvider(userRequest.getSsoProvider())
                .state(State.ON_BOARDING)
                .uuid(UUID.randomUUID())
                .popularity(50)
                .address(userRequest.getAddress())
                .boostExpireTime(System.currentTimeMillis() - 10000)
                .lastLocationUpdateTimeInMillis(-1L)
                .role(Role.NORMAL)
//                .jobCompany(userRequest.getJobCompany())
                .qcJobTitle(userRequest.getQcJobTitle() == null || StringUtils.isEmpty(userRequest.getQcJobTitle().getJobTitle()) ? null : QcJobTitle.builder()
                        .jobTitle(userRequest.getQcJobTitle().getJobTitle())
                        .qcState(QcState.builder()
                                .state(State.QC_PENDING)
                                .build())
                        .build())
                .jobTitle(userRequest.getJobTitle())
                .messageLikeNotification(true)
                .messageNotification(true)
                .barrierCrossedNotification(true)
                .newMatchNotification(true)
                .invisible(false)
                .doNotDisturb(false)
                .minInterestedAge(18L * 365 * 24 * 60 * 60 * 1000)
                .maxInterestedAge(200L * 365 * 24 * 60 * 60 * 1000)
                .locationRangeInMeters(500000)
                .hobbyMappings(new HashSet<>())
                .foodMappings(new HashSet<>())
                .interestedGenderMappings(new HashSet<>())
                .barrierMappings(new HashSet<>())
                .musicMappings(new HashSet<>())
                .movieMappings(new HashSet<>())
                .readMappings(new HashSet<>())
                .mediaMappings(userMediaMapping)
                .socialMediaMappings(new HashSet<>())
                .build();
        user.setReferralCode(generateReferralCode(userRequest.getQcName()));
        userDao.save(user);
        userSubscriptionDao.save(UserSubscriptionMapping.builder()
                .createdOn(System.currentTimeMillis())
                .modifiedOn(System.currentTimeMillis())
                .user(user)
                .expireTime(-1L)
                .membership(Membership.DEFAULT)
                .build());
        if (StringUtils.isNotEmpty(userRequest.getReferralCode())) {
            handleRefererBy(userRequest.getReferralCode(), user);
        }
        return user;
    }

    private void handleRefererBy(String referralCode, User user) {
        if (StringUtils.isNotEmpty(referralCode)) {
            long time = System.currentTimeMillis();
            User referrerUser = userDao.findByReferralCode(referralCode);
            if (referrerUser != null) {
                UserReferralMapping userReferralMapping = userReferralDao.findByUser(user);
                if (userReferralMapping == null && !referrerUser.getId().equals(user.getId())) {
                    userReferralDao.save(UserReferralMapping.builder()
                            .referrer(referrerUser)
                            .user(user)
                            .timestamp(time)
                            .build());
                    subscriptionService.awardSubscription(user, referrerUser, time);
                    return;
                }
                throw new ValidationRuntimeException(Collections.singletonList(
                        ValidationErrorEnum.REFERRAL_MAPPING_ALREADY_EXIST));
            }
        }
        throw new ValidationRuntimeException(Collections.singletonList(
                ValidationErrorEnum.INVALID_REFERRER_TOKEN));
    }

    private String generateReferralCode(QcName qcName) {
        if (qcName == null || StringUtils.isEmpty(qcName.getName())) {
            return RandomStringUtils.randomAlphanumeric(6);
        }
        String name = qcName.getName();
        return StringUtils.deleteWhitespace((name.length() >= 4 ? name.substring(0, 4) : name) + RandomStringUtils.randomAlphanumeric(6));
    }

    @Timed
    public void updateLocation(LocationRequest locationRequest, User principal) {
        log.info("update location user {} request {}", principal.getId(), locationRequest);
        handleLocationUpdateForMatch(locationRequest, principal);
        Long lastUpdateTime = principal.getLastLocationUpdateTimeInMillis();
        principal.setLastLive(System.currentTimeMillis());
        principal.setLatitude(locationRequest.getLatitude());
        principal.setLongitude(locationRequest.getLongitude());
        if (locationRequest.getLocationTimeInMillis() == null) {
            principal.setLastLocationUpdateTimeInMillis(System.currentTimeMillis());
        } else {
            principal.setLastLocationUpdateTimeInMillis(locationRequest.getLocationTimeInMillis());
        }
        if (principal.getLastLocationUpdateTimeInMillis() > lastUpdateTime) {
            log.info("will update location user {} request {}", principal.getId(), locationRequest);
            cardDao.updateLocation(locationRequest, principal);

            userDao.save(principal);

        }
    }

    private void handleLocationUpdateForMatch(LocationRequest locationRequest, User principal) {
        int changeInDistance = distanceCalculator.getDistanceInMetres(principal.getLatitude(), principal.getLongitude(), locationRequest.getLatitude(), locationRequest.getLongitude());
        if (changeInDistance > matchLimitationsConfig.getMinimaLocationRequiredToTriggerComputation()) {
            try {
                actionEventPropagator.handleProfileUpdate(principal, WorkerPayloadType.LOCATION);
            } catch (RuntimeException e) {
                log.error("failed to insert location watch request {} for user {} ", locationRequest, principal.getId(), e);
            }
        }
    }

    public UserResponse updateUser(User principal, UserRequest userRequest) {
        userRequestValidationService.validate(userRequest);
        principal = getEnriched(principal);
        boolean qcPending = false;

        if (StringUtils.isNotEmpty(userRequest.getName())
                && changeDetector.isChanged(principal.getName(), userRequest.getName())) {
            principal.setName(userRequest.getName());
            principal.setQcName(QcName.builder()
                    .name(userRequest.getName())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (userRequest.getQcName() != null && StringUtils.isNotEmpty(userRequest.getQcName().getName())
                && changeDetector.isChanged(principal.getQcName(), userRequest.getQcName())) {
            principal.setName(userRequest.getQcName().getName());
            principal.setQcName(QcName.builder()
                    .name(userRequest.getQcName().getName())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (State.DISABLED.equals(principal.getState())) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.DISABLED_USER));
        }
        if (userRequest.getCountry() != null) {
            principal.setCountry(userRequest.getCountry());
        }
        if (Objects.nonNull(userRequest.getDob())
                && changeDetector.isChanged(principal.getDobInMillis(), userRequest.getDob())) {
            principal.setDobInMillis(userRequest.getDob());
            principal.setQcDobInMillis(QcDobInMillis.builder()
                    .dobInMillis(userRequest.getDob())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (userRequest.getQcDobInMillis() != null && Objects.nonNull(userRequest.getQcDobInMillis().getDobInMillis())
                && changeDetector.isChanged(principal.getQcDobInMillis(), userRequest.getQcDobInMillis())) {
            principal.setDobInMillis(userRequest.getQcDobInMillis().getDobInMillis());
            principal.setQcDobInMillis(QcDobInMillis.builder()
                    .dobInMillis(userRequest.getQcDobInMillis().getDobInMillis())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
//        if (userRequest.getLatitude() != null) {
//            principal.setLatitude(userRequest.getLatitude());
//        }
//        if (userRequest.getLongitude() != null) {
//            principal.setLongitude(userRequest.getLongitude());
//        }
        if (Objects.nonNull(userRequest.getDescription())
                && changeDetector.isChanged(principal.getDescription(), userRequest.getDescription())) {
            principal.setDescription(userRequest.getDescription());
            principal.setQcDescription(QcDescription.builder()
                    .description(userRequest.getDescription())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (userRequest.getQcDescription() != null && Objects.nonNull(userRequest.getQcDescription().getDescription())
                && changeDetector.isChanged(principal.getQcDescription(), userRequest.getQcDescription())) {
            principal.setDescription(userRequest.getQcDescription().getDescription());
            principal.setQcDescription(QcDescription.builder()
                    .description(userRequest.getQcDescription().getDescription())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (Objects.nonNull(userRequest.getEducation())
                && changeDetector.isChanged(principal.getEducation(), userRequest.getEducation())) {
            principal.setEducation(userRequest.getEducation());
            principal.setQcEducation(QcEducation.builder()
                    .education(userRequest.getEducation())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (userRequest.getQcEducation() != null && Objects.nonNull(userRequest.getQcEducation().getEducation())
                && changeDetector.isChanged(principal.getQcEducation(), userRequest.getQcEducation())) {
            principal.setEducation(userRequest.getQcEducation().getEducation());
            principal.setQcEducation(QcEducation.builder()
                    .education(userRequest.getQcEducation().getEducation())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (Objects.nonNull(userRequest.getJobCompany())
                && changeDetector.isChanged(principal.getJobCompany(), userRequest.getJobCompany())) {
            principal.setJobCompany(userRequest.getJobCompany());
            principal.setQcJobCompany(QcJobCompany.builder()
                    .jobCompany(userRequest.getJobCompany())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (userRequest.getQcJobCompany() != null && Objects.nonNull(userRequest.getQcJobCompany().getJobCompany())
                && changeDetector.isChanged(principal.getQcJobCompany(), userRequest.getQcJobCompany())) {
            principal.setJobCompany(userRequest.getQcJobCompany().getJobCompany());
            principal.setQcJobCompany(QcJobCompany.builder()
                    .jobCompany(userRequest.getQcJobCompany().getJobCompany())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
//        if (userRequest.getState() != null) {
//            principal.setState(userRequest.getState());
//        }
        if (userRequest.getGender() != null) {
            principal.setGender(userRequest.getGender());
        }
        if (StringUtils.isNotEmpty(userRequest.getReferralCode())) {
            handleRefererBy(userRequest.getReferralCode(), principal);
        }
        if (userRequest.getLocationRangeInMeters() != null) {
            principal.setLocationRangeInMeters(userRequest.getLocationRangeInMeters());
        }
        if (StringUtils.isNotEmpty(userRequest.getAddress())) {
            principal.setAddress(userRequest.getAddress());
        }
        if (StringUtils.isNotEmpty(userRequest.getJobTitle())
                && changeDetector.isChanged(principal.getJobTitle(), userRequest.getJobTitle())) {
            principal.setJobTitle(userRequest.getJobTitle());
            principal.setQcJobTitle(QcJobTitle.builder()
                    .jobTitle(userRequest.getJobTitle())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
        if (userRequest.getQcJobTitle() != null && StringUtils.isNotEmpty(userRequest.getQcJobTitle().getJobTitle())
                && changeDetector.isChanged(principal.getQcJobTitle(), userRequest.getQcJobTitle())) {
            principal.setJobTitle(userRequest.getQcJobTitle().getJobTitle());
            principal.setQcJobTitle(QcJobTitle.builder()
                    .jobTitle(userRequest.getQcJobTitle().getJobTitle())
                    .qcState(QcState.builder()
                            .state(State.QC_PENDING)
                            .build())
                    .build());
            qcPending = true;
        }
//        if (StringUtils.isNotEmpty(userRequest.getJobCompany())) {
//            principal.setJobCompany(userRequest.getJobCompany());
//        }
        if (userRequest.getMinInterestedAge() != null) {
            principal.setMinInterestedAge(userRequest.getMinInterestedAge());
        }
        if (userRequest.getMaxInterestedAge() != null) {
            principal.setMaxInterestedAge(userRequest.getMaxInterestedAge());
        }
        if (userRequest.getNewMatchNotification() != null) {
            principal.setNewMatchNotification(userRequest.getNewMatchNotification());
        }
        if (userRequest.getMessageNotification() != null) {
            principal.setMessageNotification(userRequest.getMessageNotification());
        }
        if (userRequest.getMessageLikeNotification() != null) {
            principal.setMessageLikeNotification(userRequest.getMessageLikeNotification());
        }
        if (userRequest.getBarrierCrossedNotification() != null) {
            principal.setBarrierCrossedNotification(userRequest.getBarrierCrossedNotification());
        }
        if (userRequest.getDoNotDisturb() != null) {
            principal.setDoNotDisturb(userRequest.getDoNotDisturb());
        }
        if (userRequest.getInvisible() != null) {
            principal.setInvisible(userRequest.getInvisible());
        }
        qcPending = handleUserMappings(principal, userRequest) || qcPending;
        principal.setModifiedOn(System.currentTimeMillis());
        principal.setLastLive(System.currentTimeMillis());
        enrichState(principal, qcPending);
        log.info("going to save user {} after put api", principal);
        userDao.save(principal);
        log.info("save successfully user {} after put api", principal);
        try {
            if (State.ACTIVE.equals(principal.getState())) {
                actionEventPropagator.handleProfileUpdate(principal, WorkerPayloadType.PROFILE_UPDATE);
            } else if (Arrays.asList(State.QC_PENDING, State.ON_BOARDING).contains(principal.getState())) {
                actionEventPropagator.handleProfileUpdate(principal, WorkerPayloadType.PROFILE_UPDATE_QC_PENDING);
            }
            if (!State.ACTIVE.equals(principal.getState())) {
                actionEventPropagator.propagateUserDisablement(principal.getId());
            }
        } catch (Exception e) {
            log.error("failed to push event for profile update {}", userRequest, e);
        }
        return userResponseTransformer.transform(principal);
    }

    private void enrichState(User principal, boolean qcPending) {
        if (!isCompleteDetailsMissingForMatching(principal) && (qcPending || State.ON_BOARDING.equals(principal.getState()))) {
            principal.setState(State.QC_PENDING);
        }
    }

    private boolean handleUserMappings(User principal, UserRequest userRequest) {
        boolean qcPending = false;
        if (userRequest.getReads() != null) {
            Set<UserReadMapping> userReadMappings = userRequest.getReads().stream()
                    .map(uReads -> UserReadMapping.builder()
                            .title(uReads.getTitle())
                            .authorTitles(CollectionUtils.isNotEmpty(uReads.getAuthorTitle()) ? String.join(SEPARATOR, uReads.getAuthorTitle()) : null)
                            .isbn10(uReads.getIsbn10())
                            .isbn13(uReads.getIsbn13())
                            .posterUri(uReads.getPosterUri())
                            .uuid(UUID.randomUUID())
                            .build()).collect(Collectors.toSet());
            principal.setReadMappings(userReadMappings);
        }
        if (userRequest.getFoods() != null) {
            Set<UserFoodMapping> userFoodMappings = userRequest.getFoods().stream()
                    .map(uf -> UserFoodMapping.builder()
                            .uuid(UUID.randomUUID())
                            .recipeName(uf.getRecipeName())
                            .build()).collect(Collectors.toSet());
            if(changeDetector.isChangeCollection(principal.getFoodMappings(), userFoodMappings)) {
                principal.setFoodMappings(userFoodMappings);
                qcPending = true;
            }
        }
        if (userRequest.getMovieResponseRequests() != null) {
            Set<UserMovieMapping> userMovieMapping = userRequest.getMovieResponseRequests().stream()
                    .map(mr -> UserMovieMapping.builder()
                            .uuid(UUID.randomUUID())
                            .movieId(mr.getMovieId())
                            .posterUri(mr.getPosterUri())
                            .title(mr.getTitle())
                            .adult(mr.isAdult())
                            .genreIds(mr.getGenreIds().stream().map(Object::toString)
                                    .collect(Collectors.joining(SEPARATOR)))
                            .genreName(StringUtils.join(mr.getGenreNames(), SEPARATOR))
                            .build()).collect(Collectors.toSet());
            principal.setMovieMappings(userMovieMapping);
        }
        if (userRequest.getHobby() != null) {
            principal.setHobbyMappings(userRequest.getHobby().stream()
                    .map(h -> UserHobbyMapping.builder()
                            .uuid(UUID.randomUUID())
                            .hobby(h)
                            .build())
                    .collect(Collectors.toSet()));
        }
        if (CollectionUtils.isNotEmpty(userRequest.getInterestedGenders())) {
            Set<UserInterestedGenderMapping> mappedGender = userRequest.getInterestedGenders().stream()
                    .map(g -> UserInterestedGenderMapping.builder()
                            .uuid(UUID.randomUUID())
                            .gender(g)
                            .build())
                    .collect(Collectors.toSet());
            log.info("mapping request genders {} to gender pojo {} for user {}", userRequest.getInterestedGenders(), mappedGender, principal.getId());
            principal.setInterestedGenderMappings(mappedGender);
        }
        return qcPending;
    }

    public void logout(User principal) {
        tokenService.deleteByUserId(principal);
    }

    //TOOD: set up crond job to clear old deleted accounts
    public FbGdprResponse deleteAccount(User principal) {
        principal.setState(State.DELETED);
        principal.setStateChangedOn(System.currentTimeMillis());
        userDao.save(principal);
        return FbGdprResponse.builder()
                .url(String.format(appConfig.getAppDbDeleteGdprDeleteUri(), principal.getUuid()))
                .confirmationCode(principal.getUuid().toString())
                .build();
    }

    //TODO: update user state as deleting last media made him onboarding
    public List<UserMediaResponse> addMedia(List<MediaRequest> mediaRequests, User principal) {
        log.info("media add request {} for user {}", mediaRequests, principal.getId());
        if (CollectionUtils.isNotEmpty(principal.getMediaMappings()) && principal.getMediaMappings().size() >= 9) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.MEDIA_MORE_THAN_ALLOWED));
        }
        Map<Integer, UserMediaMapping> existingPositionMap = principal.getMediaMappings().stream()
                .collect(Collectors.toMap(UserMediaMapping::getPosition, m -> m));
        List<UserMediaMapping> userMediaMappings = mediaRequests.stream()
                .map(requestMediaToUserMediaTransformer::transform)
                .collect(Collectors.toList());
        userMediaMappings.forEach(mediaMappingValidationService::validate);
        userMediaMappings.forEach(umm -> existingPositionMap.put(umm.getPosition(), umm));
        principal.setMediaMappings(new HashSet<>(existingPositionMap.values()));
        enrichState(principal, true);
        userDao.save(principal);
        return userMediaMappings.stream()
                .map(userMediaTransformer::transform)
                .collect(Collectors.toList());
    }

    public List<UserMediaResponse> updateMedia(List<MediaRequest> mediaRequests, User principal) {
        log.info("put request by princiapl {} is {}", principal.getId(), mediaRequests);
        Map<UUID, MediaRequest> userMediaRequestMap = mediaRequests.stream()
                .filter(m -> StringUtils.isNotEmpty(m.getUuid()))
                .collect(Collectors.toMap(mr -> UUID.fromString(mr.getUuid()), mr -> mr));
        boolean qcRequired = false;
        if (userMediaRequestMap.size() != mediaRequests.size()) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_UUID));
        }
        if (CollectionUtils.isEmpty(principal.getMediaMappings())) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_REQUEST));
        }
        for (UserMediaMapping userMediaMapping : principal.getMediaMappings()) {
            MediaRequest mediaRequest = userMediaRequestMap.get(userMediaMapping.getUuid());
            if (mediaRequest != null) {
                if (StringUtils.isNotEmpty(mediaRequest.getUri()) && !userMediaMapping.getUri().equals(mediaRequest.getUri())) {
                    userMediaMapping.setUri(mediaRequest.getUri());
                    userMediaMapping.setQcState(DEFAULT_STATE);
                    userMediaMapping.setMediaType(mediaRequest.getMediaType());
                    qcRequired = true;
                }
                if (mediaRequest.getPosition() != null) {
                    userMediaMapping.setPosition(mediaRequest.getPosition());
                }
            }
        }

        principal.getMediaMappings().forEach(mediaMappingValidationService::validate);
        enrichState(principal, qcRequired);
        log.info("sort media mapping {}", principal);
        userDao.save(principal);
        return principal.getMediaMappings().stream()
                .map(userMediaTransformer::transform)
                .collect(Collectors.toList());
    }

    public void dropMedia(String uuid, User principal) {
        if (CollectionUtils.isNotEmpty(principal.getMediaMappings())) {
            UserMediaMapping deleteCandidate = null;
            for (UserMediaMapping userMediaMapping : principal.getMediaMappings()) {
                if (UUID.fromString(uuid).equals(userMediaMapping.getUuid())) {
                    deleteCandidate = userMediaMapping;
                }
            }
            if (deleteCandidate != null) {
                principal.getMediaMappings().remove(deleteCandidate);
                if (!State.ON_BOARDING.equals(principal.getState()) && CollectionUtils.isEmpty(principal.getMediaMappings())) {
                    throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.MEDIA_POSITION_NULL));
                }
                userDao.save(principal);
            }
        }
    }

    public OAuth2Response refreshToken(RefreshTokenRequest refreshTokenRequest) {
        OAuthToken authToken = tokenService.refreshStaleToken(refreshTokenRequest);
        return authTokenTransformer.transform(authToken);
    }

    public UserResponse getDetails(User principal) {
        principal = getEnriched(principal);
        return userResponseTransformer.transform(principal);
    }

    public SocialMediaResponse getSocialMediaLongLivedToken(SocialMediaLongLivedTokenRequest request, User principal) {
        log.info("got request to fetch long lived token request {}", request);
        SocialMediaLongLivedTokenResponse longLivedResponse = socialMediaTokenService
                .getLongLivedToken(request);
        UserSocialMediaMapping socialMediaMapping = null;
        for (UserSocialMediaMapping userSocialMediaMapping : principal.getSocialMediaMappings()) {
            if (request.getPlatform().equals(userSocialMediaMapping.getSocialMediaPlatform())) {
                socialMediaMapping = userSocialMediaMapping;
            }
        }
        if (socialMediaMapping == null) {
            socialMediaMapping = UserSocialMediaMapping.builder()
                    .socialMediaPlatform(request.getPlatform())
                    .createdOn(System.currentTimeMillis())
                    .shortLivedToken(request.getAuthToken())
                    .build();
            principal.getSocialMediaMappings().add(socialMediaMapping);
        }
        socialMediaMapping.setExpiredOn(System.currentTimeMillis() + longLivedResponse.getExpiresIn() * 1000L);
        socialMediaMapping.setLongLivedToken(longLivedResponse.getAccessToken());
        socialMediaMapping.setRefreshToken(longLivedResponse.getRefreshToken());
        socialMediaMapping.setModifiedOn(System.currentTimeMillis());
        userDao.save(principal);
        return userSocialMediaTransformer.transform(socialMediaMapping);
    }

    public List<User> getAllExpiringSocialMediaToken(long scanTimeRange) {
        return userDao.findAllByExpiredSocialMediaOnLessThanEqual(scanTimeRange);
    }

    public void refreshSocialMediaToken(User user, UserSocialMediaMapping socialMediaMapping) {
        user.getSocialMediaMappings().remove(socialMediaMapping);
        SocialMediaLongLivedTokenResponse longLivedResponse = socialMediaTokenService
                .refreshToken(socialMediaMapping);
        socialMediaMapping.setLongLivedToken(longLivedResponse.getAccessToken());
        socialMediaMapping.setModifiedOn(System.currentTimeMillis());
        socialMediaMapping.setRefreshToken(longLivedResponse.getRefreshToken());
        socialMediaMapping.setExpiredOn(System.currentTimeMillis() + longLivedResponse.getExpiresIn() * 1000L);
        user.getSocialMediaMappings().add(socialMediaMapping);
        userDao.save(user);
    }

    public User getEnriched(User user) {
        return user;
    }

    public List<User> eagerFetch(List<User> passiveUsers) {
        List<Long> ids = passiveUsers.stream().map(User::getId).collect(Collectors.toList());
        return getEnrichedUsers(ids);
    }

    public User getUserByUuid(UUID userUuid) {
        return userDao.findByUuid(userUuid);
    }

    public List<UserBarrierMapping> getBarrier(User principal) {
        if (CollectionUtils.isNotEmpty(principal.getBarrierMappings())) {
            return new ArrayList<>(principal.getBarrierMappings());
        }
        return Collections.emptyList();
    }

    public void boost(User principal) {
        if (principal.getBoostExpireTime() > System.currentTimeMillis()) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.BOOST_RENEW_BUFFER_EXPIRED));
        }
        if (principal.getBoostExpireTime() + userBoostBufferConfig.getRenewPeriodInMillis() <
                System.currentTimeMillis()) {
            principal.setBoostExpireTime(System.currentTimeMillis() + userBoostBufferConfig.getBufferInMillis());
        } else {
            CustomValidationError customValidationError = CustomValidationError.builder()
                    .code(ValidationErrorEnum.BOOST_RENEW_BUFFER_EXPIRED.getCode())
                    .messageFormat(ValidationErrorEnum.BOOST_RENEW_BUFFER_EXPIRED.getMessageFormat())
                    .attributes(Collections.singletonList(Long.valueOf(principal.getBoostExpireTime()
                            + userBoostBufferConfig.getRenewPeriodInMillis()).toString()))
                    .build();
            throw new ValidationRuntimeException(Collections.singletonList(customValidationError));
        }
    }

    public AllReferralResponse getAllReferredUsers(User principal) {
        List<UserReferralMapping> referralMappings = userReferralDao.findAllByReferrer(principal);
        List<ReferralUserResponse> referredUsers = referralMappings.stream().map(urm -> ReferralUserResponse.builder()
                .epochInMillis(urm.getTimestamp())
                .name(urm.getUser().getName())
                .build())
                .collect(Collectors.toList());
        return new AllReferralResponse(referredUsers.size(), referredUsers);
    }

    public User getUserById(Long userId) {
        Optional<User> optionalUser = userDao.findById(userId);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_ID));
    }

    @Timed
    public List<User> getEnrichedUsers(List<Long> userIds) {
        if (CollectionUtils.isNotEmpty(userIds)) {
            return userDao.findByIdIn(userIds);
        }
        return Collections.emptyList();

    }

    public void activateAccount(User principal) {
        if (!State.DISABLED.equals(principal.getState())) {
            principal.setState(State.ACTIVE);
            principal.setStateChangedOn(System.currentTimeMillis());
            userDao.save(principal);
        } else {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.CAN_NOT_ACTIVATE_DISABLED_ACCOUNT));
        }
    }

    public OAuth2Response signUpViaUserPass(UserRequest userRequest, User principal) {
        if (Role.ADMIN.equals(principal.getRole())) {
            if (StringUtils.isEmpty(userRequest.getPassword())) {
                throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.EMPTY_PASSWORD));
            }
            if (StringUtils.isEmpty(userRequest.getUsername())) {
                throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.EMPTY_USERNAME));
            }
            User user = signUp(userRequest);
            user.setRole(Role.BOT);
            try {
                userDao.save(user);
            } catch (ConstraintViolationException e) {
                throw new ValidationRuntimeException(e, Collections.singletonList(ValidationErrorEnum.USER_ALREADY_EXISTS));
            }
            AuthRequest authRequest = AuthRequest.builder()
                    .clientId(BOT)
                    .clientSecret(BOT)
                    .grantType(PASSWORD)
                    .scope(PASSWORD)
                    .build();
            OAuthToken oAuthToken = tokenService.save(authRequest, user);
            return authTokenTransformer.transform(oAuthToken);
        }
        throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.UNAUTHORIZED_REQUEST));
    }

    public void updatePopularity(User user, int popularity) {
        userDao.updatePopularity(user.getId(), popularity);
    }

    public SocialMediaResponse getFirebaseToken(User principal) {
        return SocialMediaResponse.builder()
                .authToken(tokenFactory.getAuthToken(principal.getUuid().toString()))
                .socialMediaPlatform(SocialMediaPlatform.FIREBASE)
                .build();
    }

    public void updateDeviceToken(DeviceTokenRequest deviceTokenRequest, User principal) {
        log.info(DEVICE_TOKEN_UPDATE_REQUEST_BY_USER_WITH_TOKEN, principal.getId(), deviceTokenRequest.getDeviceToken());
        if (StringUtils.isNotEmpty(deviceTokenRequest.getDeviceToken())) {
            principal.setDeviceToken(deviceTokenRequest.getDeviceToken());
            userDao.save(principal);
        }
    }

    public GeoLocationResponse getGeoLocation(User principal) {
        TomTomReverseGeoCodeResponse response = tomTomGeoServiceHelper.getAddress(principal.getLatitude(), principal.getLongitude());
        if (CollectionUtils.isNotEmpty(response.getAddresses())) {
            return GeoLocationResponse.builder()
                    .country(response.getAddresses().get(0).getAddress().getCountry())
                    .secondarySubdivision(response.getAddresses().get(0).getAddress().getCountrySecondarySubdivision())
                    .subdivision(response.getAddresses().get(0).getAddress().getCountrySubdivision())
                    .build();
        }
        return GeoLocationResponse.builder().build();

    }

    public User getUserBySsoId(String ssoId, SsoProvider ssoProvider) {
        return userDao.findBySsoIdAndSsoProvider(ssoId, ssoProvider);
    }

    public FbGdprResponse instaCallbackHandler(String signedRequest, InstaCallbackAction action) {
        log.info("got insta callback request {} for action {}", signedRequest, action);
        String[] spiltRequest = signedRequest.split("\\.");
        if (spiltRequest.length > 1) {
            String signature = Arrays.toString(Base64.getDecoder().decode(spiltRequest[0]));
            String payload = Arrays.toString(Base64.getDecoder().decode(spiltRequest[1]));
            try {
                FbSignedRequest accountDeleteRequest = objectMapper.readValue(payload, FbSignedRequest.class);
                if (signature.equals(calculateHMAC(spiltRequest[1], socialMediaConfig.getFbAppSecret()))) {
                    User principal = getUserBySsoId(accountDeleteRequest.getUserId(), SsoProvider.FACEBOOK);
                    if (InstaCallbackAction.DELETE.equals(action)) {
                        return deleteAccount(principal);
                    } else if (InstaCallbackAction.DEAUTHORIZE.equals(action)) {
                        tokenService.deleteByUserId(principal);
                    }
                    throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_REQUEST));
                }
            } catch (Exception e) {
                log.error("failed to verify gdpr delete request {}", signedRequest, e);
            }
        } else {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_REQUEST));
        }
        throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.UNAUTHORIZED_REQUEST));
    }

    public String getDeleteVerifyResponse(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            User principal = getUserByUuid(uuid);
            if (principal != null && State.DELETED.equals(principal.getState())) {
                return "<html>This user has been deleted</html>";
            }
            return "<html>Deletion is in progress </html>";
        } catch (Exception e) {
            log.error("invalid delete verification request {}", id, e);
        }
        throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_ID));
    }

    public void updateLivePresence(UUID userUuid, FirebasePresenceStatusMessage status) {
        User user = getUserByUuid(userUuid);
        if (user != null && (user.getLastLive() == null || user.getLastLive() < status.getTime())) {
            user.setLastLive(status.getTime());
            user.setLive(status.getPresence());
            userDao.save(user);
        }
    }

    public List<Long> findAllActiveUsers() {
        return userDao.getAllUserIdsByState(State.ACTIVE);
    }

    public void updateMatchScore(long matchScore, User user) {
        user.setScore(matchScore);
        userDao.save(user);
    }

    public void save(User principal) {
        principal.setModifiedOn(System.currentTimeMillis());
        userDao.save(principal);
    }

    public void saveAndChangeState(User principal) {
        enrichState(principal, true);
        principal.setModifiedOn(System.currentTimeMillis());
        userDao.save(principal);
    }

    public FbGdprResponse changeState(UserStateChaneRequest userStateChaneRequest, User principal) {
        if (!DEACTIVATED_STATES.contains(userStateChaneRequest.getState())) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_REQUEST));
        }
        principal.setState((userStateChaneRequest.getState()));
        principal.setStateChangedOn(System.currentTimeMillis());
        userDao.save(principal);
        if (State.DELETED.equals(userStateChaneRequest.getState())) {
            return FbGdprResponse.builder()
                    .url(String.format(appConfig.getAppDbDeleteGdprDeleteUri(), principal.getUuid()))
                    .confirmationCode(principal.getUuid().toString())
                    .build();
        }
        return FbGdprResponse.builder().build();
    }

    public void qcLockMark(List<Long> userIds) {
    }

    public List<User> getQcPendingUser(int limit, int offset) {
        return userDaoImpl.getQcPendingUsers(limit, offset, System.currentTimeMillis() + QC_LOCK_BUFFER);
    }
}
