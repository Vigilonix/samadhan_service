package com.vigilonix.applicationnadministrativeservice.model;

import com.vigilonix.applicationnadministrativeservice.enums.ChannelType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;


import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

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
    @Enumerated(EnumType.STRING)
    private ChannelType channelType;

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

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String raw_json;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> interactiveResponse;

    @Column
    private String metadataDisplayPhoneNumber;

    @Column
    private String metadataPhoneNumberId;

    @Column
    private String contextMessageId;
    @Column
    private String contextUuid;

    @Column
    private String contextFrom;
}
