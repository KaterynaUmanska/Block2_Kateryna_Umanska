package org.example.block2.exception;

/**
 * Exception thrown when invalid file format errors during import.
 */
public class InvalidFileFormatException extends RuntimeException {
    public InvalidFileFormatException(String message) {
        super(message);
    }
}