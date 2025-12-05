package io.life.order.controller;

import io.life.order.dto.ProductionControlOrderDTO;
import io.life.order.service.ProductionControlOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for ProductionControlOrder management.
 * Exposes endpoints for Production Control workstations to view and manage their assigned orders.
 */
@RestController
@RequestMapping("/api/production-control-orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductionControlOrderController {

    private final ProductionControlOrderService productionControlOrderService;

    public ProductionControlOrderController(ProductionControlOrderService productionControlOrderService) {
        this.productionControlOrderService = productionControlOrderService;
    }

    /**
     * Get all control orders for a workstation
     */
    @GetMapping("/workstation/{workstationId}")
    public ResponseEntity<List<ProductionControlOrderDTO>> getOrdersByWorkstation(
            @PathVariable Long workstationId) {
        List<ProductionControlOrderDTO> orders = productionControlOrderService.getOrdersByWorkstation(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get active (in progress) control orders for a workstation
     */
    @GetMapping("/workstation/{workstationId}/active")
    public ResponseEntity<List<ProductionControlOrderDTO>> getActiveOrdersByWorkstation(
            @PathVariable Long workstationId) {
        List<ProductionControlOrderDTO> orders = productionControlOrderService.getActiveOrdersByWorkstation(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get unassigned (status=ASSIGNED) control orders for a workstation
     */
    @GetMapping("/workstation/{workstationId}/unassigned")
    public ResponseEntity<List<ProductionControlOrderDTO>> getUnassignedOrders(
            @PathVariable Long workstationId) {
        List<ProductionControlOrderDTO> orders = productionControlOrderService.getUnassignedOrders(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get control order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductionControlOrderDTO> getOrderById(@PathVariable Long id) {
        Optional<ProductionControlOrderDTO> order = productionControlOrderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get control order by control order number
     */
    @GetMapping("/number/{controlOrderNumber}")
    public ResponseEntity<ProductionControlOrderDTO> getOrderByNumber(
            @PathVariable String controlOrderNumber) {
        Optional<ProductionControlOrderDTO> order = productionControlOrderService.getOrderByNumber(controlOrderNumber);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Start production on a control order
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<ProductionControlOrderDTO> startProduction(@PathVariable Long id) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.startProduction(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Complete production on a control order
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ProductionControlOrderDTO> completeProduction(@PathVariable Long id) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.completeProduction(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Halt production on a control order
     */
    @PostMapping("/{id}/halt")
    public ResponseEntity<ProductionControlOrderDTO> haltProduction(
            @PathVariable Long id,
            @RequestBody HaltRequest request) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.haltProduction(id, request.getReason());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update operator notes
     */
    @PatchMapping("/{id}/notes")
    public ResponseEntity<ProductionControlOrderDTO> updateNotes(
            @PathVariable Long id,
            @RequestBody NotesRequest request) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.updateOperatorNotes(id, request.getNotes());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update defect information
     */
    @PatchMapping("/{id}/defects")
    public ResponseEntity<ProductionControlOrderDTO> updateDefects(
            @PathVariable Long id,
            @RequestBody DefectsRequest request) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.updateDefects(
                    id,
                    request.getDefectsFound(),
                    request.getDefectsReworked(),
                    request.getReworkRequired()
            );
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Request class for halting production
     */
    public static class HaltRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    /**
     * Request class for operator notes
     */
    public static class NotesRequest {
        private String notes;

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    /**
     * Request class for defect information
     */
    public static class DefectsRequest {
        private Integer defectsFound;
        private Integer defectsReworked;
        private Boolean reworkRequired;

        public Integer getDefectsFound() { return defectsFound; }
        public void setDefectsFound(Integer defectsFound) { this.defectsFound = defectsFound; }

        public Integer getDefectsReworked() { return defectsReworked; }
        public void setDefectsReworked(Integer defectsReworked) { this.defectsReworked = defectsReworked; }

        public Boolean getReworkRequired() { return reworkRequired; }
        public void setReworkRequired(Boolean reworkRequired) { this.reworkRequired = reworkRequired; }
    }

    /**
     * Create a production control order from SimAL schedule data.
     * This endpoint is called by the SimAL integration service when a production
     * order has been scheduled and assigned to a production workstation.
     */
    @PostMapping
    public ResponseEntity<ProductionControlOrderDTO> createControlOrder(
            @RequestBody CreateControlOrderRequest request) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime targetStart = LocalDateTime.parse(request.getTargetStartTime(), formatter);
            LocalDateTime targetCompletion = LocalDateTime.parse(request.getTargetCompletionTime(), formatter);

            ProductionControlOrderDTO order = productionControlOrderService.createControlOrder(
                    request.getSourceProductionOrderId(),
                    request.getAssignedWorkstationId(),
                    request.getSimalScheduleId(),
                    request.getPriority(),
                    targetStart,
                    targetCompletion,
                    request.getProductionInstructions(),
                    request.getQualityCheckpoints(),
                    "Standard safety procedures apply",
                    120  // Default 2-hour estimate
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Request class for creating a production control order from SimAL
     */
    public static class CreateControlOrderRequest {
        private Long sourceProductionOrderId;
        private Long assignedWorkstationId;
        private String simalScheduleId;
        private String targetStartTime;
        private String targetCompletionTime;
        private String priority;
        private String productionInstructions;
        private String qualityCheckpoints;

        // Getters and setters
        public Long getSourceProductionOrderId() { return sourceProductionOrderId; }
        public void setSourceProductionOrderId(Long sourceProductionOrderId) {
            this.sourceProductionOrderId = sourceProductionOrderId;
        }

        public Long getAssignedWorkstationId() { return assignedWorkstationId; }
        public void setAssignedWorkstationId(Long assignedWorkstationId) {
            this.assignedWorkstationId = assignedWorkstationId;
        }

        public String getSimalScheduleId() { return simalScheduleId; }
        public void setSimalScheduleId(String simalScheduleId) { this.simalScheduleId = simalScheduleId; }

        public String getTargetStartTime() { return targetStartTime; }
        public void setTargetStartTime(String targetStartTime) { this.targetStartTime = targetStartTime; }

        public String getTargetCompletionTime() { return targetCompletionTime; }
        public void setTargetCompletionTime(String targetCompletionTime) {
            this.targetCompletionTime = targetCompletionTime;
        }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getProductionInstructions() { return productionInstructions; }
        public void setProductionInstructions(String productionInstructions) {
            this.productionInstructions = productionInstructions;
        }

        public String getQualityCheckpoints() { return qualityCheckpoints; }
        public void setQualityCheckpoints(String qualityCheckpoints) {
            this.qualityCheckpoints = qualityCheckpoints;
        }
    }
}
