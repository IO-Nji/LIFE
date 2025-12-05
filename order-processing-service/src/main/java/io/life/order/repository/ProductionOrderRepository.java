package io.life.order.repository;

import io.life.order.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductionOrder entities.
 * Handles database access for production orders submitted to SimAL.
 */
@Repository
public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {

    /**
     * Find a production order by its unique production order number.
     */
    Optional<ProductionOrder> findByProductionOrderNumber(String productionOrderNumber);

    /**
     * Find all production orders created from a specific customer order.
     */
    List<ProductionOrder> findBySourceCustomerOrderId(Long sourceCustomerOrderId);

    /**
     * Find all production orders created from a specific warehouse order.
     */
    List<ProductionOrder> findBySourceWarehouseOrderId(Long sourceWarehouseOrderId);

    /**
     * Find all production orders by status.
     */
    List<ProductionOrder> findByStatus(String status);

    /**
     * Find all production orders by priority.
     */
    List<ProductionOrder> findByPriority(String priority);

    /**
     * Find all production orders created by a specific workstation (Production Planning operator).
     */
    List<ProductionOrder> findByCreatedByWorkstationId(Long createdByWorkstationId);

    /**
     * Find production order by SimAL schedule ID.
     */
    Optional<ProductionOrder> findBySimalScheduleId(String simalScheduleId);
}
