package io.life.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ProductionOrder.
 * Represents a production order that has been submitted to SimAL for scheduling.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionOrderDTO {

    private Long id;

    private String productionOrderNumber;

    private Long sourceCustomerOrderId;

    private Long sourceWarehouseOrderId;

    private String simalScheduleId;

    private String status; // CREATED, SUBMITTED, SCHEDULED, IN_PRODUCTION, COMPLETED, CANCELLED

    private String priority; // LOW, MEDIUM, HIGH

    private LocalDateTime dueDate;

    private String triggerScenario; // SCENARIO_3, STANDALONE

    private Long createdByWorkstationId;

    private String notes;

    private Integer estimatedDuration; // in minutes

    private LocalDateTime expectedCompletionTime;

    private LocalDateTime actualCompletionTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
