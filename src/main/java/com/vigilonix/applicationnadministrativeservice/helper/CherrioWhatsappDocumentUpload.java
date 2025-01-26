package com.vigilonix.applicationnadministrativeservice.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigilonix.applicationnadministrativeservice.config.WhatsappConfig;
import com.vigilonix.applicationnadministrativeservice.pojo.NotificationPayload;
import com.vigilonix.applicationnadministrativeservice.pojo.NotificationWorkerResponse;
import com.vigilonix.applicationnadministrativeservice.pojo.whatsapp.WhatsappFileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CherrioWhatsappDocumentUpload implements INotificationWorker {
    private final WhatsappConfig whatsappConfig;
    private final ObjectMapper objectMapper;

    private static final String API_URL = "https://pre-prod.cheerio.in/direct-apis/v1/whatsapp/media-id"; // Replace with actual URL

    @Override
    public NotificationWorkerResponse work(NotificationPayload notificationPayload) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost uploadFile = new HttpPost(API_URL);

            // Add API Key as a header
            uploadFile.setHeader("x-api-key", whatsappConfig.getCherrioApiKey());

            // Build multipart entity with file
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", notificationPayload.getAttachments().get(0));

            HttpEntity multipart = builder.build();
            uploadFile.setEntity(multipart);

            // Execute the request
            CloseableHttpResponse response = httpClient.execute(uploadFile);
            HttpEntity responseEntity = response.getEntity();
            String responseString = EntityUtils.toString(responseEntity);
            WhatsappFileUploadResponse fileUploadResponse= objectMapper.readValue(responseString, WhatsappFileUploadResponse.class);
            return NotificationWorkerResponse.builder().success(true).response(fileUploadResponse.getData().getId()).build();
            // Print response
        } catch (RuntimeException | IOException | ParseException e) {
            log.error("Failed to upload pdf {}", notificationPayload, e);
            return NotificationWorkerResponse.builder().success(false).build();
        }
    }


    @Override
    public int getPriority() {
        return 0;
    }
}
