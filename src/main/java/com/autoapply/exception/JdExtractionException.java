package com.autoapply.exception;

public class JdExtractionException extends RuntimeException {
    public JdExtractionException(String message) {
        super(message);
    }

    public JdExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
