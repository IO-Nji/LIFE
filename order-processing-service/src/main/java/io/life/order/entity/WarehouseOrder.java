package io.life.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "warehouse_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String warehouseOrderNumber;

    @Column(nullable = false)
    private Long sourceCustomerOrderId; // Link to the customer order that triggered this

    @Column(nullable = false)
    private Long requestingWorkstationId; // Plant Warehouse (workstation 7)

    @Column(nullable = false)
    private Long fulfillingWorkstationId; // Modules Supermarket (workstation 8)

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private String status; // PENDING, PROCESSING, FULFILLED, REJECTED, CANCELLED

    // Items needed for this warehouse order (modules or parts)
    @OneToMany(mappedBy = "warehouseOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WarehouseOrderItem> warehouseOrderItems;

    // Fulfillment scenario that triggered this order
    private String triggerScenario; // "SCENARIO_2", "SCENARIO_3", etc.

    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
