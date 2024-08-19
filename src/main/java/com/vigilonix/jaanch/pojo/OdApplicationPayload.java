package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.UUID;

@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OdApplicationPayload {
    private UUID uuid;
    private String applicantName;
    private String applicantPhoneNumber;
    private UUID odUuid;
    private String odName;
    private String applicationFilePath;
    private UUID fieldGeoNodeUuid;
    private String fieldGeoNodeName;
    private UUID enquiryOfficerUuid;
    private String enquiryOfficerName;
    private String enquiryFilePath;
    private Long enquirySubmittedAt;
    private ODApplicationStatus status;
    private String receiptNo;
    private Long createdAt;
    private Long modifiedAt;
    private boolean hasAuthorityOnOpenStatus;
    private boolean hasAuthorityOnEnquiryStatus;
    private boolean hasAuthorityOnReviewStatus;
    private boolean hasAuthorityOnClosedStatus;
    private boolean hasAuthorityToReassign;
}
