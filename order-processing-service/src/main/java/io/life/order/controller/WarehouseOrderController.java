package io.life.order.controller;

import io.life.order.dto.WarehouseOrderDTO;
import io.life.order.service.WarehouseOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/warehouse-orders")
public class WarehouseOrderController {

    private final WarehouseOrderService warehouseOrderService;

    public WarehouseOrderController(WarehouseOrderService warehouseOrderService) {
        this.warehouseOrderService = warehouseOrderService;
    }

    /**
     * GET /api/warehouse-orders
     * Retrieve all warehouse orders (admin only)
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<WarehouseOrderDTO>> getAllWarehouseOrders() {
        List<WarehouseOrderDTO> orders = warehouseOrderService.getAllWarehouseOrders();
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/warehouse-orders/{id}
     * Retrieve a specific warehouse order by ID
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODULES_SUPERMARKET')")
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseOrderDTO> getWarehouseOrderById(@PathVariable Long id) {
        Optional<WarehouseOrderDTO> order = warehouseOrderService.getWarehouseOrderById(id);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * GET /api/warehouse-orders/workstation/{workstationId}
     * Retrieve warehouse orders for a specific workstation
     * Modules Supermarket (8) can retrieve orders that need to be fulfilled
     */
    @PreAuthorize("hasRole('MODULES_SUPERMARKET')")
    @GetMapping("/workstation/{workstationId}")
    public ResponseEntity<List<WarehouseOrderDTO>> getWarehouseOrdersByWorkstationId(@PathVariable Long workstationId) {
        List<WarehouseOrderDTO> orders = warehouseOrderService.getWarehouseOrdersByFulfillingWorkstationId(workstationId);
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/warehouse-orders/status/{status}
     * Retrieve warehouse orders by status
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODULES_SUPERMARKET')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<WarehouseOrderDTO>> getWarehouseOrdersByStatus(@PathVariable String status) {
        List<WarehouseOrderDTO> orders = warehouseOrderService.getWarehouseOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    /**
     * PUT /api/warehouse-orders/{id}/fulfill-modules
     * Fulfill a warehouse order at Modules Supermarket
     * Updates warehouse order and related inventory
     */
    @PreAuthorize("hasRole('MODULES_SUPERMARKET')")
    @PutMapping("/{id}/fulfill-modules")
    public ResponseEntity<WarehouseOrderDTO> fulfillWarehouseOrder(@PathVariable Long id) {
        WarehouseOrderDTO fulfilledOrder = warehouseOrderService.fulfillWarehouseOrder(id);
        return ResponseEntity.ok(fulfilledOrder);
    }

    /**
     * PATCH /api/warehouse-orders/{id}/status
     * Update warehouse order status
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODULES_SUPERMARKET')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<WarehouseOrderDTO> updateWarehouseOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        WarehouseOrderDTO updatedOrder = warehouseOrderService.updateWarehouseOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }
}
