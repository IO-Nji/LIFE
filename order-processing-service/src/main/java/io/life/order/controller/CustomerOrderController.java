package io.life.order.controller;

import io.life.order.dto.CustomerOrderDTO;
import io.life.order.service.CustomerOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer-orders")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    public CustomerOrderController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @PostMapping
    public ResponseEntity<CustomerOrderDTO> createOrder(@RequestBody CustomerOrderDTO orderDTO) {
        CustomerOrderDTO createdOrder = customerOrderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerOrderDTO> getOrderById(@PathVariable Long id) {
        Optional<CustomerOrderDTO> order = customerOrderService.getOrderById(id);
        return order.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<CustomerOrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        Optional<CustomerOrderDTO> order = customerOrderService.getOrderByNumber(orderNumber);
        return order.map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/workstation/{workstationId}")
    public ResponseEntity<List<CustomerOrderDTO>> getOrdersByWorkstationId(@PathVariable Long workstationId) {
        List<CustomerOrderDTO> orders = customerOrderService.getOrdersByWorkstationId(workstationId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<CustomerOrderDTO>> getOrdersByStatus(@PathVariable String status) {
        List<CustomerOrderDTO> orders = customerOrderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CustomerOrderDTO> updateOrderStatus(
        @PathVariable Long id,
        @RequestParam String status) {
        CustomerOrderDTO updatedOrder = customerOrderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        customerOrderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
