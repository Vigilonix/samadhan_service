package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.aop.Timed;
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

@Service
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {
    private final NotificationPayloadTransformerFactory notificationPayloadTransformerFactory;
    private final NotificationWorkerFactory notificationWorkerFactory;
    private final GeoHierarchyService geoHierarchyService;

    @Timed
    public boolean sendNotification(OdApplication odApplication) {
        log.debug("going to send notification for {}", odApplication);
        if (geoHierarchyService.isTestNode(odApplication.getGeoHierarchyNodeUuid())) {
            log.info("test geonode skipping notification {}", odApplication);
            return false;
        }
        try {
            List<NotificationPayload> notificationPayloads = notificationPayloadTransformerFactory.transform(odApplication);
            log.debug("transformed payload {} for odApplication {}", notificationPayloads, odApplication);
            for (NotificationPayload notificationPayload : notificationPayloads) {
                return notificationWorkerFactory.notify(notificationPayload).isSuccess();
            }
            return false;
        }catch (Exception e) {
            log.error("failed to notify {}", odApplication, e);
        }
        return false;
    }
}
