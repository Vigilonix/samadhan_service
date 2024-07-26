package com.vigilonix.jaanch.validator;


import com.vigilonix.jaanch.enums.ValidationError;
import com.vigilonix.jaanch.exception.ValidationRuntimeException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface ValidationService<T> {
    List<Validator<List<ValidationError>, T>> getValidators();

    default void validate(T t) {
        List<ValidationError> validatorResponse = getValidators().stream()
                .map(v -> v.validate(t))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (Collections.isNotEmpty(validatorResponse)) {
            throw new ValidationRuntimeException(validatorResponse);
        }
    }
}
