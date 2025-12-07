package io.life.order.service;

import io.life.order.dto.ProductionControlOrderDTO;
import io.life.order.dto.SupplyOrderDTO;
import io.life.order.dto.SupplyOrderItemDTO;
import io.life.order.entity.ProductionControlOrder;
import io.life.order.repository.ProductionControlOrderRepository;
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
 * Service for managing ProductionControlOrder entities.
 * Handles control orders assigned to Production Control workstations.
 */
@Service
@Transactional
public class ProductionControlOrderService {

    private static final Logger logger = LoggerFactory.getLogger(ProductionControlOrderService.class);

    private final ProductionControlOrderRepository repository;
    private final SupplyOrderService supplyOrderService;
    private final RestTemplate restTemplate;
    private final InventoryService inventoryService;

    @Value("${simal.service.url:http://localhost:8018}")
    private String simalServiceUrl;

    @Value("${modules.supermarket.workstation.id:8}")
    private Long modulesSupermarketWorkstationId;

    public ProductionControlOrderService(ProductionControlOrderRepository repository, 
                                        SupplyOrderService supplyOrderService,
                                        RestTemplate restTemplate,
                                        InventoryService inventoryService) {
        this.repository = repository;
        this.supplyOrderService = supplyOrderService;
        this.restTemplate = restTemplate;
        this.inventoryService = inventoryService;
    }

    /**
     * Create a production control order from a production order.
     */
    public ProductionControlOrderDTO createControlOrder(
            Long sourceProductionOrderId,
            Long assignedWorkstationId,
            String simalScheduleId,
            String priority,
            LocalDateTime targetStartTime,
            LocalDateTime targetCompletionTime,
            String productionInstructions,
            String qualityCheckpoints,
            String safetyProcedures,
            Integer estimatedDurationMinutes) {

        String controlOrderNumber = generateControlOrderNumber();

        ProductionControlOrder order = ProductionControlOrder.builder()
                .controlOrderNumber(controlOrderNumber)
                .sourceProductionOrderId(sourceProductionOrderId)
                .assignedWorkstationId(assignedWorkstationId)
                .simalScheduleId(simalScheduleId)
                .status("ASSIGNED")
                .priority(priority)
                .targetStartTime(targetStartTime)
                .targetCompletionTime(targetCompletionTime)
                .productionInstructions(productionInstructions)
                .qualityCheckpoints(qualityCheckpoints)
                .safetyProcedures(safetyProcedures)
                .estimatedDurationMinutes(estimatedDurationMinutes)
                .build();

        ProductionControlOrder saved = repository.save(order);
        logger.info("Created production control order {} for workstation {}", controlOrderNumber, assignedWorkstationId);

        return mapToDTO(saved);
    }

    /**
     * Get all control orders.
     */
    public List<ProductionControlOrderDTO> getAllOrders() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all control orders for a workstation.
     */
    public List<ProductionControlOrderDTO> getOrdersByWorkstation(Long workstationId) {
        return repository.findByAssignedWorkstationId(workstationId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active control orders for a workstation.
     */
    public List<ProductionControlOrderDTO> getActiveOrdersByWorkstation(Long workstationId) {
        return repository.findByAssignedWorkstationIdAndStatus(workstationId, "IN_PROGRESS").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all unassigned control orders (status = ASSIGNED).
     */
    public List<ProductionControlOrderDTO> getUnassignedOrders(Long workstationId) {
        return repository.findByAssignedWorkstationIdAndStatus(workstationId, "ASSIGNED").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get control order by ID.
     */
    public Optional<ProductionControlOrderDTO> getOrderById(Long id) {
        return repository.findById(id).map(this::mapToDTO);
    }

    /**
     * Get control order by control order number.
     */
    public Optional<ProductionControlOrderDTO> getOrderByNumber(String controlOrderNumber) {
        return repository.findByControlOrderNumber(controlOrderNumber).map(this::mapToDTO);
    }

    /**
     * Start production on a control order.
     */
    public ProductionControlOrderDTO startProduction(Long id) {
        ProductionControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        if (!"ASSIGNED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot start production - order status is " + order.getStatus());
        }

        order.setStatus("IN_PROGRESS");
        order.setActualStartTime(LocalDateTime.now());

        ProductionControlOrder updated = repository.save(order);
        logger.info("Started production on control order {}", order.getControlOrderNumber());

        return mapToDTO(updated);
    }

    /**
     * Complete production on a control order.
     */
    public ProductionControlOrderDTO completeProduction(Long id) {
        ProductionControlOrder order = repository.findById(id)
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

        ProductionControlOrder updated = repository.save(order);
        logger.info("Completed production on control order {}", order.getControlOrderNumber());

        return mapToDTO(updated);
    }

    /**
     * Complete manufacturing production with SimAL integration and Modules Supermarket inventory update.
     * This is called when a manufacturing workstation completes their assigned task.
     * 
     * Flow:
     * 1. Update control order status to COMPLETED with actual completion time
     * 2. Call SimAL to update production schedule status
     * 3. Credit Modules Supermarket (workstation 8) with completed modules/products
     *
     * @param id Control order ID
     * @return Updated control order DTO
     */
    public ProductionControlOrderDTO completeManufacturingProduction(Long id) {
        ProductionControlOrder order = repository.findById(id)
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

        ProductionControlOrder updated = repository.save(order);
        logger.info("Completed manufacturing production on control order {}", order.getControlOrderNumber());

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
     * Credit Modules Supermarket inventory when manufacturing completes.
     * Awards one module unit to Modules Supermarket (workstation 8).
     */
    private void creditModulesSupermarket(Long productionOrderId, Integer quantity) {
        try {
            // Award to Modules Supermarket - we assume product ID 1 for completed modules
            // This could be enhanced to track actual module IDs based on production order
            boolean credited = inventoryService.updateStock(
                    modulesSupermarketWorkstationId,
                    1L, // Product ID 1 represents completed modules
                    -quantity  // Negative to ADD to inventory
            );
            
            if (credited) {
                logger.info("Credited Modules Supermarket with {} module unit(s) for production order {}", 
                        quantity, productionOrderId);
            } else {
                logger.warn("Failed to credit Modules Supermarket for production order {}", productionOrderId);
                throw new RuntimeException("Inventory credit failed");
            }
        } catch (Exception e) {
            logger.error("Failed to credit Modules Supermarket for order {}", productionOrderId, e);
            throw new RuntimeException("Inventory credit failed: " + e.getMessage(), e);
        }
    }

    /**
     * Halt production on a control order.
     */
    public ProductionControlOrderDTO haltProduction(Long id, String reason) {
        ProductionControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        order.setStatus("HALTED");
        order.setOperatorNotes("Halted: " + reason);

        ProductionControlOrder updated = repository.save(order);
        logger.warn("Halted production on control order {}: {}", order.getControlOrderNumber(), reason);

        return mapToDTO(updated);
    }

    /**
     * Update operator notes.
     */
    public ProductionControlOrderDTO updateOperatorNotes(Long id, String notes) {
        ProductionControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        order.setOperatorNotes(notes);
        ProductionControlOrder updated = repository.save(order);

        return mapToDTO(updated);
    }

    /**
     * Update defect information.
     */
    public ProductionControlOrderDTO updateDefects(Long id, Integer defectsFound, Integer defectsReworked, Boolean reworkRequired) {
        ProductionControlOrder order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + id));

        order.setDefectsFound(defectsFound);
        order.setDefectsReworked(defectsReworked);
        order.setReworkRequired(reworkRequired);

        ProductionControlOrder updated = repository.save(order);
        logger.info("Updated defect info for control order {}: found={}, reworked={}", 
                order.getControlOrderNumber(), defectsFound, defectsReworked);

        return mapToDTO(updated);
    }

    /**
     * Request parts/supplies for this production control order.
     * Creates a SupplyOrder that will be sent to the Parts Supply Warehouse.
     */
    public SupplyOrderDTO requestSupplies(
            Long controlOrderId,
            List<SupplyOrderItemDTO> requiredParts,
            LocalDateTime neededBy,
            String notes) {
        
        ProductionControlOrder order = repository.findById(controlOrderId)
                .orElseThrow(() -> new RuntimeException("Control order not found: " + controlOrderId));

        return supplyOrderService.createSupplyOrder(
                controlOrderId,
                "PRODUCTION",
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
        return "PCO-" + String.format("%04d", count + 1);
    }

    /**
     * Map entity to DTO.
     */
    private ProductionControlOrderDTO mapToDTO(ProductionControlOrder order) {
        return ProductionControlOrderDTO.builder()
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
                .productionInstructions(order.getProductionInstructions())
                .qualityCheckpoints(order.getQualityCheckpoints())
                .safetyProcedures(order.getSafetyProcedures())
                .estimatedDurationMinutes(order.getEstimatedDurationMinutes())
                .actualDurationMinutes(order.getActualDurationMinutes())
                .defectsFound(order.getDefectsFound())
                .defectsReworked(order.getDefectsReworked())
                .reworkRequired(order.getReworkRequired())
                .reworkNotes(order.getReworkNotes())
                .operatorNotes(order.getOperatorNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
}
