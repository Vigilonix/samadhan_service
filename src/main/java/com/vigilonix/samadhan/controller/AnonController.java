package com.vigilonix.samadhan.controller;

import com.vigilonix.samadhan.service.AnonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/anon")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AnonController {
    private final AnonService anonService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(path = "/wba/chat/webhoook", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void wbaChatWebhook(@RequestBody String payload) {
        anonService.wbaChatWebhook(payload);
    }

}
