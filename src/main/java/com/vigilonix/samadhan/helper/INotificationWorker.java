package com.vigilonix.samadhan.helper;

import com.vigilonix.samadhan.pojo.NotificationPayload;
import com.vigilonix.samadhan.pojo.NotificationWorkerResponse;

public interface INotificationWorker {
    NotificationWorkerResponse work(NotificationPayload notificationPayload);
    int getPriority();
}
