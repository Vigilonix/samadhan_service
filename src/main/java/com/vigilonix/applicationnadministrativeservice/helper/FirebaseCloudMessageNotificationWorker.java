package com.vigilonix.applicationnadministrativeservice.helper;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.vigilonix.applicationnadministrativeservice.pojo.FirebaseCloudMessageRequest;
import com.vigilonix.applicationnadministrativeservice.pojo.NotificationPayload;
import com.vigilonix.applicationnadministrativeservice.pojo.NotificationWorkerResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FirebaseCloudMessageNotificationWorker implements INotificationWorker{

    @Override
    public NotificationWorkerResponse work(NotificationPayload notificationPayload) {
        log.debug("sending cloud message {}", notificationPayload);
        if(!(notificationPayload.getRequest() instanceof FirebaseCloudMessageRequest firebaseCloudMessageRequest) || StringUtils.isEmpty(firebaseCloudMessageRequest.getTo())) {
            log.info("skipping notification {}", notificationPayload);
            return NotificationWorkerResponse.builder().success(false).build();
        }
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(firebaseCloudMessageRequest.getTitle())
                        .setBody(firebaseCloudMessageRequest.getBody())
                        .build())
                .setToken(firebaseCloudMessageRequest.getTo())
                .putAllData(firebaseCloudMessageRequest.getData())
                .build();
        try {
            FirebaseMessaging.getInstance().send(message);
            log.info("successfully sent cloud message {}", message);
            return NotificationWorkerResponse.builder().success(true).build();
        } catch (FirebaseMessagingException e) {
            log.error("failed to send message {} with errorCode {}", notificationPayload, e);
        }
        return NotificationWorkerResponse.builder().success(false).build();
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
