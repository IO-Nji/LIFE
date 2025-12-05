package io.life.order.controller;

import io.life.order.dto.SupplyOrderDTO;
import io.life.order.service.SupplyOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Supply Order management.
 * Handles supply orders for Parts Supply Warehouse.
 */
@RestController
@RequestMapping("/api/supply-orders")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SupplyOrderController {

    private final SupplyOrderService supplyOrderService;

    public SupplyOrderController(SupplyOrderService supplyOrderService) {
        this.supplyOrderService = supplyOrderService;
    }

    /**
     * Get all supply orders for the Parts Supply Warehouse dashboard.
     * Optional status filter: PENDING, IN_PROGRESS, FULFILLED, REJECTED, CANCELLED
     */
    @GetMapping("/warehouse")
    public ResponseEntity<List<SupplyOrderDTO>> getWarehouseOrders(
            @RequestParam(required = false) String status) {
        List<SupplyOrderDTO> orders = supplyOrderService.getOrdersForSupplyWarehouse(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get all supply orders for a specific workstation (requesting workstation).
     * Used by production/assembly control operators to see their pending supplies.
     */
    @GetMapping("/workstation/{workstationId}")
    public ResponseEntity<List<SupplyOrderDTO>> getOrdersByWorkstation(
            @PathVariable Long workstationId,
            @RequestParam(required = false) String status) {
        List<SupplyOrderDTO> orders = supplyOrderService.getOrdersByRequestingWorkstation(workstationId, status);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get a specific supply order by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplyOrderDTO> getSupplyOrder(@PathVariable Long id) {
        SupplyOrderDTO order = supplyOrderService.getSupplyOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Get supply orders for a specific source control order.
     */
    @GetMapping("/source/{controlOrderId}")
    public ResponseEntity<List<SupplyOrderDTO>> getBySourceControlOrder(
            @PathVariable Long controlOrderId,
            @RequestParam String type) { // PRODUCTION or ASSEMBLY
        List<SupplyOrderDTO> orders = supplyOrderService.getBySourceControlOrder(controlOrderId, type);
        return ResponseEntity.ok(orders);
    }

    /**
     * Fulfill a supply order.
     * Changes status from PENDING/IN_PROGRESS to FULFILLED.
     * Debits from inventory-service.
     */
    @PutMapping("/{id}/fulfill")
    public ResponseEntity<SupplyOrderDTO> fulfillSupplyOrder(@PathVariable Long id) {
        SupplyOrderDTO order = supplyOrderService.fulfillSupplyOrder(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Update supply order status.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<SupplyOrderDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String newStatus = request.get("status");
        SupplyOrderDTO order = supplyOrderService.updateStatus(id, newStatus);
        return ResponseEntity.ok(order);
    }

    /**
     * Reject a supply order due to insufficient stock or other reason.
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<SupplyOrderDTO> rejectSupplyOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        SupplyOrderDTO order = supplyOrderService.rejectSupplyOrder(id, reason);
        return ResponseEntity.ok(order);
    }

    /**
     * Cancel a supply order.
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SupplyOrderDTO> cancelSupplyOrder(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String reason = request.get("reason");
        SupplyOrderDTO order = supplyOrderService.cancelSupplyOrder(id, reason);
        return ResponseEntity.ok(order);
    }
}
