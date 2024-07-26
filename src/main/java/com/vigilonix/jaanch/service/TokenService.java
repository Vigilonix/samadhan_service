package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.config.Constant;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import com.vigilonix.jaanch.exception.ValidationRuntimeException;
import com.vigilonix.jaanch.model.OAuthToken;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.repository.OAuthTokenDao;
import com.vigilonix.jaanch.request.AuthRequest;
import com.vigilonix.jaanch.request.RefreshTokenRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Component
@AllArgsConstructor
@Transactional
@Slf4j
public class TokenService {
    public static final String FAILED_TO_FIND_TOKEN_FOR_REFRESH_TOKEN_REQUEST = "failed to find token for refreshToken request {}";
    private final com.vigilonix.jaanch.repository.OAuthTokenDao OAuthTokenDao;

    public OAuthToken save(AuthRequest authRequest, User user) {
        OAuthToken oAuthToken = OAuthToken.builder()
                .createTime(System.currentTimeMillis())
                .expireTime(System.currentTimeMillis() + Constant.TOKEN_EXPIRE_TIME_IN_MS)
                .user(user)
                .clientId(authRequest.getClientId())
                .clientSecret(authRequest.getClientSecret())
                .grantType(authRequest.getGrantType())
                .scope(authRequest.getScope())
                .refreshToken(UUID.randomUUID().toString())
                .token(UUID.randomUUID().toString())
                .build();
        OAuthTokenDao.deleteByUser(user);
        OAuthTokenDao.save(oAuthToken);
        return oAuthToken;
    }

    public OAuthToken validateToken(String authToken) {
        return OAuthTokenDao.findByTokenAndExpireTimeGreaterThan(authToken, System.currentTimeMillis());
    }

    public void deleteByUserId(User user) {
        OAuthTokenDao.deleteByUser(user);
    }

    public void removeStaleToken() {
        OAuthTokenDao.deleteByExpireTime(System.currentTimeMillis() - Constant.TOKEN_EXPIRE_BUFFER);
    }

    public OAuthToken refreshStaleToken(RefreshTokenRequest refreshTokenRequest) {
        OAuthToken token = OAuthTokenDao.
                findByTokenAndRefreshToken(refreshTokenRequest.getAuthToken(), refreshTokenRequest.getRefreshToken());
        if (token == null) {
            log.error(FAILED_TO_FIND_TOKEN_FOR_REFRESH_TOKEN_REQUEST, refreshTokenRequest);
            throw new ValidationRuntimeException(Collections.singletonList(ValidationErrorEnum.INVALID_TOKEN));
        }
        OAuthToken newToken = OAuthToken.builder()
                .createTime(token.getCreateTime())
                .expireTime(System.currentTimeMillis() + Constant.TOKEN_EXPIRE_TIME_IN_MS)
                .user(token.getUser())
                .clientId(token.getClientId())
                .clientSecret(token.getClientSecret())
                .grantType(token.getGrantType())
                .scope(token.getScope())
                .refreshToken(UUID.randomUUID().toString())
                .token(token.getUser().getUuid().toString())
                .build();
        OAuthTokenDao.delete(token);
        OAuthTokenDao.save(newToken);
        return newToken;
    }
}
