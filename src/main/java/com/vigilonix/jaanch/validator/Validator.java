package com.vigilonix.jaanch.validator;

public interface Validator<T, M> {
    T validate(M m);
}
