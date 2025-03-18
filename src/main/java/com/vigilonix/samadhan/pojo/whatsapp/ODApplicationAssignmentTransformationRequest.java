package com.vigilonix.samadhan.pojo.whatsapp;

import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.OdApplicationAssignment;
import com.vigilonix.samadhan.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class ODApplicationAssignmentTransformationRequest {
    private final User principalUser;
    private final OdApplicationAssignment assignment;
}
