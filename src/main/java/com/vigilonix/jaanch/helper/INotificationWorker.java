package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.pojo.NotificationPayload;

public interface INotificationWorker {
    boolean work(NotificationPayload notificationPayload);
    int getPriority();
}
