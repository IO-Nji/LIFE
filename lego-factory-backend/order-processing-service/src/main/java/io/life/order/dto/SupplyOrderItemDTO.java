package io.life.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SupplyOrderItem entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyOrderItemDTO {

    private Long id;

    private Long partId;

    private Integer quantityRequested;

    private Integer quantitySupplied;

    private String unit;

    private String notes;
}
