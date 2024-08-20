package com.vigilonix.jaanch.transformer;

import com.vigilonix.jaanch.enums.NotificationMethod;
import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.NotificationPayload;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantApplicationCreationNotificationTransformer implements Transformer<OdApplication, NotificationPayload> {
    @Override
    public NotificationPayload transform(OdApplication input) {
        return NotificationPayload.builder()
                .notificationMethod(Collections.singletonList(NotificationMethod.WHATSAPP))
                .build();
    }
}
