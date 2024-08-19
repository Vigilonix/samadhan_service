package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.enums.NotificationMethod;
import com.vigilonix.jaanch.pojo.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationWorkerFactory {
    private final Map<NotificationMethod, INotificationWorker> notificationWorkerMap;

    public boolean notify(NotificationPayload notificationPayload) {
        for(NotificationMethod notificationMethod : notificationPayload.getNotificationMethod()) {
            if(notificationWorkerMap.containsKey(notificationMethod)) {
                if(notificationWorkerMap.get(notificationMethod).notify(notificationPayload))
                    return true;
            }
        }
        return false;
    }
}
