package com.greenhouse.app.exception;

/**
 * Exception thrown when a requested resource does not exist in the database.
 *
 * <p>This exception is mapped to HTTP 404 Not Found by the global exception handler.</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception with the given detail message.
     *
     * @param message human-readable description of the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with a formatted message.
     *
     * @param resourceName the entity class name (e.g., "Greenhouse")
     * @param id           the primary key that was not found
     */
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " with id " + id + " not found");
    }
}
