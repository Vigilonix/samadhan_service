package com.vigilonix.applicationnadministrativeservice.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponse {
    private final OAuth2Response oAuth2Response;
    private final int statusCode;
}
