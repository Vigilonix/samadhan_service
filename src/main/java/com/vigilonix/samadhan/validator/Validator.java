package com.vigilonix.samadhan.validator;

public interface Validator<T, M> {
    T validate(M m);
}
