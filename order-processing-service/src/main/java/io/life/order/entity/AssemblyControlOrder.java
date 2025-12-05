package io.life.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * AssemblyControlOrder entity represents an order assigned to an Assembly Control workstation.
 * Created from ProductionOrder when production is complete and assembly begins.
 * Contains detailed instructions for the assembly control operator.
 */
@Entity
@Table(name = "assembly_control_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssemblyControlOrder {

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
    private String assemblyInstructions;

    @Column(length = 500)
    private String qualityCheckpoints;

    @Column(length = 500)
    private String testingProcedures;

    @Column(length = 500)
    private String packagingRequirements;

    private Integer estimatedDurationMinutes;

    private Integer actualDurationMinutes;

    private Integer defectsFound;

    private Integer defectsReworked;

    private Boolean reworkRequired;

    @Column(length = 500)
    private String reworkNotes;

    @Column(length = 500)
    private String operatorNotes;

    @Column(length = 500)
    private String shippingNotes;

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
