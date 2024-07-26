package com.vigilonix.jaanch.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthToken {
    @OneToOne()
    private User user;

    @Id
    @Column(name = "id", nullable = false, length = 2048)
    private String token;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "grant_type", nullable = false)
    private String grantType;

    @Column(name = "scope", nullable = false)
    private String scope;

    @Column(name = "create_time", nullable = false)
    private Long createTime;

    @Column(name = "expire_time", nullable = false)
    private Long expireTime;
}
