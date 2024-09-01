package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.pojo.NotificationPayload;
import com.vigilonix.jaanch.pojo.NotificationWorkerResponse;

public interface INotificationWorker {
    NotificationWorkerResponse work(NotificationPayload notificationPayload);
    int getPriority();
}
