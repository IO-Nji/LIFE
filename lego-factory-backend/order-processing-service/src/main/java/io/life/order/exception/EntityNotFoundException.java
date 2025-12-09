package io.life.order.exception;

/**
 * Exception thrown when an entity is not found in the database.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s with ID %d not found", entityName, id));
    }

    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s with identifier %s not found", entityName, identifier));
    }
}
