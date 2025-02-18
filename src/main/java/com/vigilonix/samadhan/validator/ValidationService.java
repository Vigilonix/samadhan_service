package com.vigilonix.samadhan.validator;


import com.vigilonix.samadhan.enums.ValidationError;
import com.vigilonix.samadhan.exception.ValidationRuntimeException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public interface ValidationService<T> {
    List<Validator<List<ValidationError>, T>> getValidators();

    default void validate(T t) {
        List<ValidationError> validatorResponse = getValidators().stream()
                .map(v -> v.validate(t))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(validatorResponse)) {
            throw new ValidationRuntimeException(validatorResponse);
        }
    }
}
