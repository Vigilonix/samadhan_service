package com.vigilonix.samadhan.exception;

public class InvalidLoginAttemptException extends RuntimeException {
    public InvalidLoginAttemptException(String message) {
        super(message);
    }
}
