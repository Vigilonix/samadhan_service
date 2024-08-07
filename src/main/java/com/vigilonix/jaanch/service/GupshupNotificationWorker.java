package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.helper.NotificationTemplatePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GupshupNotificationWorker implements INotificationWorker {
    private final NotificationWorker notificationWorker;
    @Override
    public void notify(NotificationTemplatePayload notificationTemplatePayload) {

        notificationWorker.notifu(notificationTemplatePayload);
    }
}
