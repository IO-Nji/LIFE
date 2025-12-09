package io.life.order.controller;

import io.life.order.dto.AssemblyControlOrderDTO;
import io.life.order.service.AssemblyControlOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Gear Assembly Station (Assembly Workstation).
 * Handles assembly control orders assigned to gear assembly operations.
 * Specializes in gear component assembly for drill products.
 */
@RestController
@RequestMapping("/api/assembly/gear-assembly")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GearAssemblyStationController {

    private final AssemblyControlOrderService assemblyControlOrderService;

    public GearAssemblyStationController(AssemblyControlOrderService assemblyControlOrderService) {
        this.assemblyControlOrderService = assemblyControlOrderService;
    }

    /**
     * Get all gear assembly orders for a workstation
     */
    @GetMapping("/workstation/{workstationId}")
    public ResponseEntity<List<AssemblyControlOrderDTO>> getOrdersByWorkstation(
            @PathVariable Long workstationId) {
        List<AssemblyControlOrderDTO> orders = assemblyControlOrderService.getOrdersByWorkstation(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get active gear assembly orders (IN_PROGRESS) for a workstation
     */
    @GetMapping("/workstation/{workstationId}/active")
    public ResponseEntity<List<AssemblyControlOrderDTO>> getActiveOrdersByWorkstation(
            @PathVariable Long workstationId) {
        List<AssemblyControlOrderDTO> orders = assemblyControlOrderService.getActiveOrdersByWorkstation(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get unassigned gear assembly orders (status=ASSIGNED) for a workstation
     */
    @GetMapping("/workstation/{workstationId}/unassigned")
    public ResponseEntity<List<AssemblyControlOrderDTO>> getUnassignedOrders(
            @PathVariable Long workstationId) {
        List<AssemblyControlOrderDTO> orders = assemblyControlOrderService.getUnassignedOrders(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get gear assembly order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssemblyControlOrderDTO> getOrderById(@PathVariable Long id) {
        Optional<AssemblyControlOrderDTO> order = assemblyControlOrderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get gear assembly order by control order number
     */
    @GetMapping("/number/{controlOrderNumber}")
    public ResponseEntity<AssemblyControlOrderDTO> getOrderByNumber(
            @PathVariable String controlOrderNumber) {
        Optional<AssemblyControlOrderDTO> order = assemblyControlOrderService.getOrderByNumber(controlOrderNumber);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Start gear assembly work
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<AssemblyControlOrderDTO> startAssembly(@PathVariable Long id) {
        try {
            AssemblyControlOrderDTO order = assemblyControlOrderService.startAssembly(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Complete gear assembly with SimAL and inventory integration.
     * Updates status to COMPLETED, notifies SimAL, and credits Modules Supermarket.
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<AssemblyControlOrderDTO> completeAssembly(@PathVariable Long id) {
        try {
            AssemblyControlOrderDTO order = assemblyControlOrderService.completeAssemblyProduction(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Halt gear assembly work
     */
    @PostMapping("/{id}/halt")
    public ResponseEntity<AssemblyControlOrderDTO> haltAssembly(@PathVariable Long id) {
        try {
            AssemblyControlOrderDTO order = assemblyControlOrderService.haltAssembly(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Update operator notes for a gear assembly order
     */
    @PatchMapping("/{id}/notes")
    public ResponseEntity<AssemblyControlOrderDTO> updateNotes(
            @PathVariable Long id,
            @RequestBody NotesRequest request) {
        try {
            AssemblyControlOrderDTO order = assemblyControlOrderService.updateOperatorNotes(id, request.getNotes());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    public static class NotesRequest {
        private String notes;

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }
    }
}
