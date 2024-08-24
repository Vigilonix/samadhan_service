package com.vigilonix.jaanch.transformer;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.OdApplicationPayload;
import com.vigilonix.jaanch.pojo.OdApplicationStatus;
import com.vigilonix.jaanch.pojo.ODApplicationTransformationRequest;
import com.vigilonix.jaanch.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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
                .enquiryOfficerUuid(odApplication.getEnquiryOfficer()!=null?odApplication.getEnquiryOfficer().getUuid():null)
                .enquiryOfficerName(odApplication.getEnquiryOfficer()!=null?odApplication.getEnquiryOfficer().getName():null)
                .enquiryFilePath(odApplication.getEnquiryFilePath())
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
