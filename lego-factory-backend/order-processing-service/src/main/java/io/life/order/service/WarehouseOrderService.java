package io.life.order.service;

import io.life.order.dto.WarehouseOrderDTO;
import io.life.order.dto.WarehouseOrderItemDTO;
import io.life.order.entity.CustomerOrder;
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
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseOrderService {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseOrderService.class);
    private static final Long PLANT_WAREHOUSE_WORKSTATION_ID = 7L;
    private static final Long FINAL_ASSEMBLY_WORKSTATION_ID = 6L;

    private final WarehouseOrderRepository warehouseOrderRepository;
    private final InventoryService inventoryService;
    private final ProductionOrderService productionOrderService;
    private final CustomerOrderRepository customerOrderRepository;

    public WarehouseOrderService(WarehouseOrderRepository warehouseOrderRepository,
                                 InventoryService inventoryService,
                                 ProductionOrderService productionOrderService,
                                 CustomerOrderRepository customerOrderRepository) {
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.inventoryService = inventoryService;
        this.productionOrderService = productionOrderService;
        this.customerOrderRepository = customerOrderRepository;
    }

    /**
     * Get all warehouse orders
     */
    public List<WarehouseOrderDTO> getAllWarehouseOrders() {
        return warehouseOrderRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get warehouse order by ID
     */
    public Optional<WarehouseOrderDTO> getWarehouseOrderById(Long id) {
        return warehouseOrderRepository.findById(id)
                .map(this::mapToDTO);
    }

    /**
     * Get warehouse orders by fulfilling workstation ID
     * Used to retrieve orders that need to be fulfilled (e.g., Modules Supermarket)
     */
    public List<WarehouseOrderDTO> getWarehouseOrdersByFulfillingWorkstationId(Long fulfillingWorkstationId) {
        return warehouseOrderRepository.findByFulfillingWorkstationId(fulfillingWorkstationId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get warehouse orders by requesting workstation ID
     * Used to retrieve orders that were created by a workstation (e.g., Plant Warehouse)
     */
    public List<WarehouseOrderDTO> getWarehouseOrdersByRequestingWorkstationId(Long requestingWorkstationId) {
        return warehouseOrderRepository.findByRequestingWorkstationId(requestingWorkstationId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get warehouse orders by status
     */
    public List<WarehouseOrderDTO> getWarehouseOrdersByStatus(String status) {
        return warehouseOrderRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fulfill a warehouse order (Modules Supermarket workflow)
     * 
     * ENHANCED LOGIC:
     * 1. Check Modules Supermarket stock FIRST
     * 2. Fulfill what's available (partial or full)
     * 3. If shortage exists, AUTO-TRIGGER production order for missing items
     * 4. Update source customer order status accordingly
     */
    public WarehouseOrderDTO fulfillWarehouseOrder(Long warehouseOrderId) {
        Optional<WarehouseOrder> orderOpt = warehouseOrderRepository.findById(warehouseOrderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Warehouse order not found: " + warehouseOrderId);
        }

        WarehouseOrder order = orderOpt.get();
        logger.info("Processing warehouse order {} from Modules Supermarket (WS-8)", order.getWarehouseOrderNumber());

        // STEP 1: Check which items are available at Modules Supermarket (workstation 8)
        boolean allItemsAvailable = order.getWarehouseOrderItems().stream()
                .allMatch(item -> inventoryService.checkStock(
                        order.getFulfillingWorkstationId(),
                        item.getItemId(),
                        item.getRequestedQuantity()
                ));

        if (allItemsAvailable) {
            // SCENARIO A: All items available - Direct fulfillment
            logger.info("Scenario A: All items available in Modules Supermarket - Direct fulfillment");
            return fulfillAllItems(order);
        } else {
            // Check if ANY items are available
            boolean anyItemsAvailable = order.getWarehouseOrderItems().stream()
                    .anyMatch(item -> inventoryService.checkStock(
                            order.getFulfillingWorkstationId(),
                            item.getItemId(),
                            item.getRequestedQuantity()
                    ));

            if (anyItemsAvailable) {
                // SCENARIO B: Partial items available - Partial fulfillment + Auto-create production order
                logger.info("Scenario B: Partial items available in Modules Supermarket");
                return fulfillPartialAndTriggerProduction(order);
            } else {
                // SCENARIO C: No items available - Auto-create production order for all
                logger.info("Scenario C: No items available in Modules Supermarket - Creating production order for complete order");
                return fulfillNoneAndTriggerProduction(order);
            }
        }
    }

    /**
     * Scenario A: All items available - Deduct from Modules Supermarket, complete order
     */
    private WarehouseOrderDTO fulfillAllItems(WarehouseOrder order) {
        logger.info("Fulfilling all items for warehouse order {}", order.getWarehouseOrderNumber());

        boolean allItemsFulfilled = true;
        for (WarehouseOrderItem item : order.getWarehouseOrderItems()) {
            try {
                // Deduct from Modules Supermarket stock
                boolean deducted = inventoryService.updateStock(
                        order.getFulfillingWorkstationId(),
                        item.getItemId(),
                        item.getRequestedQuantity()
                );

                if (deducted) {
                    item.setFulfilledQuantity(item.getRequestedQuantity());
                    logger.info("  ✓ Item {} qty {} fulfilled", item.getItemId(), item.getRequestedQuantity());
                } else {
                    allItemsFulfilled = false;
                    logger.warn("  ✗ Failed to deduct item {} from inventory", item.getItemId());
                }
            } catch (Exception e) {
                allItemsFulfilled = false;
                logger.error("  ✗ Error fulfilling item {}: {}", item.getItemId(), e.getMessage());
            }
        }

        if (allItemsFulfilled) {
            order.setStatus("FULFILLED");
            logger.info("Warehouse order {} fully fulfilled", order.getWarehouseOrderNumber());
            
            // Complete source customer order
            completeSourceCustomerOrder(order);
        } else {
            order.setStatus("PARTIALLY_FULFILLED");
            logger.warn("Warehouse order {} partially fulfilled due to inventory errors", order.getWarehouseOrderNumber());
        }

        order.setUpdatedAt(LocalDateTime.now());
        return mapToDTO(warehouseOrderRepository.save(order));
    }

    /**
     * Scenario B: Partial items available - Fulfill available items + Auto-trigger production for missing
     */
    private WarehouseOrderDTO fulfillPartialAndTriggerProduction(WarehouseOrder order) {
        logger.info("Fulfilling partial items for warehouse order {}", order.getWarehouseOrderNumber());

        List<WarehouseOrderItem> itemsToProduceLater = new ArrayList<>();

        // Fulfill available items only
        for (WarehouseOrderItem item : order.getWarehouseOrderItems()) {
            if (inventoryService.checkStock(order.getFulfillingWorkstationId(), item.getItemId(), item.getRequestedQuantity())) {
                try {
                    // Deduct from Modules Supermarket stock
                    boolean deducted = inventoryService.updateStock(
                            order.getFulfillingWorkstationId(),
                            item.getItemId(),
                            item.getRequestedQuantity()
                    );

                    if (deducted) {
                        item.setFulfilledQuantity(item.getRequestedQuantity());
                        logger.info("  ✓ Item {} qty {} fulfilled from Modules Supermarket", item.getItemId(), item.getRequestedQuantity());
                    }
                } catch (Exception e) {
                    logger.error("  ✗ Error fulfilling available item {}: {}", item.getItemId(), e.getMessage());
                }
            } else {
                // Mark as to be produced later
                itemsToProduceLater.add(item);
                logger.info("  ⚠ Item {} NOT available - will trigger production order", item.getItemId());
            }
        }

        order.setStatus("PROCESSING");
        order.setNotes((order.getNotes() != null ? order.getNotes() + " | " : "") + 
                "Partial fulfillment: " + itemsToProduceLater.size() + " item(s) short");

        // AUTO-TRIGGER: Create production order for missing items
        if (!itemsToProduceLater.isEmpty()) {
            logger.info("AUTO-TRIGGERING production order for {} missing item(s)", itemsToProduceLater.size());
            triggerProductionOrderForShortfall(order, itemsToProduceLater);
        }

        order.setUpdatedAt(LocalDateTime.now());
        return mapToDTO(warehouseOrderRepository.save(order));
    }

    /**
     * Scenario C: No items available - Auto-trigger production order for complete order
     */
    private WarehouseOrderDTO fulfillNoneAndTriggerProduction(WarehouseOrder order) {
        logger.info("No items available in Modules Supermarket - AUTO-TRIGGERING production order for entire warehouse order");

        order.setStatus("PENDING_PRODUCTION");
        order.setNotes((order.getNotes() != null ? order.getNotes() + " | " : "") + 
                "No stock available - production order auto-triggered");

        // AUTO-TRIGGER: Create production order for ALL items
        triggerProductionOrderForShortfall(order, order.getWarehouseOrderItems());

        order.setUpdatedAt(LocalDateTime.now());
        return mapToDTO(warehouseOrderRepository.save(order));
    }

    /**
     * AUTO-TRIGGER: Create production order for items not available at Modules Supermarket
     */
    private void triggerProductionOrderForShortfall(WarehouseOrder order, List<WarehouseOrderItem> shortfallItems) {
        try {
            String priority = determinePriority(order);
            
            // Create production order linked to this warehouse order
            productionOrderService.createProductionOrderFromWarehouse(
                    order.getSourceCustomerOrderId(),
                    order.getId(),
                    priority,
                    order.getOrderDate().plusDays(7), // Due 7 days from order date
                    "Auto-created from warehouse order " + order.getWarehouseOrderNumber() + 
                    " - Modules Supermarket shortfall (" + shortfallItems.size() + " item(s))",
                    order.getRequestingWorkstationId(),
                    order.getFulfillingWorkstationId()  // Assign back to Modules Supermarket for completion
            );
            
            logger.info("✓ Production order AUTO-CREATED for warehouse order {} with {} shortfall item(s)", 
                    order.getWarehouseOrderNumber(), shortfallItems.size());
            
            // Update notes with auto-trigger confirmation
            order.setNotes((order.getNotes() != null ? order.getNotes() + " | " : "") + 
                    "Production order auto-triggered for Modules Supermarket shortfall");
            
        } catch (Exception e) {
            logger.error("✗ Failed to auto-create production order for warehouse order {}: {}", 
                    order.getWarehouseOrderNumber(), e.getMessage());
        }
    }

    /**
     * Complete the source customer order when warehouse order is fulfilled
     */
    private void completeSourceCustomerOrder(WarehouseOrder order) {
        try {
            Optional<CustomerOrder> sourceOrder = customerOrderRepository.findById(order.getSourceCustomerOrderId());
            if (sourceOrder.isPresent()) {
                CustomerOrder customerOrder = sourceOrder.get();
                customerOrder.setStatus("COMPLETED");
                customerOrder.setNotes((customerOrder.getNotes() != null ? customerOrder.getNotes() + " | " : "") 
                        + "Warehouse order " + order.getWarehouseOrderNumber() + " fully fulfilled - customer order completed");
                customerOrderRepository.save(customerOrder);
                logger.info("✓ Source customer order {} completed after warehouse order fulfillment", customerOrder.getOrderNumber());
            }
        } catch (Exception e) {
            logger.error("✗ Failed to complete source customer order after warehouse fulfillment: {}", e.getMessage());
        }
    }

    /**
     * Determine priority based on warehouse order urgency
     */
    private String determinePriority(WarehouseOrder order) {
        LocalDateTime now = LocalDateTime.now();
        long daysDifference = java.time.temporal.ChronoUnit.DAYS.between(now, order.getOrderDate());
        
        if (daysDifference < 1) {
            return "HIGH";
        } else if (daysDifference < 3) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Update warehouse order status
     */
    public WarehouseOrderDTO updateWarehouseOrderStatus(Long warehouseOrderId, String status) {
        Optional<WarehouseOrder> orderOpt = warehouseOrderRepository.findById(warehouseOrderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Warehouse order not found: " + warehouseOrderId);
        }

        WarehouseOrder order = orderOpt.get();
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        logger.info("Updated warehouse order {} status to {}", order.getWarehouseOrderNumber(), status);

        return mapToDTO(warehouseOrderRepository.save(order));
    }

    /**
     * Map WarehouseOrder entity to DTO
     */
    private WarehouseOrderDTO mapToDTO(WarehouseOrder order) {
        WarehouseOrderDTO dto = new WarehouseOrderDTO();
        dto.setId(order.getId());
        dto.setWarehouseOrderNumber(order.getWarehouseOrderNumber());
        dto.setSourceCustomerOrderId(order.getSourceCustomerOrderId());
        dto.setRequestingWorkstationId(order.getRequestingWorkstationId());
        dto.setFulfillingWorkstationId(order.getFulfillingWorkstationId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTriggerScenario(order.getTriggerScenario());
        dto.setNotes(order.getNotes());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        // Map warehouse order items
        if (order.getWarehouseOrderItems() != null) {
            dto.setWarehouseOrderItems(order.getWarehouseOrderItems().stream()
                    .map(this::mapItemToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Map WarehouseOrderItem entity to DTO
     */
    private WarehouseOrderItemDTO mapItemToDTO(WarehouseOrderItem item) {
        WarehouseOrderItemDTO dto = new WarehouseOrderItemDTO();
        dto.setId(item.getId());
        dto.setItemId(item.getItemId());
        dto.setItemName(item.getItemName());
        dto.setRequestedQuantity(item.getRequestedQuantity());
        dto.setFulfilledQuantity(item.getFulfilledQuantity());
        dto.setItemType(item.getItemType());
        dto.setNotes(item.getNotes());
        return dto;
    }
}
