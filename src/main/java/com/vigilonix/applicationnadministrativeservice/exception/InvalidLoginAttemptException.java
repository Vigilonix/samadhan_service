package com.vigilonix.applicationnadministrativeservice.exception;

public class InvalidLoginAttemptException extends RuntimeException {
    public InvalidLoginAttemptException(String message) {
        super(message);
    }
}
