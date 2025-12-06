package io.life.simal_integration_service.exception;

/**
 * Base exception class for all service exceptions.
 */
public class UserServiceException extends RuntimeException {
    
    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
