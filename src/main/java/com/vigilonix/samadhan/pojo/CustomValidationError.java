package com.vigilonix.samadhan.pojo;

import com.vigilonix.samadhan.enums.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
@AllArgsConstructor
public class CustomValidationError implements ValidationError {
    private final int code;
    private final String messageFormat;
    private final List<String> attributes;
}
