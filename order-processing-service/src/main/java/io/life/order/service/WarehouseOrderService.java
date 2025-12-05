package io.life.order.service;

import io.life.order.dto.WarehouseOrderDTO;
import io.life.order.dto.WarehouseOrderItemDTO;
import io.life.order.entity.WarehouseOrder;
import io.life.order.entity.WarehouseOrderItem;
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

    private final WarehouseOrderRepository warehouseOrderRepository;
    private final InventoryService inventoryService;

    public WarehouseOrderService(WarehouseOrderRepository warehouseOrderRepository,
                                 InventoryService inventoryService) {
        this.warehouseOrderRepository = warehouseOrderRepository;
        this.inventoryService = inventoryService;
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
        } else {
            order.setStatus("PROCESSING");
            order.setNotes((order.getNotes() != null ? order.getNotes() + " | " : "") + "Partial fulfillment completed");
            logger.info("Warehouse order {} partially fulfilled", order.getWarehouseOrderNumber());
        }

        order.setUpdatedAt(LocalDateTime.now());
        return mapToDTO(warehouseOrderRepository.save(order));
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
