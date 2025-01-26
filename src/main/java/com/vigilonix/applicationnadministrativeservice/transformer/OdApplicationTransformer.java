package com.vigilonix.applicationnadministrativeservice.transformer;

import com.vigilonix.applicationnadministrativeservice.model.OdApplication;
import com.vigilonix.applicationnadministrativeservice.model.User;
import com.vigilonix.applicationnadministrativeservice.pojo.EnquiryPayload;
import com.vigilonix.applicationnadministrativeservice.pojo.OdApplicationPayload;
import com.vigilonix.applicationnadministrativeservice.pojo.OdApplicationStatus;
import com.vigilonix.applicationnadministrativeservice.pojo.ODApplicationTransformationRequest;
import com.vigilonix.applicationnadministrativeservice.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OdApplicationTransformer implements Transformer<ODApplicationTransformationRequest, OdApplicationPayload> {
    private final GeoHierarchyService geoHierarchyService;
    @Override
    public OdApplicationPayload transform(ODApplicationTransformationRequest odApplicationTransformationRequest) {
        User principalUser = odApplicationTransformationRequest.getPrincipalUser();
        OdApplication odApplication = odApplicationTransformationRequest.getOdApplication();
        return OdApplicationPayload.builder()
                .uuid(odApplication.getUuid())
                .applicantName(odApplication.getApplicantName())
                .applicantPhoneNumber(odApplication.getApplicantPhoneNumber())
                .odUuid(odApplication.getOd().getUuid())
                .odName(odApplication.getOd().getName())
                .applicationFilePath(odApplication.getApplicationFilePath())
                .geoHierarchyNodeUuid(odApplication.getGeoHierarchyNodeUuid())
                .geoHierarchyNodeName(geoHierarchyService.getNodeById(odApplication.getGeoHierarchyNodeUuid()).getName())
                .enquiries(CollectionUtils.isEmpty(odApplication.getEnquiries()) ?null : odApplication.getEnquiries().stream().map(e-> EnquiryPayload.builder().path(e.getPath()).ownerUuid(e.getOwnerUuid()).createdAt(e.getCreatedAt()).enquiryOfficeName("foo").build()).collect(Collectors.toList()))
                .status(odApplication.getStatus())
                .receiptNo(odApplication.getReceiptNo())
                .createdAt(odApplication.getCreatedAt())
                .modifiedAt(odApplication.getModifiedAt())
                .hasAuthorityOnReviewStatus(OdApplicationStatus.REVIEW.equals(odApplication.getStatus()) && geoHierarchyService.hasAuthority(odApplication.getGeoHierarchyNodeUuid(), principalUser.getPostGeoHierarchyNodeUuidMap()))
                .hasAuthorityOnEnquiryStatus(OdApplicationStatus.ENQUIRY.equals(odApplication.getStatus()) && (principalUser.getUuid().equals(odApplication.getEnquiryOfficer().getUuid())))
                .hasAuthorityOnOpenStatus(OdApplicationStatus.OPEN.equals(odApplication.getStatus()) && geoHierarchyService.hasAuthority(odApplication.getGeoHierarchyNodeUuid(), principalUser.getPostGeoHierarchyNodeUuidMap()))
                .hasAuthorityOnClosedStatus(OdApplicationStatus.CLOSED.equals(odApplication.getStatus()) && geoHierarchyService.hasAuthority(odApplication.getGeoHierarchyNodeUuid(), principalUser.getPostGeoHierarchyNodeUuidMap()))
                .hasAuthorityToReassign(Arrays.asList(OdApplicationStatus.ENQUIRY, OdApplicationStatus.REVIEW).contains(odApplication.getStatus()) && geoHierarchyService.hasAuthority(odApplication.getGeoHierarchyNodeUuid(), principalUser.getPostGeoHierarchyNodeUuidMap()))
                .build();
    }
}
