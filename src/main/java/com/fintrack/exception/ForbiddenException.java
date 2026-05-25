package com.fintrack.exception;

/**
 * Thrown when a user tries to access or modify a resource that belongs to another user.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
