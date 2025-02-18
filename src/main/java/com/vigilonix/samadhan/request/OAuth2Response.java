package com.vigilonix.samadhan.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OAuth2Response {
    @JsonProperty(value = "access_token")
    private final String accessToken;
    @JsonProperty(value = "token_type")
    private final String tokenType;
    @JsonProperty(value = "expire_on")
    private final long expireOn;
    @JsonProperty(value = "scope")
    private final String scope;
    @JsonProperty(value = "refresh_token")
    private final String refreshToken;
}
