package com.vigilonix.samadhan.transformer;

import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.OdApplicationAssignment;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.ODApplicationTransformationRequest;
import com.vigilonix.samadhan.pojo.OdApplicationPayload;
import com.vigilonix.samadhan.pojo.OdApplicationStatus;
import com.vigilonix.samadhan.pojo.OdAssignmentPayload;
import com.vigilonix.samadhan.pojo.whatsapp.ODApplicationAssignmentTransformationRequest;
import com.vigilonix.samadhan.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OdApplicationAssignmentTransformer implements Transformer<ODApplicationAssignmentTransformationRequest, OdAssignmentPayload> {
    private final GeoHierarchyService geoHierarchyService;
    @Override
    public OdAssignmentPayload transform(ODApplicationAssignmentTransformationRequest odApplicationAssignmentTransformationRequest) {
        OdApplicationAssignment odApplicationAssignment = odApplicationAssignmentTransformationRequest.getAssignment();
        return
                OdAssignmentPayload.builder()
                        .uuid(odApplicationAssignment.getUuid())
                        .filePath(odApplicationAssignment.getFilePath())
                        .status(odApplicationAssignment.getStatus())
                        .createdAt(odApplicationAssignment.getCreatedAt())
                        .geoHierarchyNodeName(geoHierarchyService.getNodeById(odApplicationAssignment.getGeoHierarchyNodeUuid()).getName())
                        .geoHierarchyNodeUuid(odApplicationAssignment.getGeoHierarchyNodeUuid())
                        .modifiedAt(odApplicationAssignment.getModifiedAt())
                        .comment(odApplicationAssignment.getComment())
                        .hasAuthorityOnEnquiryStatus(geoHierarchyService
                                .hasAuthority(odApplicationAssignment.getGeoHierarchyNodeUuid(),
                                    odApplicationAssignmentTransformationRequest.getPrincipalUser().getPostGeoHierarchyNodeUuidMap()))
                        .build();
    }
}
