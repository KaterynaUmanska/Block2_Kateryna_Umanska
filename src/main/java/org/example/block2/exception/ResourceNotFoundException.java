package org.example.block2.exception;

/**
 * Exception thrown when resource not found.
 */
public class ResourceNotFoundException  extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
