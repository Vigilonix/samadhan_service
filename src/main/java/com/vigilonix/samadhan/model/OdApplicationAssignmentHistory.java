package com.vigilonix.samadhan.model;

import com.vigilonix.samadhan.pojo.OdApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table
public class OdApplicationAssignmentHistory {
    @Id
    @Column
    private UUID uuid;

    @Column
    private UUID assignmentUuid;

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
}
