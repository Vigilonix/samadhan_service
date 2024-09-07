package com.vigilonix.jaanch.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@Getter
@ToString
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefreshTokenRequest {
    @JsonProperty("auth_token")
    private String authToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
}
