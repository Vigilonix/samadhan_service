package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.request.OAuth2Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AnonService {
    public OAuth2Response wbaChatWebhook(Map<String, Object> payload) {
        log.info("wba chat webhook received {}", payload);
        return null;
    }
}
