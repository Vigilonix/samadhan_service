package com.vigilonix.jaanch.controller;

import com.vigilonix.jaanch.request.OAuth2Response;
import com.vigilonix.jaanch.service.AnonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/anon")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AnonController {
    private final AnonService anonService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/wba/chat/webhoook", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public OAuth2Response wbaChatWebhook(@RequestBody Map<String, Object> payload) {
        return anonService.wbaChatWebhook(payload);
    }

}
