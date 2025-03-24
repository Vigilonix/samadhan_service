package com.vigilonix.samadhan.model;

import com.vigilonix.samadhan.enums.ActorType;
import com.vigilonix.samadhan.enums.OdApplicationStatus;
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

    @Column
    private ActorType actorType;
}
