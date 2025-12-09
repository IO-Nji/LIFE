package io.life.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionControlOrderDTO {

    private Long id;
    private String controlOrderNumber;
    private Long sourceProductionOrderId;
    private Long assignedWorkstationId;
    private String simalScheduleId;
    private String status;
    private LocalDateTime targetStartTime;
    private LocalDateTime targetCompletionTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualCompletionTime;
    private String priority;
    private String productionInstructions;
    private String qualityCheckpoints;
    private String safetyProcedures;
    private Integer estimatedDurationMinutes;
    private Integer actualDurationMinutes;
    private Integer defectsFound;
    private Integer defectsReworked;
    private Boolean reworkRequired;
    private String reworkNotes;
    private String operatorNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;
}
