package com.vigilonix.jaanch.transformer;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.ODApplicationPojo;
import org.apache.commons.collections4.Transformer;
import org.springframework.stereotype.Component;

@Component
public class OdApplicationTransformer implements Transformer<OdApplication, ODApplicationPojo> {
    @Override
    public ODApplicationPojo transform(OdApplication odApplication) {
        return ODApplicationPojo.builder()
                .uuid(odApplication.getUuid())
                .applicantName(odApplication.getApplicantName())
                .applicantPhoneNumber(odApplication.getApplicantPhoneNumber())
                .ODUuid(odApplication.getOdUuid())
                .applicationFilePath(odApplication.getApplicationFilePath())
                .fieldGeoNodeUuid(odApplication.getFieldGeoNodeUuid())
                .enquiryOfficerUuid(odApplication.getEnquiryOfficerUuid())
                .enquiryFilePath(odApplication.getEnquiryFilePath())
                .status(odApplication.getStatus())
                .receiptNo(odApplication.getReceiptNo())
                .createdAt(odApplication.getCreatedAt())
                .modifiedAt(odApplication.getModifiedAt())
                .build();
    }
}
