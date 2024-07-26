package com.vigilonix.jaanch.exception;

public class InvalidLoginAttemptException extends RuntimeException {
    public InvalidLoginAttemptException(String message) {
        super(message);
    }
}
