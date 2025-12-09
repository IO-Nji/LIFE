package io.life.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for SupplyOrder entity.
 * Used for API responses and requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyOrderDTO {

    private Long id;

    private String supplyOrderNumber;

    private Long sourceControlOrderId;

    private String sourceControlOrderType; // PRODUCTION, ASSEMBLY

    private Long requestingWorkstationId;

    private Long supplyWarehouseWorkstationId;

    private String status; // PENDING, IN_PROGRESS, FULFILLED, REJECTED, CANCELLED

    private List<SupplyOrderItemDTO> supplyOrderItems;

    private String priority; // LOW, MEDIUM, HIGH, URGENT

    private LocalDateTime createdAt;

    private LocalDateTime requestedByTime;

    private LocalDateTime fulfilledAt;

    private LocalDateTime rejectedAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime updatedAt;

    private String notes;
}
