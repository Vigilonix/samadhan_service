package com.vigilonix.jaanch.pojo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsappDirectSendRequest implements INotificationRequest {
    private final String recipientType;
    private final String to;
    private final String type;
    private final WhatsappInteractive interactive;
}
