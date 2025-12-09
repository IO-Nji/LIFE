package io.life.masterdata.exception;

/**
 * Base exception for all custom exceptions in the user service.
 */
public class UserServiceException extends RuntimeException {
    
    public UserServiceException(String message) {
        super(message);
    }

    public UserServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
