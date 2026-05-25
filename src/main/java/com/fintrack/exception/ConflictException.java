package com.fintrack.exception;

/**
 * Thrown when attempting to create a resource that already exists (e.g. duplicate category name).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
