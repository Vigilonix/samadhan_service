package com.vigilonix.applicationnadministrativeservice.validator;

public interface Validator<T, M> {
    T validate(M m);
}
