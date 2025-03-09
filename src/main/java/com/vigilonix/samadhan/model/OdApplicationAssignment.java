package com.vigilonix.samadhan.model;

import com.vigilonix.samadhan.enums.ApplicationCategory;
import com.vigilonix.samadhan.pojo.OdApplicationStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;
import java.util.UUID;

@Entity(name = "OdApplicationAssignment")
@Table(
        name = "od_application_assignment"
)
@Getter
@AllArgsConstructor
@Builder
@Setter
@NoArgsConstructor
@ToString
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
}
