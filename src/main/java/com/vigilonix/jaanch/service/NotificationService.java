package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.pojo.NotificationPayload;
import com.vigilonix.jaanch.helper.NotificationPayloadTransformerFactory;
import com.vigilonix.jaanch.helper.NotificationWorkerFactory;
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
    private final NotificationPayloadTransformerFactory notificationPayloadTransformerFactory;
    private final NotificationWorkerFactory notificationWorkerFactory;

    void sendNotification(OdApplication odApplication) {
        Optional<NotificationPayload> notificationPayload = notificationPayloadTransformerFactory.transform(odApplication);
        if(notificationPayload.isPresent()) {
            notificationWorkerFactory.notify(notificationPayload.get());
        }
    }
}
