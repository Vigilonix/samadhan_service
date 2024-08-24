package com.vigilonix.jaanch.model;

import com.vigilonix.jaanch.pojo.OdApplicationStatus;
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
    @ManyToOne
    private User enquiryOfficer;
    @Column
    private String enquiryFilePath;
    @Column
    private Long enquirySubmittedAt;
    @Column
    private OdApplicationStatus status;
    @Column
    private String receiptNo;
    @Column
    private Integer receiptBucketNumber;
    @Column
    private Long createdAt;
    @Column
    private Long modifiedAt;
}
