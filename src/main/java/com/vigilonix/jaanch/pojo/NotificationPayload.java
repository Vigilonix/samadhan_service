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
    private final INotificationRequest request;
    private final NotificationMethod notificationMethod;
}
