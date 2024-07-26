package com.vigilonix.jaanch.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.config.AppConfig;
import com.vigilonix.jaanch.enums.Role;
import com.vigilonix.jaanch.enums.State;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.exception.ValidationRuntimeException;
import com.vigilonix.jaanch.model.OAuthToken;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.request.*;
import com.vigilonix.jaanch.transformer.UserResponseTransformer;
import com.vigilonix.jaanch.validator.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.TokenFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.http.HttpStatus;
import org.hibernate.exception.ConstraintViolationException;
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
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder;
    private final ValidationService<UserRequest> userRequestValidationService;
    private final SsoAuthValidator authValidator;
    private final UserMediaTransformer userMediaTransformer;
    private final AuthTokenTransformer authTokenTransformer;
    private final UserResponseTransformer userResponseTransformer;
    private final TokenFactory tokenFactory;
    private final ValidationService<AuthRequest> clientValidator;
    private final ObjectMapper objectMapper;
    private final AppConfig appConfig;

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

            user = userDao.findByUsername(authRequest.getUsername());
            if (user != null && !encoder.matches(authRequest.getPassword(), user.getSecret())) {
                user = null;
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

    private User signUp(UserRequest userRequest) {
        userRequestValidationService.validate(userRequest);

        User user = User.builder()

                .email(userRequest.getEmail())
                .secret(StringUtils.isNotEmpty(userRequest.getPassword()) ? encoder.encode(userRequest.getPassword()) : null)
                .username(userRequest.getUsername())
                .createdOn(System.currentTimeMillis())
                .modifiedOn(System.currentTimeMillis())
                .lastLive(System.currentTimeMillis())
                .state(State.ON_BOARDING)
                .uuid(UUID.randomUUID())
                .popularity(50)
                .address(userRequest.getAddress())
                .boostExpireTime(System.currentTimeMillis() - 10000)
                .lastLocationUpdateTimeInMillis(-1L)
                .role(Role.NORMAL)
//                .jobCompany(userRequest.getJobCompany())

                .build();

        userDao.save(user);

        return user;
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



    public UserResponse updateUser(User principal, UserRequest userRequest) {
        userRequestValidationService.validate(userRequest);
        principal = getEnriched(principal);
        boolean qcPending = false;

        if (StringUtils.isNotEmpty(userRequest.getName())
                && changeDetector.isChanged(principal.getName(), userRequest.getName())) {
            principal.setName(userRequest.getName());

        }
        if (userRequest.getQcName() != null && StringUtils.isNotEmpty(userRequest.getQcName().getName())
                && changeDetector.isChanged(principal.getQcName(), userRequest.getQcName())) {
            principal.setName(userRequest.getQcName().getName());

        }
        if (State.DISABLED.equals(principal.getState())) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.DISABLED_USER));
        }


        principal.setModifiedOn(System.currentTimeMillis());
        principal.setLastLive(System.currentTimeMillis());
        log.info("going to save user {} after put api", principal);
        userDao.save(principal);
        log.info("save successfully user {} after put api", principal);

        return userResponseTransformer.transform(principal);
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


    public OAuth2Response refreshToken(RefreshTokenRequest refreshTokenRequest) {
        OAuthToken authToken = tokenService.refreshStaleToken(refreshTokenRequest);
        return authTokenTransformer.transform(authToken);
    }

    public UserResponse getDetails(User principal) {
        principal = getEnriched(principal);
        return userResponseTransformer.transform(principal);
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

}
