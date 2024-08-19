package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.pojo.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationWorkerFactory {
    private final INotificationWorker whatsappNotificationWorker;

    public void notify(NotificationPayload notificationPayload) {
        whatsappNotificationWorker.notify(notificationPayload);
    }
}
