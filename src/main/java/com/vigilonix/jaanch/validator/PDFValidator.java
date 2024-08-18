package com.vigilonix.jaanch.validator;

import com.google.common.net.HttpHeaders;
import com.vigilonix.jaanch.enums.ValidationError;
import com.vigilonix.jaanch.enums.ValidationErrorEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PDFValidator implements Validator<List<ValidationError>, String> {

    @Override
    public List<ValidationError> validate(String url) {
        if (StringUtils.isEmpty(url) || !url.toLowerCase().endsWith(".pdf") || url.length() > 100) {
            return Collections.singletonList(ValidationErrorEnum.INVALID_MEDIA_URI);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHead request = new HttpHead(url); // Send a HEAD request to get headers only
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
                if(contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                    return Collections.singletonList(ValidationErrorEnum.INVALID_MEDIA_URI);
                }
            }
        } catch (Exception e) {
            log.error("failed to fetch head for pdf file path {} validation", url, e);
            return Collections.singletonList(ValidationErrorEnum.INVALID_MEDIA_URI);
        }
        return Collections.emptyList();
    }
}