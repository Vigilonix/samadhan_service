package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vigilonix.jaanch.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
public class ODApplicationValidationPayload {
    private final User principalUser;
    private final ODApplicationPayload odApplicationPayload;
}
