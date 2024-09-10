package com.vigilonix.jaanch.helper;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.vigilonix.jaanch.pojo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FirestoreWriter implements INotificationWorker{
    private final Firestore firestore;
    @Override
    public NotificationWorkerResponse work(NotificationPayload notificationPayload) {
        log.info("Updating last_od_application_refresh_epoch for user: {}", notificationPayload);
        if(!(notificationPayload.getRequest() instanceof FirestoreNotificationRequest firestoreNotificationRequest) || StringUtils.isEmpty(firestoreNotificationRequest.getTo())) {
            log.info("skipping notification {}", notificationPayload);
            return NotificationWorkerResponse.builder().success(false).build();
        }

        DocumentReference docRef= firestore.collection("users").document(firestoreNotificationRequest.getTo());

        // Create a map to hold the field to be updated
        Map<String, Object> updates = new HashMap<>(firestoreNotificationRequest.getDataMap());

        // Firestore update operation
        try {
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                // Perform update
                docRef.update(updates).get();
            } else {
                // Create new document
                docRef.set(updates).get();
            }
            return NotificationWorkerResponse.builder().success(true).build();
        }catch (Exception e) {
            throw new RuntimeException("failed to write for payload" + notificationPayload.toString(), e);
        }
    }

    @Override
    public int getPriority() {
        return 5;
    }
}
