package com.vigilonix.samadhan.pojo;

import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.User;
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
