package com.vigilonix.applicationnadministrativeservice.pojo;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
@Setter
@NoArgsConstructor
@ToString
public class EnquiryPayload {
    private String path;
    private Long createdAt;
    private UUID ownerUuid;
    private String enquiryOfficeName;
}
