package com.exe201.project.exception;

public class InsufficientUsageException extends RuntimeException {
    public InsufficientUsageException(String message) {
        super(message);
    }
}
