package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.ODApplicationStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class TemplateTransformerWorker {
    Map<ODApplicationStatus, NotificationTemplateTransformer> templateTransformerMap;
    public Optional<NotificationTemplatePayload> getTemplatePayload(OdApplication odApplication) {
        if(templateTransformerMap.containsKey(odApplication.getStatus())) {
            return Optional.ofNullable(templateTransformerMap.get(odApplication.getStatus()).transform(odApplication));
        }
        return Optional.empty();
    }
}
