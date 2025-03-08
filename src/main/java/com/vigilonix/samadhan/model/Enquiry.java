package com.vigilonix.samadhan.model;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
@Setter
@NoArgsConstructor
@ToString
public class Enquiry {
    private String path;
    private Long createdAt;
    private UUID ownerUuid;
    private String comment;
}
