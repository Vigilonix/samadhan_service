package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.pojo.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WhatsappNotificationWorker implements INotificationWorker {
    @Override
    public boolean notify(NotificationPayload notificationPayload) {
        return false;
    }
}
