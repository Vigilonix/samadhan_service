package com.vigilonix.jaanch.controller;

import com.dt.beyond.aop.LogPayload;
import com.dt.beyond.config.AuthHelper;
import com.dt.beyond.pojo.*;
import com.dt.beyond.request.*;
import com.dt.beyond.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(UserController.V_1_USER)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    public static final String V_1_USER = "/v1/user";
    private final UserService userService;
    private final AuthHelper authHelper;

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2Response signup(@RequestBody UserRequest userRequest) {
        return userService.signUpViaUserPass(userRequest, authHelper.getPrincipal());
    }


    @PostMapping(path = "/location", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateLocation(@Valid @RequestBody LocationRequest locationRequest) {
        userService.updateLocation(locationRequest, authHelper.getPrincipal());
    }

    @PostMapping(path = "/media", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @LogPayload
    public List<UserMediaResponse> addMedia(@RequestBody List<MediaRequest> mediaRequests) {
        return userService.addMedia(mediaRequests, authHelper.getPrincipal());
    }

    @LogPayload
    @PutMapping(path = "/media", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<UserMediaResponse> updateMediaPosition(@RequestBody List<MediaRequest> mediaRequests) {
        return userService.updateMedia(mediaRequests, authHelper.getPrincipal());
    }

    @DeleteMapping(path = "/media/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteMediaPosition(@PathVariable(name = "uuid") String uuid) {
        userService.dropMedia(uuid, authHelper.getPrincipal());
    }

    @LogPayload
    @PutMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse updateUser(@Valid @RequestBody UserRequest userRequest) {
        return userService.updateUser(authHelper.getPrincipal(), userRequest);
    }

    @PostMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void logout() {
        userService.logout(authHelper.getPrincipal());
    }

    @DeleteMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public FbGdprResponse deleteAccount() {
        return userService.deleteAccount(authHelper.getPrincipal());
    }

    @PostMapping(path = "/state", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public FbGdprResponse changeState(@Valid @RequestBody UserStateChaneRequest userStateChaneRequest) {
        return userService.changeState(userStateChaneRequest, authHelper.getPrincipal());
    }

    @PostMapping(path = "/activate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void activateAccount() {
        userService.activateAccount(authHelper.getPrincipal());
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse getDetails() {
        return userService.getDetails(authHelper.getPrincipal());
    }

    @PostMapping(path = "/social_media/long_lived_token", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SocialMediaResponse getSpotifyLongLivedToken(@RequestBody SocialMediaLongLivedTokenRequest socialMediaLongLivedTokenRequest) {
        return userService.getSocialMediaLongLivedToken(socialMediaLongLivedTokenRequest, authHelper.getPrincipal());
    }

    @PostMapping(path = "/boost", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void boost() {
        userService.boost(authHelper.getPrincipal());
    }

    @GetMapping(path = "/referral/users", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public AllReferralResponse getAllReferredUsers() {
        return userService.getAllReferredUsers(authHelper.getPrincipal());
    }

    @GetMapping(path = "/firebase/token", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SocialMediaResponse getFirebaseToken() {
        return userService.getFirebaseToken(authHelper.getPrincipal());
    }


    @PostMapping(path = "/device_token", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateDeviceToken(@Valid @RequestBody DeviceTokenRequest deviceTokenRequest) {
        userService.updateDeviceToken(deviceTokenRequest, authHelper.getPrincipal());
    }

    @GetMapping(path = "/geoLocation", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GeoLocationResponse getGeoLocation() {
        return userService.getGeoLocation(authHelper.getPrincipal());
    }

}
