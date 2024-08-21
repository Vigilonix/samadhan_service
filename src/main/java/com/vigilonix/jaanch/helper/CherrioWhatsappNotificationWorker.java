package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.pojo.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CherrioWhatsappNotificationWorker implements INotificationWorker {
    @Override
    public boolean work(NotificationPayload notificationPayload) {
        log.debug("received work {}", notificationPayload);
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
