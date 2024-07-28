package com.vigilonix.jaanch.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.config.AppConfig;
import com.vigilonix.jaanch.enums.Role;
import com.vigilonix.jaanch.enums.State;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.exception.ValidationRuntimeException;
import com.vigilonix.jaanch.helper.ChangeDetector;
import com.vigilonix.jaanch.model.OAuthToken;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.repository.UserRepository;
import com.vigilonix.jaanch.request.*;
import com.vigilonix.jaanch.transformer.AuthTokenTransformer;
import com.vigilonix.jaanch.transformer.UserResponseTransformer;
import com.vigilonix.jaanch.validator.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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
    private final TokenService tokenService;
    private final BCryptPasswordEncoder encoder;
    private final ValidationService<UserRequest> userRequestValidationService;
    private final AuthTokenTransformer authTokenTransformer;
    private final UserResponseTransformer userResponseTransformer;
    private final ValidationService<AuthRequest> clientValidator;
    private final UserRepository userRepository;
    private final ChangeDetector changeDetector;
    private final FieldGeoService fieldGeoService;

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

    public OAuth2Response login(AuthRequest authRequest) {
        clientValidator.validate(authRequest);
        User user = userRepository.findByUsername(authRequest.getUsername());
        if (user != null && !encoder.matches(authRequest.getPassword(), user.getSecret())) {
            user = null;
        }
        if (user == null) {
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
                .postFieldGeoNodeUuidMap(userRequest.getPostFieldGeoNodeUuidMap())
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
        if (MapUtils.isNotEmpty(userRequest.getPostFieldGeoNodeUuidMap())) {
            userRequest.getPostFieldGeoNodeUuidMap().values().stream().flatMap(Collection::stream).forEach(uuid -> {
                        if (fieldGeoService.getFieldGeoNode(uuid) == null) {
                            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_UUID));
                        }
                    }
            );
            principal.setPostFieldGeoNodeUuidMap(userRequest.getPostFieldGeoNodeUuidMap());
        }


        principal.setModifiedOn(System.currentTimeMillis());
        principal.setLastLive(System.currentTimeMillis());
        log.info("going to save user {} after put api", principal);
        userRepository.save(principal);
        log.info("save successfully user {} after put api", principal);

        return userResponseTransformer.transform(principal);
    }


    public void logout(User principal) {
        tokenService.deleteByUserId(principal);
    }

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
}
