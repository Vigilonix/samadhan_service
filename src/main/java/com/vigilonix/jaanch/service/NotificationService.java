package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.pojo.NotificationPayload;
import com.vigilonix.jaanch.helper.NotificationPayloadTransformerFactory;
import com.vigilonix.jaanch.helper.NotificationWorkerFactory;
import com.vigilonix.jaanch.model.OdApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {
    private final NotificationPayloadTransformerFactory notificationPayloadTransformerFactory;
    private final NotificationWorkerFactory notificationWorkerFactory;

    public boolean sendNotification(OdApplication odApplication) {
        log.debug("going to send notification for {}", odApplication);
        List<NotificationPayload> notificationPayloads = notificationPayloadTransformerFactory.transform(odApplication);
        log.debug("transformed payload {} for odApplication {}", notificationPayloads, odApplication);
        for(NotificationPayload notificationPayload: notificationPayloads){
            if(notificationWorkerFactory.notify(notificationPayload)) return true;
        }
        return false;
    }
}
