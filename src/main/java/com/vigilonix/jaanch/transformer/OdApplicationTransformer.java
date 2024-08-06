package com.vigilonix.jaanch.transformer;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.model.User;
import com.vigilonix.jaanch.pojo.ODApplicationPojo;
import com.vigilonix.jaanch.pojo.ODApplicationStatus;
import com.vigilonix.jaanch.pojo.ODApplicationTransformationRequest;
import com.vigilonix.jaanch.service.FieldGeoService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OdApplicationTransformer implements Transformer<ODApplicationTransformationRequest, ODApplicationPojo> {
    private final FieldGeoService fieldGeoService;
    @Override
    public ODApplicationPojo transform(ODApplicationTransformationRequest odApplicationTransformationRequest) {
        User principalUser = odApplicationTransformationRequest.getPrincipalUser();
        OdApplication odApplication = odApplicationTransformationRequest.getOdApplication();
        return ODApplicationPojo.builder()
                .uuid(odApplication.getUuid())
                .applicantName(odApplication.getApplicantName())
                .applicantPhoneNumber(odApplication.getApplicantPhoneNumber())
                .odUuid(odApplication.getOd().getUuid())
                .odName(odApplication.getOd().getName())
                .applicationFilePath(odApplication.getApplicationFilePath())
                .fieldGeoNodeUuid(odApplication.getFieldGeoNodeUuid())
                .enquiryOfficerUuid(odApplication.getEnquiryOfficer()!=null?odApplication.getEnquiryOfficer().getUuid():null)
                .enquiryOfficerName(odApplication.getEnquiryOfficer()!=null?odApplication.getEnquiryOfficer().getName():null)
                .enquiryFilePath(odApplication.getEnquiryFilePath())
                .status(odApplication.getStatus())
                .receiptNo(odApplication.getReceiptNo())
                .createdAt(odApplication.getCreatedAt())
                .modifiedAt(odApplication.getModifiedAt())
                .hasAuthorityOnReviewStatus(ODApplicationStatus.REVIEW.equals(odApplication.getStatus()) && fieldGeoService.hasGeoAuthority(odApplication.getFieldGeoNodeUuid(), principalUser))
                .hasAuthorityOnEnquiryStatus(ODApplicationStatus.ENQUIRY.equals(odApplication.getStatus()) && principalUser.getUuid().equals(odApplication.getEnquiryOfficer().getUuid()))
                .hasAuthorityOnOpenStatus(ODApplicationStatus.OPEN.equals(odApplication.getStatus()) && fieldGeoService.hasGeoAuthority(odApplication.getFieldGeoNodeUuid(), principalUser))
                .build();
    }
}
