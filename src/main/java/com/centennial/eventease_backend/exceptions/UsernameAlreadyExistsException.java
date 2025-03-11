package com.centennial.eventease_backend.exceptions;

public class UsernameAlreadyExistsException extends Exception {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
