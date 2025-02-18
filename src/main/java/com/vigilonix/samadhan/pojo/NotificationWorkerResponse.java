package com.vigilonix.samadhan.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Builder
@ToString
public class NotificationWorkerResponse {
    private final boolean success;
    private final String response;
}
