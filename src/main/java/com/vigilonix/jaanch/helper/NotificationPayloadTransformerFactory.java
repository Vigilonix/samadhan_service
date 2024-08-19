package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.NotificationPayload;
import com.vigilonix.jaanch.pojo.OdApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationPayloadTransformerFactory implements Transformer<OdApplication, Optional<NotificationPayload>> {
    private final Map<OdApplicationStatus, Transformer<OdApplication, NotificationPayload>> templateTransformerMap;

    @Override
    public Optional<NotificationPayload> transform(OdApplication odApplication) {
        if(templateTransformerMap.containsKey(odApplication.getStatus())) {
            return Optional.ofNullable(templateTransformerMap.get(odApplication.getStatus()).transform(odApplication));
        }
        return Optional.empty();
    }

}
