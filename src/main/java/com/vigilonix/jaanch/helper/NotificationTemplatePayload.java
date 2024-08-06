package com.vigilonix.jaanch.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class NotificationTemplatePayload {
    private final List<String> values;
    private final String templateId;
    private final String phoneNumber;
}
