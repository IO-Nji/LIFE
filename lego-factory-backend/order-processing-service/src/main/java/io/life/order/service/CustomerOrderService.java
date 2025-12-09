package io.life.order.service;

import io.life.order.dto.CustomerOrderDTO;
import io.life.order.dto.OrderItemDTO;
import io.life.order.entity.CustomerOrder;
import io.life.order.entity.OrderItem;
import io.life.order.repository.CustomerOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerOrderService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerOrderService.class);
    private final CustomerOrderRepository customerOrderRepository;

    public CustomerOrderService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    @Transactional
    public CustomerOrderDTO createOrder(CustomerOrderDTO orderDTO) {
        CustomerOrder order = new CustomerOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setWorkstationId(orderDTO.getWorkstationId());
        order.setNotes(orderDTO.getNotes());

        // Convert DTOs to entities
        List<OrderItem> orderItems = orderDTO.getOrderItems().stream()
            .map(itemDTO -> {
                OrderItem item = new OrderItem();
                item.setItemType(itemDTO.getItemType());
                item.setItemId(itemDTO.getItemId());
                item.setQuantity(itemDTO.getQuantity());
                item.setNotes(itemDTO.getNotes());
                item.setCustomerOrder(order);
                return item;
            })
            .collect(Collectors.toList());

        order.setOrderItems(orderItems);
        CustomerOrder savedOrder = customerOrderRepository.save(order);

        return mapToDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerOrderDTO> getOrderById(Long id) {
        return customerOrderRepository.findById(id)
            .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<CustomerOrderDTO> getOrderByNumber(String orderNumber) {
        return customerOrderRepository.findByOrderNumber(orderNumber)
            .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderDTO> getOrdersByWorkstationId(Long workstationId) {
        try {
            logger.info("Fetching orders for workstation: {}", workstationId);
            List<CustomerOrder> orders = customerOrderRepository.findByWorkstationId(workstationId);
            logger.info("Found {} orders for workstation {}", orders.size(), workstationId);
            return orders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching orders for workstation: {}", workstationId, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderDTO> getOrdersByStatus(String status) {
        return customerOrderRepository.findByStatus(status).stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public CustomerOrderDTO updateOrderStatus(Long id, String newStatus) {
        CustomerOrder order = customerOrderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        order.setStatus(newStatus);
        CustomerOrder updatedOrder = customerOrderRepository.save(order);

        return mapToDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long id) {
        customerOrderRepository.deleteById(id);
    }

    private CustomerOrderDTO mapToDTO(CustomerOrder order) {
        try {
            CustomerOrderDTO dto = new CustomerOrderDTO();
            dto.setId(order.getId());
            dto.setOrderNumber(order.getOrderNumber());
            dto.setOrderDate(order.getOrderDate());
            dto.setStatus(order.getStatus());
            dto.setWorkstationId(order.getWorkstationId());
            dto.setNotes(order.getNotes());
            dto.setCreatedAt(order.getCreatedAt());
            dto.setUpdatedAt(order.getUpdatedAt());

            if (order.getOrderItems() != null) {
                dto.setOrderItems(order.getOrderItems().stream()
                    .map(this::mapItemToDTO)
                    .collect(Collectors.toList()));
            }

            return dto;
        } catch (Exception e) {
            logger.error("Error mapping order to DTO: {}", order.getId(), e);
            throw new RuntimeException("Error mapping order to DTO: " + e.getMessage(), e);
        }
    }

    private OrderItemDTO mapItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setItemType(item.getItemType());
        dto.setItemId(item.getItemId());
        dto.setQuantity(item.getQuantity());
        dto.setNotes(item.getNotes());
        return dto;
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
