package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
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
