package com.vigilonix.jaanch.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WhatsappComponent {
    private String type;
    private List<WhatsappParameter> parameters;
}