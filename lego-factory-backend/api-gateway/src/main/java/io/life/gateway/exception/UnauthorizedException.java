package io.life.gateway.exception;

/**
 * Exception thrown when there is an unauthorized access attempt.
 */
public class UnauthorizedException extends UserServiceException {
    
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
