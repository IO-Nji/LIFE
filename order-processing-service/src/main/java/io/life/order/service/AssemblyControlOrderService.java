package io.life.order.service;

import io.life.order.dto.AssemblyControlOrderDTO;
import io.life.order.dto.SupplyOrderDTO;
import io.life.order.dto.SupplyOrderItemDTO;
import io.life.order.entity.AssemblyControlOrder;
import io.life.order.repository.AssemblyControlOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final RestTemplate restTemplate;
    private final InventoryService inventoryService;

    @Value("${simal.service.url:http://localhost:8018}")
    private String simalServiceUrl;

    @Value("${modules.supermarket.workstation.id:8}")
    private Long modulesSupermarketWorkstationId;

    @Value("${plant.warehouse.workstation.id:7}")
    private Long plantWarehouseWorkstationId;

    public AssemblyControlOrderService(AssemblyControlOrderRepository repository, 
                                      SupplyOrderService supplyOrderService,
                                      RestTemplate restTemplate,
                                      InventoryService inventoryService) {
        this.repository = repository;
        this.supplyOrderService = supplyOrderService;
        this.restTemplate = restTemplate;
        this.inventoryService = inventoryService;
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
     * Complete assembly production with SimAL and Modules Supermarket inventory integration.
     * Used for Gear Assembly and Motor Assembly workstations.
     * Credits Modules Supermarket (WS-8) with one module unit.
     */
    public AssemblyControlOrderDTO completeAssemblyProduction(Long id) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot complete - order status is " + order.getStatus());
        }

        // Step 1: Update control order status and timestamps
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
        logger.info("Completed assembly production on control order {}", order.getControlOrderNumber());

        // Step 2: Call SimAL to update schedule status (fire-and-forget)
        try {
            updateSimalScheduleStatus(order.getSimalScheduleId(), "COMPLETED");
        } catch (Exception e) {
            logger.warn("Failed to update SimAL schedule status for {}: {}", order.getSimalScheduleId(), e.getMessage());
            // Don't throw - completion already succeeded, SimAL update is secondary
        }

        // Step 3: Credit Modules Supermarket inventory (fire-and-forget)
        try {
            creditModulesSupermarket(order.getSourceProductionOrderId(), 1);
        } catch (Exception e) {
            logger.warn("Failed to credit Modules Supermarket for order {}: {}", order.getSourceProductionOrderId(), e.getMessage());
            // Don't throw - completion already succeeded, inventory update is secondary
        }

        return mapToDTO(updated);
    }

    /**
     * Complete final assembly with SimAL and Plant Warehouse inventory integration.
     * Used only for Final Assembly workstations.
     * Credits Plant Warehouse (WS-7) with one finished product unit instead of Modules Supermarket.
     * This represents the completion of the entire product ready for shipping.
     */
    public AssemblyControlOrderDTO completeFinalAssembly(Long id) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot complete - order status is " + order.getStatus());
        }

        // Step 1: Update control order status and timestamps
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
        logger.info("Completed final assembly on control order {}", order.getControlOrderNumber());

        // Step 2: Call SimAL to update schedule status (fire-and-forget)
        try {
            updateSimalScheduleStatus(order.getSimalScheduleId(), "COMPLETED");
        } catch (Exception e) {
            logger.warn("Failed to update SimAL schedule status for {}: {}", order.getSimalScheduleId(), e.getMessage());
            // Don't throw - completion already succeeded, SimAL update is secondary
        }

        // Step 3: Credit Plant Warehouse inventory (fire-and-forget) - FINAL ASSEMBLY ONLY
        try {
            creditPlantWarehouse(order.getSourceProductionOrderId(), 1);
        } catch (Exception e) {
            logger.warn("Failed to credit Plant Warehouse for order {}: {}", order.getSourceProductionOrderId(), e.getMessage());
            // Don't throw - completion already succeeded, inventory update is secondary
        }

        return mapToDTO(updated);
    }

    /**
     * Halt assembly on a control order.
     */
    public AssemblyControlOrderDTO haltAssembly(Long id) {
        AssemblyControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        if (!"IN_PROGRESS".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot halt - order status is " + order.getStatus());
        }

        order.setStatus("HALTED");
        AssemblyControlOrder updated = repository.save(order);
        logger.warn("Halted assembly on control order {}", order.getControlOrderNumber());

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

    /**
     * Update SimAL schedule status via SimAL Integration Service.
     */
    private void updateSimalScheduleStatus(String scheduleId, String status) {
        try {
            String url = simalServiceUrl + "/api/simal/scheduled-orders/" + scheduleId + "/status";
            Map<String, Object> request = new HashMap<>();
            request.put("status", status);
            request.put("completedAt", LocalDateTime.now().toString());
            
            restTemplate.postForObject(url, request, String.class);
            logger.info("Updated SimAL schedule {} status to {}", scheduleId, status);
        } catch (Exception e) {
            logger.error("Failed to update SimAL schedule status for {}", scheduleId, e);
            throw new RuntimeException("SimAL update failed: " + e.getMessage(), e);
        }
    }

    /**
     * Credit Modules Supermarket inventory when assembly completes.
     * Awards one module unit to Modules Supermarket (workstation 8).
     */
    private void creditModulesSupermarket(Long productionOrderId, Integer quantity) {
        try {
            inventoryService.updateStock(modulesSupermarketWorkstationId, 1L, quantity);
            logger.info("Credited Modules Supermarket with {} units for production order {}", quantity, productionOrderId);
        } catch (Exception e) {
            logger.error("Failed to credit Modules Supermarket for production order {}", productionOrderId, e);
            throw new RuntimeException("Inventory credit failed: " + e.getMessage(), e);
        }
    }

    /**
     * Credit Plant Warehouse inventory when final assembly completes.
     * Awards one finished product unit to Plant Warehouse (workstation 7).
     * Only used for Final Assembly workstations.
     */
    private void creditPlantWarehouse(Long productionOrderId, Integer quantity) {
        try {
            inventoryService.updateStock(plantWarehouseWorkstationId, 1L, quantity);
            logger.info("Credited Plant Warehouse with {} finished units for production order {}", quantity, productionOrderId);
        } catch (Exception e) {
            logger.error("Failed to credit Plant Warehouse for production order {}", productionOrderId, e);
            throw new RuntimeException("Inventory credit failed: " + e.getMessage(), e);
        }
    }
}

