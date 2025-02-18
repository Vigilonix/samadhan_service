package com.vigilonix.samadhan.exception;

import com.vigilonix.samadhan.enums.ValidationError;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
public class ValidationRuntimeException extends RuntimeException {
    @Getter
    private final List<ValidationError> errors;

    public ValidationRuntimeException(Throwable cause, List<ValidationError> errors) {
        super(cause);
        this.errors = errors;
    }
}
