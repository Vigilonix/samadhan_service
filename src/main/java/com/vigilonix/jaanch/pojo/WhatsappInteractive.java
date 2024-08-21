package com.vigilonix.jaanch.pojo;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class WhatsappInteractive {
        private final String type;
        private final WhatsappBody body;
        private final WhatsappFooter footer;
        private final WhatsappAction action;
}
