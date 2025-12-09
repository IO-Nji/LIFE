package io.life.simal_integration_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for production order request to SimAL.
 * Represents an order to be scheduled in the production system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimalProductionOrderRequest {

    /**
     * Unique order identifier from Customer Order system.
     */
    private String orderNumber;

    /**
     * Customer name for the order.
     */
    private String customerName;

    /**
     * Due date for order completion (ISO 8601 format: YYYY-MM-DD).
     */
    private String dueDate;

    /**
     * Priority level: LOW, MEDIUM, HIGH.
     */
    private String priority;

    /**
     * List of line items to be produced.
     */
    private List<OrderLineItem> lineItems;

    /**
     * Additional notes or special instructions.
     */
    private String notes;

    /**
     * Line item for production order.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderLineItem {

        /**
         * Product/module identifier.
         */
        private String itemId;

        /**
         * Item name or description.
         */
        private String itemName;

        /**
         * Quantity to produce.
         */
        private Integer quantity;

        /**
         * Estimated production time in minutes.
         */
        private Integer estimatedDuration;

        /**
         * Required workstation type (MANUFACTURING, ASSEMBLY, WAREHOUSE).
         */
        private String workstationType;
    }
}
