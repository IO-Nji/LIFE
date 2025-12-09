package io.life.samplefactory.gateway.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends UserServiceException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
