package io.life.order.exception;

/**
 * Exception thrown when an error occurs during order processing.
 */
public class OrderProcessingException extends RuntimeException {

    public OrderProcessingException(String message) {
        super(message);
    }

    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
