package io.life.order.service;

import io.life.order.dto.CustomerOrderDTO;
import io.life.order.entity.CustomerOrder;
import io.life.order.entity.OrderItem;
import io.life.order.entity.WarehouseOrder;
import io.life.order.entity.WarehouseOrderItem;
import io.life.order.repository.CustomerOrderRepository;
import io.life.order.repository.WarehouseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Fulfillment Service implementing the 4 scenarios for order processing.
 *
 * Scenario 1: Direct Fulfillment (All items available locally)
 *   - Check inventory at the order's workstation
 *   - If all items available: Deduct from local inventory, mark order as COMPLETED
 *
 * Scenario 2: Warehouse Order (Items not available locally, need warehouse supply)
 *   - Create WarehouseOrder for missing items
 *   - Mark CustomerOrder as PROCESSING (waiting for warehouse supply)
 *   - When warehouse supplies: Update inventory and complete order
 *
 * Scenario 3: Modules Supermarket (Partially available, request modules)
 *   - For items available: Direct fulfill locally
 *   - For missing modules: Create request to Modules Supermarket
 *   - Mark order as PROCESSING until all items arrive
 *
 * Scenario 4: Production Planning (Complex/Custom items)
 *   - For items requiring production: Route to Production Planning
 *   - Standard items: Process locally if available
 *   - Mark order as PROCESSING with production status
 */
@Service
public class FulfillmentService {

    private static final Logger logger = LoggerFactory.getLogger(FulfillmentService.class);
    private static final Long MODULES_SUPERMARKET_WORKSTATION_ID = 8L;

    private final CustomerOrderRepository customerOrderRepository;
    private final WarehouseOrderRepository warehouseOrderRepository;
    private final InventoryService inventoryService;

    public FulfillmentService(CustomerOrderRepository customerOrderRepository,
                            WarehouseOrderRepository warehouseOrderRepository,
                            InventoryService inventoryService) {
        this.customerOrderRepository = customerOrderRepository;
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.inventoryService = inventoryService;
    }

    /**
     * Process fulfillment for a customer order.
     * Determines which scenario applies and executes the appropriate logic.
     */
    @Transactional
    public CustomerOrderDTO fulfillOrder(Long orderId) {
        Optional<CustomerOrder> orderOpt = customerOrderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Order not found: " + orderId);
        }

        CustomerOrder order = orderOpt.get();
        logger.info("Starting fulfillment for order {} ({})", order.getId(), order.getOrderNumber());

        // Check which items are available locally
        boolean allItemsAvailable = order.getOrderItems().stream()
                .allMatch(item -> inventoryService.checkStock(order.getWorkstationId(), item.getItemId(), item.getQuantity()));

        if (allItemsAvailable) {
            // Scenario 1: Direct Fulfillment
            return scenario1_DirectFulfillment(order);
        } else {
            // Check if any items are available
            boolean anyItemsAvailable = order.getOrderItems().stream()
                    .anyMatch(item -> inventoryService.checkStock(order.getWorkstationId(), item.getItemId(), item.getQuantity()));

            if (anyItemsAvailable) {
                // Scenario 3: Modules Supermarket (partial availability)
                return scenario3_ModulesSupermarket(order);
            } else {
                // Scenario 2: Warehouse Order (nothing available locally)
                return scenario2_WarehouseOrder(order);
            }
        }
    }

    /**
     * Scenario 1: Direct Fulfillment
     * All items are available at the order's workstation.
     * Deduct inventory and complete the order immediately.
     */
    private CustomerOrderDTO scenario1_DirectFulfillment(CustomerOrder order) {
        logger.info("Scenario 1: Direct Fulfillment for order {}", order.getOrderNumber());

        // Deduct all items from inventory
        boolean allUpdatesSuccessful = order.getOrderItems().stream()
                .allMatch(item -> inventoryService.updateStock(order.getWorkstationId(), item.getItemId(), item.getQuantity()));

        if (allUpdatesSuccessful) {
            order.setStatus("COMPLETED");
            logger.info("Order {} fulfilled directly. Inventory updated.", order.getOrderNumber());
        } else {
            order.setStatus("CANCELLED");
            logger.warn("Order {} fulfillment failed during inventory update.", order.getOrderNumber());
        }

        return mapToDTO(customerOrderRepository.save(order));
    }

    /**
     * Scenario 2: Warehouse Order
     * No items available locally. Create a warehouse order for all items.
     * Order status changes to PROCESSING.
     */
    private CustomerOrderDTO scenario2_WarehouseOrder(CustomerOrder order) {
        logger.info("Scenario 2: Warehouse Order for order {}", order.getOrderNumber());

        // Create a new WarehouseOrder
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        warehouseOrder.setWarehouseOrderNumber("WO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        warehouseOrder.setSourceCustomerOrderId(order.getId());
        warehouseOrder.setRequestingWorkstationId(order.getWorkstationId()); // Plant Warehouse (7)
        warehouseOrder.setFulfillingWorkstationId(MODULES_SUPERMARKET_WORKSTATION_ID); // Modules Supermarket (8)
        warehouseOrder.setOrderDate(LocalDateTime.now());
        warehouseOrder.setStatus("PENDING");
        warehouseOrder.setTriggerScenario("SCENARIO_2");
        warehouseOrder.setNotes("Auto-generated from customer order " + order.getOrderNumber());

        // Add items to warehouse order
        List<WarehouseOrderItem> warehouseOrderItems = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            WarehouseOrderItem woItem = new WarehouseOrderItem();
            woItem.setWarehouseOrder(warehouseOrder);
            woItem.setItemId(item.getItemId());
            woItem.setItemName("Item-" + item.getItemId()); // Item name from item ID
            woItem.setRequestedQuantity(item.getQuantity());
            woItem.setFulfilledQuantity(0);
            woItem.setItemType(item.getItemType()); // Use the same type from OrderItem
            warehouseOrderItems.add(woItem);
            
            logger.info("  - Warehouse order item: {} qty {}", item.getItemId(), item.getQuantity());
        }
        warehouseOrder.setWarehouseOrderItems(warehouseOrderItems);

        // Persist warehouse order
        warehouseOrderRepository.save(warehouseOrder);
        logger.info("Created warehouse order {} for customer order {}", warehouseOrder.getWarehouseOrderNumber(), order.getOrderNumber());

        // Update customer order status
        order.setStatus("PROCESSING");
        order.setNotes((order.getNotes() != null ? order.getNotes() + " | " : "") + "Scenario 2: Warehouse order " + warehouseOrder.getWarehouseOrderNumber() + " created");

        return mapToDTO(customerOrderRepository.save(order));
    }

    /**
     * Scenario 3: Modules Supermarket
     * Some items available locally, others need to come from Modules Supermarket.
     * Fulfill what's available, request missing items via warehouse order.
     */
    private CustomerOrderDTO scenario3_ModulesSupermarket(CustomerOrder order) {
        logger.info("Scenario 3: Modules Supermarket for order {}", order.getOrderNumber());

        // Create warehouse order for unavailable items
        WarehouseOrder warehouseOrder = new WarehouseOrder();
        warehouseOrder.setWarehouseOrderNumber("WO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        warehouseOrder.setSourceCustomerOrderId(order.getId());
        warehouseOrder.setRequestingWorkstationId(order.getWorkstationId()); // Plant Warehouse (7)
        warehouseOrder.setFulfillingWorkstationId(MODULES_SUPERMARKET_WORKSTATION_ID); // Modules Supermarket (8)
        warehouseOrder.setOrderDate(LocalDateTime.now());
        warehouseOrder.setStatus("PENDING");
        warehouseOrder.setTriggerScenario("SCENARIO_3");
        warehouseOrder.setNotes("Auto-generated from customer order " + order.getOrderNumber() + " (partial fulfillment)");

        List<WarehouseOrderItem> warehouseOrderItems = new ArrayList<>();

        // Fulfill available items and track unavailable ones
        for (OrderItem item : order.getOrderItems()) {
            if (inventoryService.checkStock(order.getWorkstationId(), item.getItemId(), item.getQuantity())) {
                inventoryService.updateStock(order.getWorkstationId(), item.getItemId(), item.getQuantity());
                logger.info("  - Item {} fulfilled from local stock", item.getItemId());
            } else {
                // Add to warehouse order for unavailable items
                WarehouseOrderItem woItem = new WarehouseOrderItem();
                woItem.setWarehouseOrder(warehouseOrder);
                woItem.setItemId(item.getItemId());
                woItem.setItemName("Item-" + item.getItemId());
                woItem.setRequestedQuantity(item.getQuantity());
                woItem.setFulfilledQuantity(0);
                woItem.setItemType(item.getItemType());
                warehouseOrderItems.add(woItem);
                
                logger.info("  - Item {} requested from Modules Supermarket", item.getItemId());
            }
        }

        // Only save warehouse order if there are items to request
        if (!warehouseOrderItems.isEmpty()) {
            warehouseOrder.setWarehouseOrderItems(warehouseOrderItems);
            warehouseOrderRepository.save(warehouseOrder);
            logger.info("Created warehouse order {} for customer order {}", warehouseOrder.getWarehouseOrderNumber(), order.getOrderNumber());
        }

        order.setStatus("PROCESSING");
        String notes = "Scenario 3: Partial fulfillment from local + Modules Supermarket";
        if (!warehouseOrderItems.isEmpty()) {
            notes += " (warehouse order: " + warehouseOrder.getWarehouseOrderNumber() + ")";
        }
        order.setNotes((order.getNotes() != null ? order.getNotes() + " | " : "") + notes);

        return mapToDTO(customerOrderRepository.save(order));
    }

    /**
     * Scenario 4: Production Planning (Future enhancement)
     * Complex items that require production routing.
     */
    private CustomerOrderDTO scenario4_ProductionPlanning(CustomerOrder order) {
        logger.info("Scenario 4: Production Planning for order {}", order.getOrderNumber());

        // Fulfill available items
        for (OrderItem item : order.getOrderItems()) {
            if (inventoryService.checkStock(order.getWorkstationId(), item.getItemId(), item.getQuantity())) {
                inventoryService.updateStock(order.getWorkstationId(), item.getItemId(), item.getQuantity());
            }
        }

        order.setStatus("PROCESSING");
        order.setNotes((order.getNotes() != null ? order.getNotes() + " | " : "") + "Scenario 4: Routed to Production Planning for custom items");

        return mapToDTO(customerOrderRepository.save(order));
    }

    private CustomerOrderDTO mapToDTO(CustomerOrder order) {
        CustomerOrderDTO dto = new CustomerOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setWorkstationId(order.getWorkstationId());
        dto.setNotes(order.getNotes());
        // Map orderItems if needed
        return dto;
    }
}
