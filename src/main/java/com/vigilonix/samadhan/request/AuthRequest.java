package com.vigilonix.samadhan.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;


@AllArgsConstructor
@Getter
@Builder
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthRequest {
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @NotEmpty(message = "grant type is required")
    @JsonProperty("grant_type")
    private String grantType;
    @NotEmpty(message = "scope is missing")
    @JsonProperty("scope")
    private String scope;
    @NotEmpty(message = "client_secret is missing")
    @JsonProperty("client_secret")
    private String clientSecret;
    @NotEmpty(message = "client_id is missing")
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("auth_token")
    private String authToken;
}