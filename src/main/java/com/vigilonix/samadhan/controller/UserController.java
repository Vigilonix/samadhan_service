package com.vigilonix.samadhan.controller;

import com.vigilonix.samadhan.aop.LogPayload;
import com.vigilonix.samadhan.helper.AuthHelper;
import com.vigilonix.samadhan.request.OAuth2Response;
import com.vigilonix.samadhan.request.UserRequest;
import com.vigilonix.samadhan.request.UserResponse;
import com.vigilonix.samadhan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(value = "/bulk", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void bulkSignup(@RequestBody List<UserRequest> userRequests) {
        userService.signUpViaUserPassBulk(userRequests, authHelper.getPrincipal());
    }

    @LogPayload
    @PutMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse updateUser(@RequestBody UserRequest userRequest) {
        return userService.updateUser(authHelper.getPrincipal(), userRequest);
    }

    @LogPayload
    @PostMapping(path = "/logout", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void logout() {
        userService.logout(authHelper.getPrincipal());
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserResponse getDetails() {
        return userService.getDetails(authHelper.getPrincipal());
    }

    @GetMapping(path = "/peer", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<UserResponse> getAllUsersFromSameGeoFences(@RequestParam(value = "prefix_name", defaultValue = "")String prefixName) {
        return userService.getAllUsersFromSameGeoFence(authHelper.getPrincipal(), prefixName);
    }


}
