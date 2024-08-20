package com.vigilonix.jaanch.pojo;

import com.vigilonix.jaanch.enums.NotificationMethod;
import com.vigilonix.jaanch.enums.NotificationTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
@Builder
public class NotificationPayload {
    private final List<String> values;
    private final NotificationTemplate template;
    private final String phoneNumber;
    private final List<NotificationMethod> notificationMethod;
}
