package com.vigilonix.samadhan.service;


import com.vigilonix.samadhan.aop.LogPayload;
import com.vigilonix.samadhan.aop.Timed;
import com.vigilonix.samadhan.enums.Role;
import com.vigilonix.samadhan.enums.State;
import com.vigilonix.samadhan.enums.ValidationErrorEnum;
import com.vigilonix.samadhan.exception.ValidationRuntimeException;
import com.vigilonix.samadhan.helper.ChangeDetector;
import com.vigilonix.samadhan.model.OAuthToken;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.GeoHierarchyNode;
import com.vigilonix.samadhan.repository.UserRepository;
import com.vigilonix.samadhan.repository.UserRepositoryCustom;
import com.vigilonix.samadhan.request.*;
import com.vigilonix.samadhan.transformer.AuthTokenTransformer;
import com.vigilonix.samadhan.transformer.SearchUserResponseTransformer;
import com.vigilonix.samadhan.transformer.UserResponseTransformer;
import com.vigilonix.samadhan.validator.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder;
    private final ValidationService<UserRequest> userRequestValidationService;
    private final AuthTokenTransformer authTokenTransformer;
    private final UserResponseTransformer userResponseTransformer;
    private final SearchUserResponseTransformer searchUserResponseTransformer;
    private final ValidationService<AuthRequest> clientValidator;
    private final UserRepository userRepository;
    private final ChangeDetector changeDetector;
    private final GeoHierarchyService geoHierarchyService;
    private final UserRepositoryCustom userRepositoryCustom;

    public OAuth2Response login(AuthRequest authRequest) {
        clientValidator.validate(authRequest);
        User user = userRepository.findByUsername(authRequest.getUsername());
        if (user != null && !encoder.matches(authRequest.getPassword(), user.getSecret())) {
            user = null;
        }
        if (user == null) {
            log.error("invalid request {}", authRequest);
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_GRANT));
        }
        if (DEACTIVATED_STATES.contains(user.getState())) {
            user.setState(State.ACTIVE);
        }
        user.setLastLive(System.currentTimeMillis());
        userRepository.save(user);
        OAuthToken oAuthToken = tokenService.save(authRequest, user);
        return authTokenTransformer.transform(oAuthToken);
    }

    private User signUp(UserRequest userRequest) {
        userRequestValidationService.validate(userRequest);
        User user = User.builder()
                .email(userRequest.getEmail())
                .name(userRequest.getName())
                .secret(StringUtils.isNotEmpty(userRequest.getPassword()) ? encoder.encode(userRequest.getPassword()) : null)
                .username(userRequest.getUsername())
                .createdOn(System.currentTimeMillis())
                .modifiedOn(System.currentTimeMillis())
                .lastLive(System.currentTimeMillis())
                .rank(userRequest.getRank())
                .state(State.ACTIVE)
                .uuid(UUID.randomUUID())
                .role(Role.NORMAL)
                .phoneNumber(userRequest.getPhoneNumber())
                .postGeoHierarchyNodeUuidMap(userRequest.getPostGeoHierarchyNodeUuidMap())
                .build();
        userRepository.save(user);
        return user;
    }


    @Timed
    public void updateLocation(LocationRequest locationRequest, User principal) {
        log.info("update location user {} request {}", principal.getId(), locationRequest);
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
            userRepository.save(principal);
        }
    }


    public UserResponse updateUser(User principal, UserRequest userRequest) {
        userRequestValidationService.validate(userRequest);
        if (StringUtils.isNotEmpty(userRequest.getName())
                && changeDetector.isChanged(principal.getName(), userRequest.getName())) {
            principal.setName(userRequest.getName());

        }
        if (State.DISABLED.equals(principal.getState())) {
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.DISABLED_USER));
        }
        if (MapUtils.isNotEmpty(userRequest.getPostGeoHierarchyNodeUuidMap())) {
            geoHierarchyService.getFirstLevelNodes(userRequest.getPostGeoHierarchyNodeUuidMap()).forEach(uuid -> {
                        if (geoHierarchyService.getNodeById(uuid) == null) {
                            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_UUID));
                        }
                    }
            );
            principal.setPostGeoHierarchyNodeUuidMap(userRequest.getPostGeoHierarchyNodeUuidMap());
        }


        principal.setModifiedOn(System.currentTimeMillis());
        principal.setLastLive(System.currentTimeMillis());
        if(userRequest.getLatitude() != null
                && changeDetector.isChanged(principal.getLatitude(), userRequest.getLatitude())) {
            principal.setLatitude(userRequest.getLatitude());
        }
        if(userRequest.getLongitude() != null
                && changeDetector.isChanged(principal.getLongitude(), userRequest.getLongitude())) {
            principal.setLongitude(userRequest.getLongitude());
        }
        if(StringUtils.isNotEmpty(userRequest.getFirebaseDeviceToken())
                && changeDetector.isChanged(principal.getDeviceToken(), userRequest.getFirebaseDeviceToken())) {
            principal.setDeviceToken(userRequest.getFirebaseDeviceToken());
        }
        if(StringUtils.isNotEmpty(userRequest.getPassword())) {
            principal.setSecret(encoder.encode(userRequest.getPassword()));
        }

        log.info("going to save user {} after put api", principal);
        userRepository.save(principal);
        log.info("save successfully user {} after put api", principal);

        return userResponseTransformer.transform(principal);
    }


    public void logout(User principal) {
        tokenService.deleteByUserId(principal);
    }

    @LogPayload
    public OAuth2Response refreshToken(RefreshTokenRequest refreshTokenRequest) {
        OAuthToken authToken = tokenService.refreshStaleToken(refreshTokenRequest);
        return authTokenTransformer.transform(authToken);
    }

    public UserResponse getDetails(User principal) {
        return userResponseTransformer.transform(principal);
    }


    public void activateAccount(User principal) {
        if (!State.DISABLED.equals(principal.getState())) {
            principal.setState(State.ACTIVE);
            principal.setStateChangedOn(System.currentTimeMillis());
            userRepository.save(principal);
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
            try {
                userRepository.save(user);
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


    public String getDeleteVerifyResponse(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            User principal = userRepository.findByUuid(uuid);
            if (principal != null && State.DELETED.equals(principal.getState())) {
                return "<html>This user has been deleted</html>";
            }
            return "<html>Deletion is in progress </html>";
        } catch (Exception e) {
            log.error("invalid delete verification request {}", id, e);
        }
        throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_ID));
    }

    public List<UserResponse> getAllUsersFromSameGeoFence(User principal, String prefixName) {

        List<UUID> geoHierarchyUuid = geoHierarchyService.getAllLevelNodes(principal.getPostGeoHierarchyNodeUuidMap());

        String uuidString = geoHierarchyUuid.stream()
                .map(UUID::toString)
                .collect(Collectors.joining("','", "'", "'"));
        List<User> users = userRepositoryCustom.findByPrefixNameAndGeoNodeIn(StringUtils.lowerCase(prefixName), geoHierarchyUuid);
//        List<User> users = userRepository.findByNameStartingWith(StringUtils.lowerCase(prefixName));
        return users.stream().map(searchUserResponseTransformer::transform).collect(Collectors.toList());

    }

    public void signUpViaUserPassBulk(List<UserRequest> userRequests, User principal) {
        for(UserRequest userRequest: userRequests) {
            signUpViaUserPass(userRequest, principal);
        }
    }

    @Timed
    public List<GeoHierarchyNode> searchGeoFence(User principal, String prefixName) {
        return geoHierarchyService.getAllLevelNodes(principal.getPostGeoHierarchyNodeUuidMap())
                .stream()
                .map(u -> GeoHierarchyNode.builder()
                        .uuid(u)
                        .name(geoHierarchyService.getNodeById(u).getName())
                        .build())
                .filter(n-> StringUtils.isEmpty(prefixName) || n.getName().toLowerCase().contains(prefixName.toLowerCase()))
                .collect(Collectors.toList());
    }
}
