package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@RequiredArgsConstructor
@ToString
public class ODApplicationValidationPayload {
    private final User principalUser;
    private final OdApplicationPayload odApplicationPayload;
    private final OdApplication odApplication;
    private final User enquiryUser;
}
