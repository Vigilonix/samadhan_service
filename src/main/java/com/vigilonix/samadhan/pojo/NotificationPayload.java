package com.vigilonix.samadhan.pojo;

import com.vigilonix.samadhan.enums.NotificationMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.File;
import java.util.List;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class NotificationPayload {
    private final INotificationRequest request;
    private final NotificationMethod notificationMethod;
    private final List<File> attachments;
}
