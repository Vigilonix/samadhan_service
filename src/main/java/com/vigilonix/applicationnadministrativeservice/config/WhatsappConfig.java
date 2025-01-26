package com.vigilonix.applicationnadministrativeservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class WhatsappConfig {
    @Value("${cherrio.apiKey}")
    private String cherrioApiKey;
}
