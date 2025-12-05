package io.life.order.controller;

import io.life.order.dto.AssemblyControlOrderDTO;
import io.life.order.service.AssemblyControlOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Final Assembly Station (Assembly Workstation).
 * Handles assembly control orders assigned to final assembly and product completion.
 * This is the final assembly stage before products ship to customers.
 * Completion credits stock to Plant Warehouse instead of Modules Supermarket.
 */
@RestController
@RequestMapping("/api/assembly/final-assembly")
@CrossOrigin(origins = "*", maxAge = 3600)
public class FinalAssemblyStationController {

    private final AssemblyControlOrderService assemblyControlOrderService;

    public FinalAssemblyStationController(AssemblyControlOrderService assemblyControlOrderService) {
        this.assemblyControlOrderService = assemblyControlOrderService;
    }

    /**
     * Get all final assembly orders for a workstation
     */
    @GetMapping("/workstation/{workstationId}")
    public ResponseEntity<List<AssemblyControlOrderDTO>> getOrdersByWorkstation(
            @PathVariable Long workstationId) {
        List<AssemblyControlOrderDTO> orders = assemblyControlOrderService.getOrdersByWorkstation(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get active final assembly orders (IN_PROGRESS) for a workstation
     */
    @GetMapping("/workstation/{workstationId}/active")
    public ResponseEntity<List<AssemblyControlOrderDTO>> getActiveOrdersByWorkstation(
            @PathVariable Long workstationId) {
        List<AssemblyControlOrderDTO> orders = assemblyControlOrderService.getActiveOrdersByWorkstation(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get unassigned final assembly orders (status=ASSIGNED) for a workstation
     */
    @GetMapping("/workstation/{workstationId}/unassigned")
    public ResponseEntity<List<AssemblyControlOrderDTO>> getUnassignedOrders(
            @PathVariable Long workstationId) {
        List<AssemblyControlOrderDTO> orders = assemblyControlOrderService.getUnassignedOrders(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Get final assembly order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AssemblyControlOrderDTO> getOrderById(@PathVariable Long id) {
        Optional<AssemblyControlOrderDTO> order = assemblyControlOrderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get final assembly order by control order number
     */
    @GetMapping("/number/{controlOrderNumber}")
    public ResponseEntity<AssemblyControlOrderDTO> getOrderByNumber(
            @PathVariable String controlOrderNumber) {
        Optional<AssemblyControlOrderDTO> order = assemblyControlOrderService.getOrderByNumber(controlOrderNumber);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Start final assembly work
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
     * Complete final assembly with SimAL and inventory integration.
     * Updates status to COMPLETED, notifies SimAL, and credits Plant Warehouse (not Modules Supermarket).
     * This is special because final assembly completes the entire product.
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<AssemblyControlOrderDTO> completeAssembly(@PathVariable Long id) {
        try {
            AssemblyControlOrderDTO order = assemblyControlOrderService.completeFinalAssembly(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Halt final assembly work
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
     * Update operator notes for a final assembly order
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
