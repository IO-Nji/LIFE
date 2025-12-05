package io.life.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseOrderItemDTO {
    private Long id;
    private Long itemId;
    private String itemName;
    private Integer requestedQuantity;
    private Integer fulfilledQuantity;
    private String itemType; // MODULE or PART
    private String notes;
}
