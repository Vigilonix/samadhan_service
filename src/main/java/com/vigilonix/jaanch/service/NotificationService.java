package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.helper.NotificationTemplatePayload;
import com.vigilonix.jaanch.helper.TemplateTransformerWorker;
import com.vigilonix.jaanch.model.OdApplication;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {
    private final TemplateTransformerWorker templateTransformerWorker;
    private final GupshupNotificationWorker notificationWorker;

    void sendNotification(OdApplication odApplication) {
        Optional<NotificationTemplatePayload> notificationPayload = templateTransformerWorker.getTemplatePayload(odApplication);
        if(notificationPayload.isPresent()) {
            notificationWorker.notify(notificationPayload.get());
        }
    }

}
