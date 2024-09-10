package com.vigilonix.jaanch.pojo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@Builder
@ToString
public class FirebaseCloudMessageRequest implements INotificationRequest{
    private final String to;
    private final String title;
    private final String body;
    private final Map<String, String> data;
}
