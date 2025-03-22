package com.vigilonix.samadhan.pojo;

import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.OdApplicationAssignment;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.whatsapp.ODApplicationAssignmentTransformationRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class ODApplicationTransformationRequest {
    private final OdApplication odApplication;
    private final User principalUser;
    private final List<ODApplicationAssignmentTransformationRequest> assignments;
}
