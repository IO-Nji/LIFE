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
     * Mark as fulfilled and update inventory at the requesting workstation
     * If partial fulfillment, create a ProductionOrder for unfulfilled items (Scenario 3)
     */
    public WarehouseOrderDTO fulfillWarehouseOrder(Long warehouseOrderId) {
        Optional<WarehouseOrder> orderOpt = warehouseOrderRepository.findById(warehouseOrderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Warehouse order not found: " + warehouseOrderId);
        }

        WarehouseOrder order = orderOpt.get();
        logger.info("Fulfilling warehouse order {} from workstation {}", order.getWarehouseOrderNumber(), order.getFulfillingWorkstationId());

        // Update inventory at the requesting workstation (Plant Warehouse)
        boolean allItemsFulfilled = true;
        for (WarehouseOrderItem item : order.getWarehouseOrderItems()) {
            try {
                // Deduct from fulfilling workstation's stock
                boolean deducted = inventoryService.updateStock(
                        order.getFulfillingWorkstationId(),
                        item.getItemId(),
                        item.getRequestedQuantity()
                );

                if (deducted) {
                    // Add to requesting workstation's stock
                    // Note: This assumes inventory service can handle adding stock
                    // For now, we just log the action
                    item.setFulfilledQuantity(item.getRequestedQuantity());
                    logger.info("Fulfilled item {} qty {} for warehouse order {}", 
                            item.getItemId(), item.getRequestedQuantity(), order.getWarehouseOrderNumber());
                } else {
                    allItemsFulfilled = false;
                    logger.warn("Failed to fulfill item {} for warehouse order {}", 
                            item.getItemId(), order.getWarehouseOrderNumber());
                }
            } catch (Exception e) {
                allItemsFulfilled = false;
                logger.error("Error fulfilling item {} for warehouse order {}: {}", 
                        item.getItemId(), order.getWarehouseOrderNumber(), e.getMessage());
            }
        }

        // Update warehouse order status
        if (allItemsFulfilled) {
            order.setStatus("FULFILLED");
            logger.info("Warehouse order {} fully fulfilled", order.getWarehouseOrderNumber());
            
            // CRITICAL: Complete source customer order when warehouse order is fully fulfilled
            try {
                Optional<CustomerOrder> sourceOrder = customerOrderRepository.findById(order.getSourceCustomerOrderId());
                if (sourceOrder.isPresent()) {
                    CustomerOrder customerOrder = sourceOrder.get();
                    customerOrder.setStatus("COMPLETED");
                    customerOrder.setNotes((customerOrder.getNotes() != null ? customerOrder.getNotes() + " | " : "") 
                            + "Warehouse order " + order.getWarehouseOrderNumber() + " fulfilled - order completed");
                    customerOrderRepository.save(customerOrder);
                    logger.info("Source customer order {} completed after warehouse order fulfillment", customerOrder.getOrderNumber());
                }
            } catch (Exception e) {
                logger.error("Failed to complete source customer order after warehouse fulfillment: {}", e.getMessage());
            }
        } else {
            // Scenario 3: Partial fulfillment - Create ProductionOrder for unfulfilled items
            order.setStatus("PROCESSING");
            order.setNotes((order.getNotes() != null ? order.getNotes() + " | " : "") + "Partial fulfillment completed");
            logger.info("Warehouse order {} partially fulfilled - creating ProductionOrder", order.getWarehouseOrderNumber());

            // Create ProductionOrder for unfulfilled items
            try {
                String priority = determinePriority(order);
                productionOrderService.createProductionOrderFromWarehouse(
                        order.getSourceCustomerOrderId(),
                        warehouseOrderId,
                        priority,
                        order.getOrderDate().plusDays(7), // Due 7 days from order date
                        "Created from warehouse order " + order.getWarehouseOrderNumber() + " - partial fulfillment",
                        PLANT_WAREHOUSE_WORKSTATION_ID,
                        FINAL_ASSEMBLY_WORKSTATION_ID  // Assign to Final Assembly for completion
                );
                logger.info("Created ProductionOrder for unfulfilled items from warehouse order {} assigned to Final Assembly", order.getWarehouseOrderNumber());
            } catch (Exception e) {
                logger.error("Failed to create ProductionOrder for warehouse order {}: {}", order.getWarehouseOrderNumber(), e.getMessage());
            }
        }

        order.setUpdatedAt(LocalDateTime.now());
        return mapToDTO(warehouseOrderRepository.save(order));
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
