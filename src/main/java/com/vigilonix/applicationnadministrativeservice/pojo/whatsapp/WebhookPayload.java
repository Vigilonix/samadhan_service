package com.vigilonix.applicationnadministrativeservice.pojo.whatsapp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookPayload {
    private List<Entry> entry;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        private String id;
        private List<Change> changes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        private Value value;
        private String field;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        private String messaging_product;
        private Metadata metadata;
        private List<Contact> contacts;
        private List<Message> messages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        private String display_phone_number;
        private String phone_number_id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        private Profile profile;
        private String wa_id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String from;
        private String id;
        private String timestamp;
        private String type;
        private Text text;
        private Interactive interactive;
        private Context context;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Text {
        private String body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Interactive {
        private String type;
        private NfmReply nfm_reply;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NfmReply {
        private String response_json; // Change this from String to Map
        private String body;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Context {
        private String from;
        private String id;
    }
}
