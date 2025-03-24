package com.vigilonix.samadhan.model;

import com.vigilonix.samadhan.enums.ApplicationPriority;
import com.vigilonix.samadhan.enums.ApplicationCategory;
import com.vigilonix.samadhan.enums.OdApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "od_application")
@Table(indexes = {
        @Index(name = "od_status_index", columnList = "od_id, status"),
        @Index(name = "enquiry_officer_status_index", columnList = "enquiry_officer_id, status"),
        @Index(name = "geo_hierarchy_node_uuid_status_index", columnList = "geoHierarchyNodeUuid, status"),
})
@Getter
@AllArgsConstructor
@Builder
@Setter
@NoArgsConstructor
@ToString
public class OdApplication {
    @Id
    @Column
    private UUID uuid;
    @Column
    private String applicantName;
    @Column
    private String applicantPhoneNumber;
    @ManyToOne
    private User od;
    @Column
    private String applicationFilePath;
    @Column
    private UUID geoHierarchyNodeUuid;
    //TODO multiple users can be endorsed.
    @ManyToOne
    private User enquiryOfficer;
    @Column
    @Enumerated(EnumType.STRING)
    private OdApplicationStatus status;
    @Column
    private String receiptNo;
    @Column
    private Integer receiptBucketNumber;
    @Column
    private Long createdAt;
    @Column
    private Long modifiedAt;
    @Column
    private Long dueEpoch;
    @Column
    @Enumerated(EnumType.STRING)
    private ApplicationPriority priority;
    @Column
    @Enumerated(EnumType.STRING)
    private ApplicationCategory category;
}
