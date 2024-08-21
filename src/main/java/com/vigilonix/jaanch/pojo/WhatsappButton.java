package com.vigilonix.jaanch.pojo;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class WhatsappButton {
        private final String type;
        private final WhatsappReply reply;
}
