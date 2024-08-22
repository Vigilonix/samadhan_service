package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsappInteractive {
        private final String type;
        private final WhatsappBody body;
        private final WhatsappFooter footer;
        private final WhatsappAction action;
}
