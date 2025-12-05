package io.life.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "warehouse_order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_order_id", nullable = false)
    private WarehouseOrder warehouseOrder;

    @Column(nullable = false)
    private Long itemId; // Product or Module ID

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer requestedQuantity;

    @Column(nullable = false)
    private Integer fulfilledQuantity; // 0 initially, updated when fulfilled

    private String itemType; // "MODULE" or "PART"

    private String notes;
}
