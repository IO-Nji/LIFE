package io.life.order.exception;

/**
 * Exception thrown when there is insufficient quantity available.
 */
public class InsufficientQuantityException extends RuntimeException {

    public InsufficientQuantityException(String message) {
        super(message);
    }

    public InsufficientQuantityException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientQuantityException(Integer required, Integer available) {
        super(String.format("Insufficient quantity. Required: %d, Available: %d", required, available));
    }
}
