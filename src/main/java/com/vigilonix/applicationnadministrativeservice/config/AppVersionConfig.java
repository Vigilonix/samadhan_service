package com.vigilonix.applicationnadministrativeservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AppVersionConfig {
    @Value("${app.version.ios}")
    private String iosVersion;
    @Value("${app.version.android}")
    private String androidVersion;
    @Value("${app.version.code.android}")
    private Integer androidVersionCode;
    @Value("${app.version.code.ios}")
    private Integer iosVersionCode;
}
