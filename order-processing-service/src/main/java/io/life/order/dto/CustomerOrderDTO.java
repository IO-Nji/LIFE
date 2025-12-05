package io.life.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderDTO {
    private Long id;
    private String orderNumber;
    private LocalDateTime orderDate;
    private String status;
    private List<OrderItemDTO> orderItems;
    private Long workstationId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
