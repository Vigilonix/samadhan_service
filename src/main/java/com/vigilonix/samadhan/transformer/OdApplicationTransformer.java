package com.vigilonix.samadhan.transformer;

import com.vigilonix.samadhan.model.OdApplication;
import com.vigilonix.samadhan.model.User;
import com.vigilonix.samadhan.pojo.EnquiryPayload;
import com.vigilonix.samadhan.pojo.OdApplicationPayload;
import com.vigilonix.samadhan.pojo.OdApplicationStatus;
import com.vigilonix.samadhan.pojo.ODApplicationTransformationRequest;
import com.vigilonix.samadhan.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OdApplicationTransformer implements Transformer<ODApplicationTransformationRequest, OdApplicationPayload> {
    private final GeoHierarchyService geoHierarchyService;
    private final OdApplicationAssignmentTransformer odApplicationAssignmentTransformer;
    @Override
    public OdApplicationPayload transform(ODApplicationTransformationRequest odApplicationTransformationRequest) {
        User principalUser = odApplicationTransformationRequest.getPrincipalUser();
        OdApplication odApplication = odApplicationTransformationRequest.getOdApplication();
        boolean hasAuthorityOnEnquiryStatus = CollectionUtils.isNotEmpty(odApplicationTransformationRequest.getAssignments()) && odApplicationTransformationRequest.getAssignments().stream().anyMatch(a-> a.getEnquiryOfficer().getUuid().equals(principalUser.getUuid()));
        return OdApplicationPayload.builder()
                .uuid(odApplication.getUuid())
                .applicantName(odApplication.getApplicantName())
                .applicantPhoneNumber(odApplication.getApplicantPhoneNumber())
                .odUuid(odApplication.getOd().getUuid())
                .odName(odApplication.getOd().getName())
                .applicationFilePath(odApplication.getApplicationFilePath())
                .geoHierarchyNodeUuid(odApplication.getGeoHierarchyNodeUuid())
                .geoHierarchyNodeName(geoHierarchyService.getNodeById(odApplication.getGeoHierarchyNodeUuid()).getName())
                .status(odApplication.getStatus())
                .receiptNo(odApplication.getReceiptNo())
                .createdAt(odApplication.getCreatedAt())
                .modifiedAt(odApplication.getModifiedAt())
                .hasAuthorityOnReviewStatus(geoHierarchyService.hasAuthority(odApplication.getGeoHierarchyNodeUuid(), principalUser.getPostGeoHierarchyNodeUuidMap()))
                .hasAuthorityOnEnquiryStatus(OdApplicationStatus.ENQUIRY.equals(odApplication.getStatus()) && hasAuthorityOnEnquiryStatus)
                .hasAuthorityOnOpenStatus(OdApplicationStatus.OPEN.equals(odApplication.getStatus()) && geoHierarchyService.hasAuthority(odApplication.getGeoHierarchyNodeUuid(), principalUser.getPostGeoHierarchyNodeUuidMap()))
                .hasAuthorityOnClosedStatus(OdApplicationStatus.CLOSED.equals(odApplication.getStatus()) && geoHierarchyService.hasAuthority(odApplication.getGeoHierarchyNodeUuid(), principalUser.getPostGeoHierarchyNodeUuidMap()))
                .hasAuthorityToReassign(Arrays.asList(OdApplicationStatus.ENQUIRY, OdApplicationStatus.REVIEW).contains(odApplication.getStatus()) && geoHierarchyService.hasAuthority(odApplication.getGeoHierarchyNodeUuid(), principalUser.getPostGeoHierarchyNodeUuidMap()))
                .category(odApplication.getCategory())
                .assignments(CollectionUtils.isEmpty(odApplicationTransformationRequest.getAssignments())?Collections.emptyList() : odApplicationTransformationRequest.getAssignments().stream().map(odApplicationAssignmentTransformer::transform).collect(Collectors.toList()))
                .build();
    }
}
