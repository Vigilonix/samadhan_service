package com.vigilonix.jaanch.transformer;

import com.vigilonix.jaanch.enums.NotificationMethod;
import com.vigilonix.jaanch.enums.NotificationTemplate;
import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.NotificationPayload;
import com.vigilonix.jaanch.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicantApplicationCreationNotificationTransformer implements Transformer<OdApplication, List<NotificationPayload>> {
    private final GeoHierarchyService geoHierarchyService;

    @Override
    public List<NotificationPayload> transform(OdApplication odApplication) {
        Map<String, String> params = Map.of("name", odApplication.getApplicantName(),
                "receiptNo", odApplication.getReceiptNo(),
                "odName", odApplication.getOd().getName(),
                "geoName", geoHierarchyService.getNodeById(odApplication.getFieldGeoNodeUuid()).getName(),
                "date", dateFormatterddMMYYY(odApplication.getCreatedAt()));
        StringSubstitutor sub = new StringSubstitutor(params);
        String payload = sub.replace(NotificationTemplate.OD_APPLICATION_CREATED_ENGLISH.getTemplate());
        return Arrays.asList(NotificationPayload.builder()
                        .payload(payload)
                        .fromPhoneCountryCode("+91")
                        .fromPhoneNumber("8986139192")
                        .toPhoneCountryCode("+91")
                        .toPhoneNumber(odApplication.getApplicantPhoneNumber())
                        .notificationMethod(NotificationMethod.WHATSAPP).build(),

                NotificationPayload.builder()
                        .payload(payload)
                        .fromPhoneCountryCode("+91")
                        .fromPhoneNumber("8986139192")
                        .toPhoneCountryCode("+91")
                        .toPhoneNumber(odApplication.getApplicantPhoneNumber())
                        .notificationMethod(NotificationMethod.SMS).build());
    }

    private String dateFormatterddMMYYY(Long epoch) {
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), istZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dateTime.format(formatter);
    }
}
