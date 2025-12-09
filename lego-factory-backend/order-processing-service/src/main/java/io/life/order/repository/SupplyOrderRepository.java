package io.life.order.repository;

import io.life.order.entity.SupplyOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SupplyOrder entity.
 */
@Repository
public interface SupplyOrderRepository extends JpaRepository<SupplyOrder, Long> {

    /**
     * Find all supply orders for a specific requesting workstation.
     */
    List<SupplyOrder> findByRequestingWorkstationId(Long workstationId);

    /**
     * Find all supply orders for a specific supply warehouse workstation.
     */
    List<SupplyOrder> findBySupplyWarehouseWorkstationId(Long warehouseWorkstationId);

    /**
     * Find all pending supply orders for a specific workstation.
     */
    List<SupplyOrder> findByRequestingWorkstationIdAndStatus(Long workstationId, String status);

    /**
     * Find all pending supply orders for the supply warehouse.
     */
    List<SupplyOrder> findBySupplyWarehouseWorkstationIdAndStatus(Long warehouseWorkstationId, String status);

    /**
     * Find supply orders by source control order.
     */
    List<SupplyOrder> findBySourceControlOrderIdAndSourceControlOrderType(Long controlOrderId, String controlOrderType);

    /**
     * Find a supply order by its number.
     */
    Optional<SupplyOrder> findBySupplyOrderNumber(String supplyOrderNumber);
}
