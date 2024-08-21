package com.vigilonix.jaanch.pojo;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class WhatsappDirectSendRequest implements INotificationRequest {
    private final String recipientType;
    private final String to;
    private final String type;
    private final WhatsappInteractive interactive;
}
