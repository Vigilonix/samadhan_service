package com.vigilonix.samadhan.controller;

import com.vigilonix.samadhan.request.AuthRequest;
import com.vigilonix.samadhan.request.OAuth2Response;
import com.vigilonix.samadhan.request.RefreshTokenRequest;
import com.vigilonix.samadhan.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AuthController.OAUTH)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthController {
    public static final String TOKEN = "/token";
    public static final String REFRESH_TOKEN = "/refresh_token";
    public static final String OAUTH = "/oauth";
    private final UserService userService;

    @PostMapping(path = TOKEN, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2Response login(@Valid @RequestBody AuthRequest athRequest) {
        return userService.login(athRequest);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = REFRESH_TOKEN, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2Response refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return userService.refreshToken(refreshTokenRequest);
    }
}
