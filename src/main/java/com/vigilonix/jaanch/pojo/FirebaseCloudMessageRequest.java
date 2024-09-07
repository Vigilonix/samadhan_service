package com.vigilonix.jaanch.pojo;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class FirebaseCloudMessageRequest implements INotificationRequest{
    private final String to;
    private final String title;
    private final Map<String, String> data;
}
