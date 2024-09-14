package com.vigilonix.jaanch.transformer;

import com.vigilonix.jaanch.enums.NotificationMethod;
import com.vigilonix.jaanch.enums.NotificationTemplate;
import com.vigilonix.jaanch.helper.CherrioWhatsappDocumentUpload;
import com.vigilonix.jaanch.model.OdApplication;
import com.vigilonix.jaanch.pojo.*;
import com.vigilonix.jaanch.pojo.whatsapp.*;
import com.vigilonix.jaanch.service.GeoHierarchyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.text.StringSubstitutor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ApplicantApplicationClosedWhatasappDocumentTemplateTransformer implements Transformer<OdApplication, NotificationPayload> {
    private final GeoHierarchyService geoHierarchyService;
    private final CherrioWhatsappDocumentUpload cherrioWhatsappDocumentUpload;

    @Override
    public NotificationPayload transform(OdApplication odApplication) {
        Map<String, String> params = Map.of("name", odApplication.getApplicantName(),
                "receiptNo", odApplication.getReceiptNo(),
                "odName", odApplication.getOd().getName(),
                "geoName", geoHierarchyService.getNodeById(odApplication.getGeoHierarchyNodeUuid()).getName(),
                "date", dateFormatterddMMYYY(odApplication.getCreatedAt()));
        StringSubstitutor sub = new StringSubstitutor(params);


        String body = sub.replace(NotificationTemplate.OD_APPLICATION_CREATED_ENGLISH.getTemplate());

        WhatsappMessageRequest sendRequest = WhatsappMessageRequest.builder()
                .to("91"+odApplication.getApplicantPhoneNumber())
//                .type("template")
                .data(WhatsappTemplate.builder()
                        .name("notifiy_applicant_application_closed")
                        .language(Language.builder()
                                .code("en")
                                .build())
                        .components(Arrays.asList(
                                WhatsappComponent.builder().type("header").parameters(Arrays.asList(WhatsappParameter.builder()
                                        .type("document")
                                        .document(WhatsappDocument.builder().link(odApplication.getEnquiryFilePath()).filename(odApplication.getReceiptNo()+"_enquiry_report.pdf").build())
                                        .build())).build(),
                                WhatsappComponent.builder()
                                        .type("body")
                                        .parameters(Arrays.asList(
                                                WhatsappParameter.builder()
                                                        .type("text")
                                                        .text(odApplication.getApplicantName())
                                                        .build(),
                                                WhatsappParameter.builder()
                                                        .type("text")
                                                        .text(odApplication.getReceiptNo())
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build())
                .build();

        log.info("wba closed transformer {}", sendRequest);
        return NotificationPayload.builder()
                .request(sendRequest)
                .notificationMethod(NotificationMethod.WHATSAPP_TEMPLATE).build();

    }

    private String dateFormatterddMMYYY(Long epoch) {
        ZoneId istZone = ZoneId.of("Asia/Kolkata");
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), istZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return dateTime.format(formatter);
    }

    public static File downloadFile(String fileUrl) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Define the request configuration
            RequestConfig requestConfig = RequestConfig.custom()
                    .setResponseTimeout(Timeout.ofMinutes(5))
                    .build();

            // Create HttpGet request
            HttpGet httpGet = new HttpGet(fileUrl);
            httpGet.setConfig(requestConfig);

            // Define the response handler
            HttpClientResponseHandler<File> responseHandler = response -> {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // Create a file object to save the downloaded file
                    File file =  File.createTempFile(UUID.randomUUID().toString(), ".pdf");

                    try (InputStream inputStream = entity.getContent();
                         FileOutputStream fileOutputStream = new FileOutputStream(file)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                        }
                        return file;
                    }
                } else {
                    throw new IOException("No content to download");
                }
            };

            // Execute the request with the response handler
            return httpClient.execute(httpGet, responseHandler);
        }catch (IOException e) {
            log.error("error while downloading file {}", fileUrl, e);
            throw new RuntimeException("failed to download file", e);
        }
    }
}
