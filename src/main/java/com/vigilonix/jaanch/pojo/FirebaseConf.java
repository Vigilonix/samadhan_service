package com.vigilonix.jaanch.pojo;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class FirebaseConf {
    @Value("${firebase.private.file}")
    private String privateFilePath;
    @Value("${firebase.db.url}")
    private String databaseUrl;
//    @Value("${firebase.web.api.key}")
//    private String webApiKey;
}
