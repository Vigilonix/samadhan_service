package com.vigilonix.samadhan.pojo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.vigilonix.samadhan.enums.ActorType;
import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OdAssignmentPayload {
    private UUID uuid;
    private UUID assigneeUuid;
    private String assigneeName;
    private OdApplicationStatus status;
    private String filePath;
    private String comment;
    private Long createdAt;
    private Long modifiedAt;
    private UUID actorUuid;
    private UUID geoHierarchyNodeUuid;
    private String geoHierarchyNodeName;
    private String actorName;
    private ActorType actorType;
    private Boolean hasAuthorityOnEnquiryStatus;
}
