package com.vigilonix.jaanch.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public enum NotificationTemplate {
    OD_APPLICATION_CREATED("", Arrays.asList(NotificationMethod.MSG, NotificationMethod.WHATSAPP));
    private final String template;
    private final List<NotificationMethod> notficationMethods;
}
