package com.vigilonix.applicationnadministrativeservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AppConfig {
    @Value("${app.host.uri}")
    private String uri;
    @Value("${app.host.fb.gdpr.delete.uri}")
    private String appDbDeleteGdprDeleteUri;
}
