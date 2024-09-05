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
//            if (geoHierarchyService.isTestNode(odApplication.getGeoHierarchyNodeUuid())) {
//                log.info("Test geonode, skipping notification for {}", odApplication);
//                return;  // Corrected from 'return false;' to 'return;' since it's a void method
//            }
            try {
                List<NotificationPayload> notificationPayloads = notificationPayloadTransformerFactory.transform(odApplication);
                log.debug("Transformed payload {} for odApplication {}", notificationPayloads, odApplication);
                for (NotificationPayload notificationPayload : notificationPayloads) {
                    boolean success = notificationWorkerFactory.notify(notificationPayload).isSuccess();
                    if (!success) {
                        log.error("Failed to notify for payload {}", notificationPayload);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to notify {}", odApplication, e);
            }
        });
    }
}
