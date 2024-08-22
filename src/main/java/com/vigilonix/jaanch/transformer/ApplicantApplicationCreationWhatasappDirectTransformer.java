package com.vigilonix.jaanch.transformer;

import com.vigilonix.jaanch.enums.NotificationMethod;
import com.vigilonix.jaanch.enums.NotificationTemplate;
import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.*;
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
public class ApplicantApplicationCreationWhatasappDirectTransformer implements Transformer<OdApplication, List<NotificationPayload>> {
    private final GeoHierarchyService geoHierarchyService;

    @Override
    public List<NotificationPayload> transform(OdApplication odApplication) {
        Map<String, String> params = Map.of("name", odApplication.getApplicantName(),
                "receiptNo", odApplication.getReceiptNo(),
                "odName", odApplication.getOd().getName(),
                "geoName", geoHierarchyService.getNodeById(odApplication.getFieldGeoNodeUuid()).getName(),
                "date", dateFormatterddMMYYY(odApplication.getCreatedAt()));
        StringSubstitutor sub = new StringSubstitutor(params);
        String body = sub.replace(NotificationTemplate.OD_APPLICATION_CREATED_ENGLISH.getTemplate());


        WhatsappDirectSendRequest requestPayload = WhatsappDirectSendRequest.builder()
                .recipientType("individual")
                .to("91"+odApplication.getApplicantPhoneNumber())
                .type("interactive")
                .interactive(
                        WhatsappInteractive.builder()
                                .type("button")
                                .body(WhatsappBody.builder().text(body).build())
                                .footer(WhatsappFooter.builder().text("your-text-footer-content").build())
                                .action(
                                        WhatsappAction.builder()
                                                .buttons(List.of(
                                                        WhatsappButton.builder()
                                                                .type("reply")
                                                                .reply(WhatsappReply.builder()
                                                                        .id("unique-postback-id")
                                                                        .title("First Button")
                                                                        .build())
                                                                .build(),
                                                        WhatsappButton.builder()
                                                                .type("reply")
                                                                .reply(WhatsappReply.builder()
                                                                        .id("unique-postback-id1")
                                                                        .title("Second Button")
                                                                        .build())
                                                                .build()
                                                ))
                                                .build()
                                )
                                .build()
                )
                .build();


        return Arrays.asList(NotificationPayload.builder()
                .request(requestPayload)
                .notificationMethod(NotificationMethod.WHATSAPP_DIRECT).build());

    }

    private String dateFormatterddMMYYY(Long epoch) {
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), istZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dateTime.format(formatter);
    }
}
