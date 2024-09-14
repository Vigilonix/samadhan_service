package com.vigilonix.jaanch.service;

import com.vigilonix.jaanch.aop.Timed;
import com.vigilonix.jaanch.enums.NotificationMethod;
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
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationService {
    private final NotificationPayloadTransformerFactory notificationPayloadTransformerFactory;
    private final NotificationWorkerFactory notificationWorkerFactory;
    private final GeoHierarchyService geoHierarchyService;
    private final ThreadPoolExecutor threadPoolExecutor;

    @Timed
    public void sendNotification(OdApplication odApplication) {
        threadPoolExecutor.submit(() -> {
            log.debug("Going to send notification for {}", odApplication);
            try {
                List<NotificationPayload> notificationPayloads = notificationPayloadTransformerFactory.transform(odApplication);
                log.info("Transformed payload {} for odApplication {}", notificationPayloads, odApplication);
                for (NotificationPayload notificationPayload : notificationPayloads) {
                    try {
                        if(NotificationMethod.WHATSAPP_TEMPLATE.equals(notificationPayload.getNotificationMethod()) && geoHierarchyService.isTestNode(odApplication.getGeoHierarchyNodeUuid())) {
                            log.info("Test geonode, skipping notification for {}", odApplication);
                            continue;
                        }
                        boolean success = notificationWorkerFactory.notify(notificationPayload).isSuccess();
                        if (!success) {
                            log.error("Failed to notify for payload {}", notificationPayload);
                        }
                        log.error("notify response for payload {} is {} ", notificationPayload, success);
                    }catch (RuntimeException e) {
                        log.error("Failed to notify using notification payload {}", notificationPayload, e);
                    }
                }
            } catch (RuntimeException e) {
                log.error("Failed to notify while creating payload {}", odApplication, e);
            }
        });
    }
}
