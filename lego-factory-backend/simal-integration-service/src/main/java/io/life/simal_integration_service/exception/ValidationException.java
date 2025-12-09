package io.life.simal_integration_service.exception;

/**
 * Exception thrown when there is a validation error.
 */
public class ValidationException extends UserServiceException {
    
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
