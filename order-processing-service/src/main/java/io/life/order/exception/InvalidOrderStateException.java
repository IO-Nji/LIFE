package io.life.order.exception;

/**
 * Exception thrown when an order state transition is invalid.
 */
public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String message) {
        super(message);
    }

    public InvalidOrderStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidOrderStateException(String currentState, String requestedState) {
        super(String.format("Cannot transition from state '%s' to state '%s'", currentState, requestedState));
    }
}
