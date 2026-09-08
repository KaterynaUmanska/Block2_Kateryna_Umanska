package org.example.block2.exception;

/**
 * Exception thrown when resource conflicts with an existing resource.
 */
public class DuplicateResourceException extends RuntimeException  {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
