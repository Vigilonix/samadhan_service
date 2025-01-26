package com.vigilonix.applicationnadministrativeservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuthToken {
    @ManyToOne()
    private User user;

    @Id
    @Column(name = "id", nullable = false, length = 2049)
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
