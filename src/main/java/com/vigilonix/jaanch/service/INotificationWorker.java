package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.helper.NotificationTemplatePayload;

public interface INotificationWorker {
    void notify(NotificationTemplatePayload notificationTemplatePayload);
}
