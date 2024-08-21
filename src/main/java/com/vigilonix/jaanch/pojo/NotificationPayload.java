package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.enums.NotificationMethod;
import com.vigilonix.jaanch.enums.NotificationTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class NotificationPayload {
    private final String toEmail;
    private final String fromEmail;
    private final String header;
    private final String footer;
    private final String payload;
    private final String toPhoneNumber;
    private final String toPhoneCountryCode;
    private final String fromPhoneNumber;
    private final String fromPhoneCountryCode;
    private final NotificationMethod notificationMethod;
}
