package com.vigilonix.samadhan.model;

import com.vigilonix.samadhan.pojo.OdApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "OdApplicationAssignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "od_application_assignment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"application_uuid", "enquiry_officer_uuid"}, name = "UC_application_enquiry_officer")
})
public class OdApplicationAssignment {
    @Id
    @Column
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    private OdApplication application;

    @Column
    private String filePath;

    @ManyToOne
    private User enquiryOfficer;

    @Column
    @Enumerated(EnumType.STRING)
    private OdApplicationStatus status;

    @Column
    private Long createdAt;

    @Column
    private Long modifiedAt;

    @Column
    private String comment;

    @Column
    private UUID geoHierarchyNodeUuid;

    @ManyToOne
    private User actor;
}
