package com.vigilonix.applicationnadministrativeservice.helper;

import com.vigilonix.applicationnadministrativeservice.pojo.NotificationPayload;
import com.vigilonix.applicationnadministrativeservice.pojo.NotificationWorkerResponse;

public interface INotificationWorker {
    NotificationWorkerResponse work(NotificationPayload notificationPayload);
    int getPriority();
}
