package com.vigilonix.jaanch.model;

import com.vigilonix.jaanch.pojo.ODApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "od_application")
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
    @Column
    private UUID odUuid;
    @Column
    private String applicationFilePath;
    @Column
    private UUID fieldGeoNodeUuid;
    @Column
    private UUID enquiryOfficerUuid;
    @Column
    private String enquiryFilePath;
    @Column
    private ODApplicationStatus status;
    @Column
    private String receiptNo;
    @Column
    private Long createdAt;
    @Column
    private Long modifiedAt;
}
