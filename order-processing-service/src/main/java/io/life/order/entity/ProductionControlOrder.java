package io.life.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * ProductionControlOrder entity represents an order assigned to a Production Control workstation.
 * Created from ProductionOrder when it enters IN_PRODUCTION status.
 * Contains detailed instructions for the production control operator.
 */
@Entity
@Table(name = "production_control_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionControlOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String controlOrderNumber;

    @Column(nullable = false)
    private Long sourceProductionOrderId;

    @Column(nullable = false)
    private Long assignedWorkstationId;

    @Column(nullable = false)
    private String simalScheduleId;

    @Column(nullable = false)
    private String status; // ASSIGNED, IN_PROGRESS, COMPLETED, HALTED, ABANDONED

    @Column(nullable = false)
    private LocalDateTime targetStartTime;

    @Column(nullable = false)
    private LocalDateTime targetCompletionTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualCompletionTime;

    @Column(length = 50)
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    @Column(length = 500)
    private String productionInstructions;

    @Column(length = 500)
    private String qualityCheckpoints;

    @Column(length = 500)
    private String safetyProcedures;

    private Integer estimatedDurationMinutes;

    private Integer actualDurationMinutes;

    private Integer defectsFound;

    private Integer defectsReworked;

    private Boolean reworkRequired;

    @Column(length = 500)
    private String reworkNotes;

    @Column(length = 500)
    private String operatorNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "ASSIGNED";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if ("COMPLETED".equals(status) && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
}
