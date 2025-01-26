package com.vigilonix.applicationnadministrativeservice.pojo;

import com.vigilonix.applicationnadministrativeservice.model.OdApplication;
import com.vigilonix.applicationnadministrativeservice.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class ODApplicationTransformationRequest {
    private final OdApplication odApplication;
    private final User principalUser;
}
