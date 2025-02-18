package com.vigilonix.samadhan.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum NotificationTemplate {
    OD_APPLICATION_CREATED_ENGLISH(
            """
                    Dear ${name},
                    Your receipt number is ${receiptNo}. This application was received by ${odName} at ${geoName} on ${date}.
                    """, Arrays.asList(NotificationMethod.SMS, NotificationMethod.WHATSAPP_TEMPLATE)),
    OD_APPLICATION_CREATED_HINDI(
            """
                    प्रिय ${name}, 
                    आपकी रसीद संख्या ${receiptNo} है। यह आवेदन ${geoName} में दिनांक ${date} को ${odName} द्वारा प्राप्त किया गया था।"""
            , Arrays.asList(NotificationMethod.SMS, NotificationMethod.WHATSAPP_TEMPLATE));
    private final String template;
    private final List<NotificationMethod> notficationMethods;
}
