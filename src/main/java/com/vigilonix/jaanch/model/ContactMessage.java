package com.vigilonix.jaanch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class ContactMessage implements Serializable {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column
    private String entryId;

    @Column
    private String contactId;

    @Column
    private String contactName;

    @Column
    private String contactWaId;

    @Column
    private String contactPhoneNumber;

    @Column
    private String contactEmail;

    @Column
    private String channelType;

    @Column
    private String messageId;

    @Column
    private Timestamp messageTimestamp;

    @Column
    private String messageBody;

    @Column
    private String messageType;

    @Column
    private String interactiveType;

    @Column(columnDefinition = "jsonb")
    private String interactiveResponseJson;

    @Column
    private String metadataDisplayPhoneNumber;

    @Column
    private String metadataPhoneNumberId;

    @Column
    private String contextMessageId;

    @Column
    private String contextFrom;
}
