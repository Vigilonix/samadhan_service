package com.vigilonix.jaanch.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vigilonix.jaanch.config.Constant;
import com.vigilonix.jaanch.config.WhatsappConfig;
import com.vigilonix.jaanch.pojo.INotificationRequest;
import com.vigilonix.jaanch.pojo.NotificationPayload;
import com.vigilonix.jaanch.pojo.WhatsappDirectSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CherrioWhatsappNotificationWorker implements INotificationWorker {
    private final WhatsappConfig whatsappConfig;

    private static final String API_URL = "https://pre-prod.cheerio.in/direct-apis/v1/whatsapp/template/send"; // Replace with actual URL

    @Override
    public boolean work(NotificationPayload notificationPayload) {
        log.debug("received work {}", notificationPayload);

        // Extract WhatsappDirectSendRequest from NotificationPayload

        try {
            INotificationRequest requestPayload = notificationPayload.getRequest();
            // Convert the request object to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(requestPayload);

            // Create the HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Create the HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", whatsappConfig.getCherrioApiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Log the response
            log.info("Response code: {}", response.statusCode());
            log.info("Response body: {}", response.body());
            // Return true if the response is successful (status code 200-299)
            return response.statusCode() >= 200 && response.statusCode() < 300;

        } catch (Exception e) {
            log.error("Failed to send WhatsApp notification", e);
            return false;
        }
    }


    @Override
    public int getPriority() {
        return 0;
    }
}
