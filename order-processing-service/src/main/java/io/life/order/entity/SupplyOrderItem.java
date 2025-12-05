package io.life.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * SupplyOrderItem represents a single part/item needed in a SupplyOrder.
 * References a part ID and the quantity needed.
 */
@Entity
@Table(name = "supply_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_order_id", nullable = false)
    private SupplyOrder supplyOrder;

    /**
     * Part ID from masterdata-service
     */
    @Column(nullable = false)
    private Long partId;

    /**
     * Quantity of this part needed
     */
    @Column(nullable = false)
    private Integer quantityRequested;

    /**
     * Quantity actually supplied (may be less than requested if partial fulfillment)
     */
    private Integer quantitySupplied;

    /**
     * Unit of measurement (e.g., "piece", "pack", "box")
     */
    @Column(length = 50)
    private String unit;

    /**
     * Notes specific to this item
     */
    @Column(length = 500)
    private String notes;
}
