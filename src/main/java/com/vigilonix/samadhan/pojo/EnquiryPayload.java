package com.vigilonix.samadhan.pojo;

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
    private Long modifiedAt;
    private UUID userUuid;
    private String enquiryOfficeName;
    private String comment;
    private OdApplicationStatus Status;
}
