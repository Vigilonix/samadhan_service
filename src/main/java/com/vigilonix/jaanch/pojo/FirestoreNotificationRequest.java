package com.vigilonix.jaanch.pojo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@Getter
@Builder
@ToString
public class FirestoreNotificationRequest implements INotificationRequest{
    private final String to;
    private final Map<String, Object> dataMap;
}
