package com.vigilonix.samadhan.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;
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
//    @JsonProperty("field_geo_node_uuid")
    private UUID geoHierarchyNodeUuid;
//    @JsonProperty("field_geo_node_name")
    private String geoHierarchyNodeName;
    private UUID enquiryOfficerUuid;
    private String enquiryOfficerName;
    private List<EnquiryPayload> enquiries;
    private OdApplicationStatus status;
    private String receiptNo;
    private Long createdAt;
    private Long modifiedAt;
    private boolean hasAuthorityOnOpenStatus;
    private boolean hasAuthorityOnEnquiryStatus;
    private boolean hasAuthorityOnReviewStatus;
    private boolean hasAuthorityOnClosedStatus;
    private boolean hasAuthorityToReassign;
}
