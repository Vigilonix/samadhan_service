package com.vigilonix.jaanch.pojo;

import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class WhatsappAction {
        private final List<WhatsappButton> buttons;
}
