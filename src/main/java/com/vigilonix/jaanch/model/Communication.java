package com.vigilonix.jaanch.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

//@Entity
//@Getter
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
public class Communication {
    private String entryUuid;
    private UUID userUuid;
    private String phoneNumber;
    private String messageType;
    private String context;
    private String surveyId;
    private String responseType;
    private String responseData;
    private Long receivedAt;
    private String createdAt;
    private String channel;
}
