package io.life.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ProductionOrder entity represents an order sent to the Production Planning (PP) system.
 * These orders are created when a WarehouseOrder cannot be fulfilled (Scenario 3).
 * ProductionOrders are submitted to SimAL for scheduling.
 */
@Entity
@Table(name = "production_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique production order number (e.g., PO-001, PO-002).
     */
    @Column(nullable = false, unique = true)
    private String productionOrderNumber;

    /**
     * Reference to the source customer order that triggered this production order.
     */
    @Column(nullable = false)
    private Long sourceCustomerOrderId;

    /**
     * Reference to the warehouse order that triggered this production order (Scenario 3).
     * Can be null for standalone production orders.
     */
    @Column(nullable = true)
    private Long sourceWarehouseOrderId;

    /**
     * Reference to the SimAL schedule ID once the order is submitted to SimAL.
     */
    @Column(nullable = true)
    private String simalScheduleId;

    /**
     * Production order status:
     * CREATED - Just created, not yet submitted to SimAL
     * SUBMITTED - Submitted to SimAL for scheduling
     * SCHEDULED - SimAL has generated a schedule
     * IN_PRODUCTION - Production has started
     * COMPLETED - Production completed
     * CANCELLED - Order cancelled
     */
    @Column(nullable = false)
    private String status; // CREATED, SUBMITTED, SCHEDULED, IN_PRODUCTION, COMPLETED, CANCELLED

    /**
     * Priority level for production.
     */
    @Column(nullable = false)
    private String priority; // LOW, MEDIUM, HIGH

    /**
     * Due date for production completion.
     */
    @Column(nullable = true)
    private LocalDateTime dueDate;

    /**
     * Scenario that triggered this production order.
     * SCENARIO_3: Warehouse order cannot be fulfilled (partial stock)
     * STANDALONE: Direct production order from customer
     */
    @Column(nullable = false)
    private String triggerScenario; // SCENARIO_3, STANDALONE, etc.

    /**
     * Workstation ID of the production planning operator who created this order.
     */
    @Column(nullable = false)
    private Long createdByWorkstationId;

    /**
     * Additional notes or special instructions for production.
     */
    @Column(length = 1000)
    private String notes;

    /**
     * Expected production time in minutes (estimated by SimAL).
     */
    @Column(nullable = true)
    private Integer estimatedDuration;

    /**
     * Expected completion time returned by SimAL.
     */
    @Column(nullable = true)
    private LocalDateTime expectedCompletionTime;

    /**
     * Actual completion time once production is done.
     */
    @Column(nullable = true)
    private LocalDateTime actualCompletionTime;

    /**
     * Audit fields.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Lifecycle hooks for audit fields.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "CREATED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
