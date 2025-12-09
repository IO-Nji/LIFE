package io.life.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SupplyOrder entity represents a supply order for parts needed by production/assembly control workstations.
 * Created from ProductionControlOrder or AssemblyControlOrder when parts are needed.
 * Sent to Parts Supply Warehouse (workstation 9) for fulfillment.
 * When fulfilled, parts are debited from inventory-service.
 */
@Entity
@Table(name = "supply_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String supplyOrderNumber;

    /**
     * The control order that triggered this supply order
     * Can reference either ProductionControlOrder or AssemblyControlOrder
     */
    @Column(nullable = false)
    private Long sourceControlOrderId;

    /**
     * Type of control order that triggered this supply order
     * PRODUCTION or ASSEMBLY
     */
    @Column(nullable = false)
    private String sourceControlOrderType; // PRODUCTION, ASSEMBLY

    /**
     * The workstation requesting the parts (where the control order is being executed)
     * This is the production or assembly workstation
     */
    @Column(nullable = false)
    private Long requestingWorkstationId;

    /**
     * The workstation that fulfills the supply (Parts Supply Warehouse = workstation 9)
     */
    @Column(nullable = false)
    private Long supplyWarehouseWorkstationId;

    /**
     * Status of the supply order
     * PENDING: Waiting to be picked from warehouse
     * IN_PROGRESS: Being picked/prepared
     * FULFILLED: All items supplied to requesting workstation
     * REJECTED: Could not fulfill (insufficient stock)
     * CANCELLED: Supply order cancelled
     */
    @Column(nullable = false)
    private String status; // PENDING, IN_PROGRESS, FULFILLED, REJECTED, CANCELLED

    /**
     * List of supply items needed
     * Each item references a part ID and quantity needed
     */
    @OneToMany(mappedBy = "supplyOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SupplyOrderItem> supplyOrderItems;

    /**
     * Priority inherited from the source control order
     */
    @Column(length = 50)
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    /**
     * Timestamps
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime requestedByTime; // When parts are needed by

    private LocalDateTime fulfilledAt; // When supply order was fulfilled

    private LocalDateTime rejectedAt; // When supply order was rejected

    private LocalDateTime cancelledAt; // When supply order was cancelled

    private LocalDateTime updatedAt;

    /**
     * Additional notes from requester or warehouse staff
     */
    @Column(length = 500)
    private String notes;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
