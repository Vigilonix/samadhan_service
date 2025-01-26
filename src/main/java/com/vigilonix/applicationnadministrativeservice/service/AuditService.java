package com.vigilonix.applicationnadministrativeservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigilonix.applicationnadministrativeservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuditService {
    public static final String REQUEST_BY_USER_ID = "request by userId: {} uri: {} payload: {}";
    public static final String FAILED_TO_AUDIT_REQUEST_FOR_TOKEN_FOR_USER_REQUEST = "failed to audit request for userId: {} uri: {} body: {}";
    private final ThreadPoolExecutor threadPoolExecutor;
    private final ObjectMapper objectMapper;

    public void audit(User principal, String requestURI, String method, Object body) {
        threadPoolExecutor.submit(() -> {
            try {
                String jsonPayload = objectMapper.writeValueAsString(body);
                log.info(REQUEST_BY_USER_ID, principal.getId(), requestURI, method, jsonPayload);
            } catch (Exception ex) {
                log.error(FAILED_TO_AUDIT_REQUEST_FOR_TOKEN_FOR_USER_REQUEST, principal.getId(),requestURI, method,  body);
            }
        });
    }
}
