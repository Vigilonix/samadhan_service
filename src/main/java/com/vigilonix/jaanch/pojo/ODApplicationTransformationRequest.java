package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ODApplicationTransformationRequest {
    private final OdApplication odApplication;
    private final User principalUser;
}
