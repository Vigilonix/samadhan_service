package com.vigilonix.samadhan.validator;

import com.google.common.net.HttpHeaders;
import com.vigilonix.samadhan.enums.ValidationError;
import com.vigilonix.samadhan.enums.ValidationErrorEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class PDFValidator implements Validator<List<ValidationError>, String> {

    @Override
    public List<ValidationError> validate(String path) {
        if (StringUtils.isEmpty(path) || path.length() > 255) {
            return Collections.singletonList(ValidationErrorEnum.INVALID_MEDIA_URI);
        }
        try {
            URL url = new URL(path);
            if(!url.getPath().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                return Collections.singletonList(ValidationErrorEnum.INVALID_MEDIA_URI);
            }
        } catch (MalformedURLException e) {
            log.error("invalid url path {}", path, e);
            return Collections.singletonList(ValidationErrorEnum.INVALID_MEDIA_URI);
        }

//        List<ValidationError> INVALID_MEDIA_URI = validateContentType(path);
//        if (INVALID_MEDIA_URI != null) return INVALID_MEDIA_URI;
        return Collections.emptyList();
    }

    private List<ValidationError> validateContentType(String path) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpHead request = new HttpHead(path); // Send a HEAD request to get headers only
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
                if(contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                    return Collections.singletonList(ValidationErrorEnum.INVALID_MEDIA_URI);
                }
            }
        } catch (Exception e) {
            log.error("failed to fetch head for pdf file path {} validation", path, e);
            return Collections.singletonList(ValidationErrorEnum.INVALID_MEDIA_URI);
        }
        return null;
    }
}