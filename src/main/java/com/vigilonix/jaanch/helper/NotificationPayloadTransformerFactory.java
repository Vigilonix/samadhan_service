package com.vigilonix.jaanch.helper;

import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.NotificationPayload;
import com.vigilonix.jaanch.pojo.OdApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NotificationPayloadTransformerFactory implements Transformer<OdApplication, List<NotificationPayload>> {
    private final Map<OdApplicationStatus, List<Transformer<OdApplication, NotificationPayload>>> templateTransformerMap;

    @Override
    public List<NotificationPayload> transform(OdApplication odApplication) {
        return templateTransformerMap.getOrDefault(odApplication.getStatus(), Collections.emptyList()).stream()
                .map(t->t.transform(odApplication))
                .collect(Collectors.toList());
    }

}
