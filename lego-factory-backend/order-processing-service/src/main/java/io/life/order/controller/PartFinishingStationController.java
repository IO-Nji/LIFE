package io.life.order.controller;

import io.life.order.dto.ProductionControlOrderDTO;
import io.life.order.service.ProductionControlOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Part Finishing Station (Manufacturing Workstation).
 * Handles production control orders assigned to part finishing and quality control.
 * These orders involve final finishing, polishing, and quality inspection.
 */
@RestController
@RequestMapping("/api/manufacturing/part-finishing")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PartFinishingStationController {

    private final ProductionControlOrderService productionControlOrderService;

    public PartFinishingStationController(ProductionControlOrderService productionControlOrderService) {
        this.productionControlOrderService = productionControlOrderService;
    }

    /**
     * Get all part finishing orders for a workstation
     */
    @GetMapping("/workstation/{workstationId}")
    public ResponseEntity<List<ProductionControlOrderDTO>> getOrdersByWorkstation(
            @PathVariable Long workstationId) {
        List<ProductionControlOrderDTO> orders = productionControlOrderService.getOrdersByWorkstation(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get active part finishing orders (IN_PROGRESS) for a workstation
     */
    @GetMapping("/workstation/{workstationId}/active")
    public ResponseEntity<List<ProductionControlOrderDTO>> getActiveOrdersByWorkstation(
            @PathVariable Long workstationId) {
        List<ProductionControlOrderDTO> orders = productionControlOrderService.getActiveOrdersByWorkstation(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get unassigned part finishing orders (status=ASSIGNED) for a workstation
     */
    @GetMapping("/workstation/{workstationId}/unassigned")
    public ResponseEntity<List<ProductionControlOrderDTO>> getUnassignedOrders(
            @PathVariable Long workstationId) {
        List<ProductionControlOrderDTO> orders = productionControlOrderService.getUnassignedOrders(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get part finishing order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductionControlOrderDTO> getOrderById(@PathVariable Long id) {
        Optional<ProductionControlOrderDTO> order = productionControlOrderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get part finishing order by control order number
     */
    @GetMapping("/number/{controlOrderNumber}")
    public ResponseEntity<ProductionControlOrderDTO> getOrderByNumber(
            @PathVariable String controlOrderNumber) {
        Optional<ProductionControlOrderDTO> order = productionControlOrderService.getOrderByNumber(controlOrderNumber);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Start part finishing work
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<ProductionControlOrderDTO> startProduction(@PathVariable Long id) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.startProduction(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Complete part finishing work with SimAL and inventory integration.
     * Updates status to COMPLETED, notifies SimAL, and credits Modules Supermarket.
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<ProductionControlOrderDTO> completeProduction(@PathVariable Long id) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.completeManufacturingProduction(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Halt part finishing work
     */
    @PostMapping("/{id}/halt")
    public ResponseEntity<ProductionControlOrderDTO> haltProduction(
            @PathVariable Long id,
            @RequestBody HaltRequest request) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.haltProduction(id, request.getReason());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Update operator notes for part finishing
     */
    @PatchMapping("/{id}/notes")
    public ResponseEntity<ProductionControlOrderDTO> updateNotes(
            @PathVariable Long id,
            @RequestBody NotesRequest request) {
        try {
            ProductionControlOrderDTO order = productionControlOrderService.updateOperatorNotes(id, request.getNotes());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
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
}
