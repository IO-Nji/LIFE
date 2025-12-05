package io.life.order.util;

import io.life.order.exception.InvalidOrderStateException;

import java.util.*;

/**
 * Utility class for managing valid order state transitions.
 */
public class OrderStateValidator {

    /**
     * Valid state transitions for manufacturing orders
     */
    private static final Map<String, Set<String>> MANUFACTURING_TRANSITIONS = new HashMap<>();

    /**
     * Valid state transitions for assembly orders
     */
    private static final Map<String, Set<String>> ASSEMBLY_TRANSITIONS = new HashMap<>();

    /**
     * Valid state transitions for production control orders
     */
    private static final Map<String, Set<String>> CONTROL_TRANSITIONS = new HashMap<>();

    /**
     * Valid state transitions for supplier orders
     */
    private static final Map<String, Set<String>> SUPPLIER_TRANSITIONS = new HashMap<>();

    static {
        // Manufacturing order transitions
        MANUFACTURING_TRANSITIONS.put("CREATED", new HashSet<>(Arrays.asList("ASSIGNED", "CANCELLED")));
        MANUFACTURING_TRANSITIONS.put("ASSIGNED", new HashSet<>(Arrays.asList("IN_PROGRESS", "CANCELLED")));
        MANUFACTURING_TRANSITIONS.put("IN_PROGRESS", new HashSet<>(Arrays.asList("COMPLETED", "HALTED")));
        MANUFACTURING_TRANSITIONS.put("COMPLETED", new HashSet<>(Arrays.asList()));
        MANUFACTURING_TRANSITIONS.put("HALTED", new HashSet<>(Arrays.asList("IN_PROGRESS", "CANCELLED")));
        MANUFACTURING_TRANSITIONS.put("CANCELLED", new HashSet<>(Arrays.asList()));

        // Assembly order transitions
        ASSEMBLY_TRANSITIONS.put("CREATED", new HashSet<>(Arrays.asList("ASSIGNED", "CANCELLED")));
        ASSEMBLY_TRANSITIONS.put("ASSIGNED", new HashSet<>(Arrays.asList("IN_PROGRESS", "CANCELLED")));
        ASSEMBLY_TRANSITIONS.put("IN_PROGRESS", new HashSet<>(Arrays.asList("COMPLETED", "HALTED")));
        ASSEMBLY_TRANSITIONS.put("COMPLETED", new HashSet<>(Arrays.asList()));
        ASSEMBLY_TRANSITIONS.put("HALTED", new HashSet<>(Arrays.asList("IN_PROGRESS", "CANCELLED")));
        ASSEMBLY_TRANSITIONS.put("CANCELLED", new HashSet<>(Arrays.asList()));

        // Production Control order transitions
        CONTROL_TRANSITIONS.put("CREATED", new HashSet<>(Arrays.asList("ASSIGNED", "CANCELLED")));
        CONTROL_TRANSITIONS.put("ASSIGNED", new HashSet<>(Arrays.asList("IN_PROGRESS", "CANCELLED")));
        CONTROL_TRANSITIONS.put("IN_PROGRESS", new HashSet<>(Arrays.asList("COMPLETED", "HALTED")));
        CONTROL_TRANSITIONS.put("COMPLETED", new HashSet<>(Arrays.asList()));
        CONTROL_TRANSITIONS.put("HALTED", new HashSet<>(Arrays.asList("IN_PROGRESS", "CANCELLED")));
        CONTROL_TRANSITIONS.put("CANCELLED", new HashSet<>(Arrays.asList()));

        // Supplier order transitions
        SUPPLIER_TRANSITIONS.put("CREATED", new HashSet<>(Arrays.asList("SENT", "CANCELLED")));
        SUPPLIER_TRANSITIONS.put("SENT", new HashSet<>(Arrays.asList("PARTIALLY_RECEIVED", "RECEIVED", "CANCELLED")));
        SUPPLIER_TRANSITIONS.put("PARTIALLY_RECEIVED", new HashSet<>(Arrays.asList("PARTIALLY_RECEIVED", "RECEIVED", "CANCELLED")));
        SUPPLIER_TRANSITIONS.put("RECEIVED", new HashSet<>(Arrays.asList()));
        SUPPLIER_TRANSITIONS.put("CANCELLED", new HashSet<>(Arrays.asList()));
    }

    /**
     * Validate if a state transition is allowed for a manufacturing order
     */
    public static void validateManufacturingTransition(String currentState, String newState) {
        validateTransition(currentState, newState, MANUFACTURING_TRANSITIONS, "Manufacturing Order");
    }

    /**
     * Validate if a state transition is allowed for an assembly order
     */
    public static void validateAssemblyTransition(String currentState, String newState) {
        validateTransition(currentState, newState, ASSEMBLY_TRANSITIONS, "Assembly Order");
    }

    /**
     * Validate if a state transition is allowed for a production control order
     */
    public static void validateControlTransition(String currentState, String newState) {
        validateTransition(currentState, newState, CONTROL_TRANSITIONS, "Production Control Order");
    }

    /**
     * Validate if a state transition is allowed for a supplier order
     */
    public static void validateSupplierTransition(String currentState, String newState) {
        validateTransition(currentState, newState, SUPPLIER_TRANSITIONS, "Supplier Order");
    }

    /**
     * Generic state transition validation
     */
    private static void validateTransition(String currentState, String newState,
                                          Map<String, Set<String>> transitions, String orderType) {
        if (!transitions.containsKey(currentState)) {
            throw new InvalidOrderStateException(
                    String.format("Unknown state '%s' for %s", currentState, orderType)
            );
        }

        Set<String> validNextStates = transitions.get(currentState);
        if (!validNextStates.contains(newState)) {
            throw new InvalidOrderStateException(currentState, newState);
        }
    }
}
