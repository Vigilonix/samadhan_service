package com.vigilonix.samadhan.helper;

import com.vigilonix.samadhan.enums.NotificationMethod;
import com.vigilonix.samadhan.pojo.NotificationPayload;
import com.vigilonix.samadhan.pojo.NotificationWorkerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class NotificationWorkerFactory {
    private final Map<NotificationMethod, NavigableSet<INotificationWorker>> notificationWorkerMap;

    public NotificationWorkerResponse notify(NotificationPayload notificationPayload) {
        log.debug("payload received to notify {}", notificationPayload);
        for (INotificationWorker notificationWorker :notificationWorkerMap.getOrDefault(notificationPayload.getNotificationMethod(), new TreeSet<>())) {
            try {
                log.info("notification worker {} for payload {}", notificationWorker, notificationPayload);
                return  notificationWorker.work(notificationPayload);
            }catch (RuntimeException e) {
                log.error("failed to notify for payload {}", notificationPayload, e);
            }
        }
        return NotificationWorkerResponse.builder().success(false).build();
    }
}
