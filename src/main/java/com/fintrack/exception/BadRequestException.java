package com.fintrack.exception;

/**
 * Thrown for business-rule validation failures (e.g. future date on transaction).
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
