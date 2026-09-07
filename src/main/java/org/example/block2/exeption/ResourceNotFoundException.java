package org.example.block2.exeption;

public class ResourceNotFoundException  extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
