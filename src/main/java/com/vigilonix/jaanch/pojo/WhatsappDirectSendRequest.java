package com.vigilonix.jaanch.pojo;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WhatsappDirectSendRequest implements INotificationRequest {
    private final String recipientType;
    private final String to;
    private final String type;
    private final WhatsappInteractive interactive;
}
