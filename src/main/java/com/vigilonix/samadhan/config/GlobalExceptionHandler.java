package com.vigilonix.samadhan.config;

import com.vigilonix.samadhan.enums.ValidationError;
import com.vigilonix.samadhan.enums.ValidationErrorEnum;
import com.vigilonix.samadhan.exception.AuthProviderException;
import com.vigilonix.samadhan.exception.ValidationRuntimeException;
import com.vigilonix.samadhan.helper.AuthHelper;
import com.vigilonix.samadhan.pojo.CustomValidationError;
import com.vigilonix.samadhan.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    public static final String VALIDATION_FAILED = "validation failed {}";
    public static final String INVALID_REQUEST = "invalid request";
    @Autowired
    AuthHelper authHelper;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private AuditService auditService;

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {AuthProviderException.class})
    public ResponseEntity<List<ValidationError>> handleConflict(RuntimeException ex) {
        log.error(INVALID_REQUEST, ex);
        return new ResponseEntity<>(Collections.singletonList(ValidationErrorEnum.INVALID_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {DataIntegrityViolationException.class})
    public ResponseEntity<List<ValidationError>> handleDataConstraintViolation(DataIntegrityViolationException ex) {
        log.error(INVALID_REQUEST, ex);
        return new ResponseEntity<>(Collections.singletonList(ValidationErrorEnum.DATA_INTEGRITY_VIOLATION), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {ValidationRuntimeException.class})
    public ResponseEntity<List<ValidationError>> handleDataConstraintViolation(ValidationRuntimeException ex) {
        log.error(VALIDATION_FAILED, ex.getErrors());
        return new ResponseEntity<>(ex.getErrors(), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationError>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        log.error("method argument validation exception", ex);
        List<ValidationError> validationErrors = ex.getBindingResult().getAllErrors().stream()
                .map(e -> CustomValidationError.builder()
                        .code(-1)
                        .messageFormat(e.getDefaultMessage())
                        .attributes(Collections.singletonList(((FieldError) e).getField()))
                        .build())
                .collect(Collectors.toList());
        return new ResponseEntity<>(validationErrors, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {NumberFormatException.class})
    public ResponseEntity<List<ValidationError>> handleDataConstraintViolation(NumberFormatException ex) {
        log.error("number format exception", ex);
        return new ResponseEntity<>(Collections.singletonList(ValidationErrorEnum.NUMBER_FORMAT_EXCEPTION), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<List<ValidationError>> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("number format exception", ex);
        return new ResponseEntity<>(Collections.singletonList(ValidationErrorEnum.INVALID_REQUEST), HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        // Extract and customize the error message as needed
        log.error("invalid parseable input {}", request, ex);
        // Create and return a custom error response
        return new ResponseEntity<>(Collections.singletonList(ValidationErrorEnum.INVALID_REQUEST), HttpStatus.BAD_REQUEST);
    }

}
