package com.vigilonix.jaanch.repository;

import com.vigilonix.jaanch.model.OAuthToken;
import com.vigilonix.jaanch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuthTokenDao extends JpaRepository<OAuthToken, String> {
    OAuthToken findByUserAndExpireTimeGreaterThan(User user, Long expireTime);

    OAuthToken findByToken(String token);

    OAuthToken findByTokenAndExpireTimeGreaterThan(String authToken, long currentTimeMillis);

    void deleteByUser(User user);

    void deleteByExpireTime(long currentTimeMillis);

    OAuthToken findByTokenAndRefreshToken(String authToken, String refreshToken);
}
