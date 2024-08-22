package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsappParameter {
    private String type;
    private WhatsappImage image;
    private String text;
}