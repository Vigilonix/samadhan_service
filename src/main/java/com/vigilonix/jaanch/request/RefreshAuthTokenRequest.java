package com.vigilonix.jaanch.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
public class RefreshAuthTokenRequest {
    @JsonProperty("token")
    String token;
    @JsonProperty("refresh_token")
    String refreshToken;
    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("client_secret")
    private String clientSecret;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("sso_provider")
    private String ssoProvider;
}
