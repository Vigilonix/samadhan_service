package com.vigilonix.samadhan.pojo.whatsapp;

import lombok.Data;

import java.util.List;

@Data
public class WhatsAppWebhookPayload {
    private String object;
    private List<WbaEntry> entry;
}