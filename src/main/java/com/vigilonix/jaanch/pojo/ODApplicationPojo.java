package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ODApplicationPojo {
    private UUID uuid;
    private String applicantName;
    private String applicantPhoneNumber;
    private UUID odUuid;
    private String odName;
    private String applicationFilePath;
    private UUID fieldGeoNodeUuid;
    private UUID enquiryOfficerUuid;
    private String enquiryOfficerName;
    private String enquiryFilePath;
    private Long enquirySubmittedAt;
    private ODApplicationStatus status;
    private String receiptNo;
    private Long createdAt;
    private Long modifiedAt;
}
