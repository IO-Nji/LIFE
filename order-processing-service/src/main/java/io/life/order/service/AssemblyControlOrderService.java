package io.life.order.service;

import io.life.order.dto.AssemblyControlOrderDTO;
import io.life.order.dto.SupplyOrderDTO;
import io.life.order.dto.SupplyOrderItemDTO;
import io.life.order.entity.AssemblyControlOrder;
import io.life.order.repository.AssemblyControlOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing AssemblyControlOrder entities.
 * Handles control orders assigned to Assembly Control workstations.
 */
@Service
@Transactional
public class AssemblyControlOrderService {

    private static final Logger logger = LoggerFactory.getLogger(AssemblyControlOrderService.class);

    private final AssemblyControlOrderRepository repository;
    private final SupplyOrderService supplyOrderService;

    public AssemblyControlOrderService(AssemblyControlOrderRepository repository, SupplyOrderService supplyOrderService) {
        this.repository = repository;
        this.supplyOrderService = supplyOrderService;
    }

    /**
     * Create an assembly control order from a production order.
     */
    public AssemblyControlOrderDTO createControlOrder(
            Long sourceProductionOrderId,
            Long assignedWorkstationId,
            String simalScheduleId,
            String priority,
            LocalDateTime targetStartTime,
            LocalDateTime targetCompletionTime,
            String assemblyInstructions,
            String qualityCheckpoints,
            String testingProcedures,
            String packagingRequirements,
            Integer estimatedDurationMinutes) {

        String controlOrderNumber = generateControlOrderNumber();

        AssemblyControlOrder order = AssemblyControlOrder.builder()
                .controlOrderNumber(controlOrderNumber)
                .sourceProductionOrderId(sourceProductionOrderId)
                .assignedWorkstationId(assignedWorkstationId)
                .simalScheduleId(simalScheduleId)
                .status("ASSIGNED")
                .priority(priority)
                .targetStartTime(targetStartTime)
                .targetCompletionTime(targetCompletionTime)
                .assemblyInstructions(assemblyInstructions)
                .qualityCheckpoints(qualityCheckpoints)
                .testingProcedures(testingProcedures)
                .packagingRequirements(packagingRequirements)
                .estimatedDurationMinutes(estimatedDurationMinutes)
                .build();

        AssemblyControlOrder saved = repository.save(order);
        logger.info("Created assembly control order {} for workstation {}", controlOrderNumber, assignedWorkstationId);

        return mapToDTO(saved);
    }

    /**
     * Get all control orders for a workstation.
     */
    public List<AssemblyControlOrderDTO> getOrdersByWorkstation(Long workstationId) {
        return repository.findByAssignedWorkstationId(workstationId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active control orders for a workstation.
     */
    public List<AssemblyControlOrderDTO> getActiveOrdersByWorkstation(Long workstationId) {
        return repository.findByAssignedWorkstationIdAndStatus(workstationId, "IN_PROGRESS").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all unassigned control orders (status = ASSIGNED).
     */
    public List<AssemblyControlOrderDTO> getUnassignedOrders(Long workstationId) {
        return repository.findByAssignedWorkstationIdAndStatus(workstationId, "ASSIGNED").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get control order by ID.
     */
    public Optional<AssemblyControlOrderDTO> getOrderById(Long id) {
        return repository.findById(id).map(this::mapToDTO);
    }

    /**
     * Get control order by control order number.
     */
    public Optional<AssemblyControlOrderDTO> getOrderByNumber(String controlOrderNumber) {
        return repository.findByControlOrderNumber(controlOrderNumber).map(this::mapToDTO);
    }

    /**
     * Start assembly on a control order.
     */
    public AssemblyControlOrderDTO startAssembly(Long id) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        if (!"ASSIGNED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot start assembly - order status is " + order.getStatus());
        }

        order.setStatus("IN_PROGRESS");
        order.setActualStartTime(LocalDateTime.now());

        AssemblyControlOrder updated = repository.save(order);
        logger.info("Started assembly on control order {}", order.getControlOrderNumber());

        return mapToDTO(updated);
    }

    /**
     * Complete assembly on a control order.
     */
    public AssemblyControlOrderDTO completeAssembly(Long id) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        order.setStatus("COMPLETED");
        order.setActualCompletionTime(LocalDateTime.now());
        
        if (order.getActualStartTime() != null) {
            long minutes = java.time.temporal.ChronoUnit.MINUTES.between(
                    order.getActualStartTime(),
                    order.getActualCompletionTime()
            );
            order.setActualDurationMinutes((int) minutes);
        }

        AssemblyControlOrder updated = repository.save(order);
        logger.info("Completed assembly on control order {}", order.getControlOrderNumber());

        return mapToDTO(updated);
    }

    /**
     * Halt assembly on a control order.
     */
    public AssemblyControlOrderDTO haltAssembly(Long id, String reason) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        order.setStatus("HALTED");
        order.setOperatorNotes("Halted: " + reason);

        AssemblyControlOrder updated = repository.save(order);
        logger.warn("Halted assembly on control order {}: {}", order.getControlOrderNumber(), reason);

        return mapToDTO(updated);
    }

    /**
     * Update operator notes.
     */
    public AssemblyControlOrderDTO updateOperatorNotes(Long id, String notes) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        order.setOperatorNotes(notes);
        AssemblyControlOrder updated = repository.save(order);

        return mapToDTO(updated);
    }

    /**
     * Update defect information.
     */
    public AssemblyControlOrderDTO updateDefects(Long id, Integer defectsFound, Integer defectsReworked, Boolean reworkRequired) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        order.setDefectsFound(defectsFound);
        order.setDefectsReworked(defectsReworked);
        order.setReworkRequired(reworkRequired);

        AssemblyControlOrder updated = repository.save(order);
        logger.info("Updated defect info for control order {}: found={}, reworked={}", 
                order.getControlOrderNumber(), defectsFound, defectsReworked);

        return mapToDTO(updated);
    }

    /**
     * Update shipping notes.
     */
    public AssemblyControlOrderDTO updateShippingNotes(Long id, String shippingNotes) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        order.setShippingNotes(shippingNotes);
        AssemblyControlOrder updated = repository.save(order);

        return mapToDTO(updated);
    }

    /**
     * Request parts/supplies for this assembly control order.
     * Creates a SupplyOrder that will be sent to the Parts Supply Warehouse.
     */
    public SupplyOrderDTO requestSupplies(
            Long controlOrderId,
            List<SupplyOrderItemDTO> requiredParts,
            LocalDateTime neededBy,
            String notes) {
        
        AssemblyControlOrder order = repository.findById(controlOrderId)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + controlOrderId));

        return supplyOrderService.createSupplyOrder(
                controlOrderId,
                "ASSEMBLY",
                order.getAssignedWorkstationId(),
                order.getPriority(),
                neededBy,
                requiredParts,
                notes
        );
    }

    /**
     * Generate unique control order number.
     */
    private String generateControlOrderNumber() {
        long count = repository.count();
        return "ACO-" + String.format("%04d", count + 1);
    }

    /**
     * Map entity to DTO.
     */
    private AssemblyControlOrderDTO mapToDTO(AssemblyControlOrder order) {
        return AssemblyControlOrderDTO.builder()
                .id(order.getId())
                .controlOrderNumber(order.getControlOrderNumber())
                .sourceProductionOrderId(order.getSourceProductionOrderId())
                .assignedWorkstationId(order.getAssignedWorkstationId())
                .simalScheduleId(order.getSimalScheduleId())
                .status(order.getStatus())
                .targetStartTime(order.getTargetStartTime())
                .targetCompletionTime(order.getTargetCompletionTime())
                .actualStartTime(order.getActualStartTime())
                .actualCompletionTime(order.getActualCompletionTime())
                .priority(order.getPriority())
                .assemblyInstructions(order.getAssemblyInstructions())
                .qualityCheckpoints(order.getQualityCheckpoints())
                .testingProcedures(order.getTestingProcedures())
                .packagingRequirements(order.getPackagingRequirements())
                .estimatedDurationMinutes(order.getEstimatedDurationMinutes())
                .actualDurationMinutes(order.getActualDurationMinutes())
                .defectsFound(order.getDefectsFound())
                .defectsReworked(order.getDefectsReworked())
                .reworkRequired(order.getReworkRequired())
                .reworkNotes(order.getReworkNotes())
                .operatorNotes(order.getOperatorNotes())
                .shippingNotes(order.getShippingNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
}
