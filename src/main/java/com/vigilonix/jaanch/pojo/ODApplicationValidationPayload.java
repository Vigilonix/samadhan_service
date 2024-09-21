package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
@ToString
public class ODApplicationValidationPayload {
    private final User principalUser;
    private final OdApplicationPayload odApplicationPayload;
    private final OdApplication odApplication;
    private final User enquiryUser;
    private final List<UUID> geoHierarchyNodeUuids;
}
