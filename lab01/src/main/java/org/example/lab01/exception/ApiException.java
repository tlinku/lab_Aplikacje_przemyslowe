package org.example.lab01.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
