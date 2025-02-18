package com.vigilonix.samadhan.model;

import com.vigilonix.samadhan.pojo.OdApplicationStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;
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
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<Enquiry> enquiries;
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
}
