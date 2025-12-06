package io.life.order.controller;

import io.life.order.dto.ProductionOrderDTO;
import io.life.order.service.ProductionOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for ProductionOrder management.
 * Exposes endpoints for creating, retrieving, and updating production orders.
 * Production orders are created when WarehouseOrders cannot be fully fulfilled (Scenario 3).
 */
@RestController
@RequestMapping("/api/production-orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;

    public ProductionOrderController(ProductionOrderService productionOrderService) {
        this.productionOrderService = productionOrderService;
    }

    /**
     * Get all production orders
     */
    @GetMapping
    public ResponseEntity<List<ProductionOrderDTO>> getAllProductionOrders() {
        List<ProductionOrderDTO> orders = productionOrderService.getAllProductionOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * Get production order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductionOrderDTO> getProductionOrderById(@PathVariable Long id) {
        Optional<ProductionOrderDTO> order = productionOrderService.getProductionOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get production order by production order number
     */
    @GetMapping("/number/{productionOrderNumber}")
    public ResponseEntity<ProductionOrderDTO> getProductionOrderByNumber(
            @PathVariable String productionOrderNumber) {
        Optional<ProductionOrderDTO> order = productionOrderService.getProductionOrderByNumber(productionOrderNumber);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get all production orders from a customer order
     */
    @GetMapping("/customer/{sourceCustomerOrderId}")
    public ResponseEntity<List<ProductionOrderDTO>> getProductionOrdersByCustomerOrder(
            @PathVariable Long sourceCustomerOrderId) {
        List<ProductionOrderDTO> orders = productionOrderService.getProductionOrdersByCustomerOrder(sourceCustomerOrderId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get all production orders from a warehouse order
     */
    @GetMapping("/warehouse/{sourceWarehouseOrderId}")
    public ResponseEntity<List<ProductionOrderDTO>> getProductionOrdersByWarehouseOrder(
            @PathVariable Long sourceWarehouseOrderId) {
        List<ProductionOrderDTO> orders = productionOrderService.getProductionOrdersByWarehouseOrder(sourceWarehouseOrderId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get production orders by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductionOrderDTO>> getProductionOrdersByStatus(
            @PathVariable String status) {
        List<ProductionOrderDTO> orders = productionOrderService.getProductionOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get production orders by priority
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<ProductionOrderDTO>> getProductionOrdersByPriority(
            @PathVariable String priority) {
        List<ProductionOrderDTO> orders = productionOrderService.getProductionOrdersByPriority(priority);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get production orders by workstation (created by)
     */
    @GetMapping("/workstation/{createdByWorkstationId}")
    public ResponseEntity<List<ProductionOrderDTO>> getProductionOrdersByWorkstation(
            @PathVariable Long createdByWorkstationId) {
        List<ProductionOrderDTO> orders = productionOrderService.getProductionOrdersByWorkstation(createdByWorkstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get production orders assigned to a workstation (for assembly/completion)
     */
    @GetMapping("/assigned/{assignedWorkstationId}")
    public ResponseEntity<List<ProductionOrderDTO>> getProductionOrdersByAssignedWorkstation(
            @PathVariable Long assignedWorkstationId) {
        List<ProductionOrderDTO> orders = productionOrderService.getProductionOrdersByAssignedWorkstation(assignedWorkstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Create a standalone production order
     */
    @PostMapping
    public ResponseEntity<ProductionOrderDTO> createStandaloneProductionOrder(
            @RequestBody CreateProductionOrderRequest request) {
        ProductionOrderDTO order = productionOrderService.createStandaloneProductionOrder(
                request.getSourceCustomerOrderId(),
                request.getPriority(),
                request.getDueDate(),
                request.getNotes(),
                request.getCreatedByWorkstationId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Update production order status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductionOrderDTO> updateProductionOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request) {
        ProductionOrderDTO order = productionOrderService.updateProductionOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(order);
    }

    /**
     * Link production order to SimAL schedule
     */
    @PatchMapping("/{id}/link-simal")
    public ResponseEntity<ProductionOrderDTO> linkToSimalSchedule(
            @PathVariable Long id,
            @RequestBody LinkSimalRequest request) {
        ProductionOrderDTO order = productionOrderService.linkToSimalSchedule(
                id,
                request.getSimalScheduleId(),
                request.getEstimatedDuration(),
                request.getExpectedCompletionTime()
        );
        return ResponseEntity.ok(order);
    }

    /**
     * Complete production order
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ProductionOrderDTO> completeProductionOrder(@PathVariable Long id) {
        ProductionOrderDTO order = productionOrderService.completeProductionOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Cancel production order
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ProductionOrderDTO> cancelProductionOrder(@PathVariable Long id) {
        ProductionOrderDTO order = productionOrderService.cancelProductionOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Request class for creating production orders
     */
    public static class CreateProductionOrderRequest {
        private Long sourceCustomerOrderId;
        private String priority;
        private LocalDateTime dueDate;
        private String notes;
        private Long createdByWorkstationId;

        // Getters and Setters
        public Long getSourceCustomerOrderId() { return sourceCustomerOrderId; }
        public void setSourceCustomerOrderId(Long sourceCustomerOrderId) { this.sourceCustomerOrderId = sourceCustomerOrderId; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public LocalDateTime getDueDate() { return dueDate; }
        public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public Long getCreatedByWorkstationId() { return createdByWorkstationId; }
        public void setCreatedByWorkstationId(Long createdByWorkstationId) { this.createdByWorkstationId = createdByWorkstationId; }
    }

    /**
     * Request class for updating production order status
     */
    public static class UpdateStatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * Request class for linking to SimAL schedule
     */
    public static class LinkSimalRequest {
        private String simalScheduleId;
        private Integer estimatedDuration;
        private LocalDateTime expectedCompletionTime;

        public String getSimalScheduleId() { return simalScheduleId; }
        public void setSimalScheduleId(String simalScheduleId) { this.simalScheduleId = simalScheduleId; }

        public Integer getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }

        public LocalDateTime getExpectedCompletionTime() { return expectedCompletionTime; }
        public void setExpectedCompletionTime(LocalDateTime expectedCompletionTime) { this.expectedCompletionTime = expectedCompletionTime; }
    }
}
