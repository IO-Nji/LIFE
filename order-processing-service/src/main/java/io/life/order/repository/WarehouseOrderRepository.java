package io.life.order.repository;

import io.life.order.entity.WarehouseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseOrderRepository extends JpaRepository<WarehouseOrder, Long> {
    Optional<WarehouseOrder> findByWarehouseOrderNumber(String warehouseOrderNumber);
    List<WarehouseOrder> findByFulfillingWorkstationId(Long fulfillingWorkstationId);
    List<WarehouseOrder> findByRequestingWorkstationId(Long requestingWorkstationId);
    List<WarehouseOrder> findByStatus(String status);
    List<WarehouseOrder> findBySourceCustomerOrderId(Long sourceCustomerOrderId);
}
