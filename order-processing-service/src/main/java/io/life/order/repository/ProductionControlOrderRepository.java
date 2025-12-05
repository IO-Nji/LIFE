package io.life.order.repository;

import io.life.order.entity.ProductionControlOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionControlOrderRepository extends JpaRepository<ProductionControlOrder, Long> {

    Optional<ProductionControlOrder> findByControlOrderNumber(String controlOrderNumber);

    List<ProductionControlOrder> findByAssignedWorkstationId(Long workstationId);

    List<ProductionControlOrder> findByAssignedWorkstationIdAndStatus(Long workstationId, String status);

    List<ProductionControlOrder> findBySourceProductionOrderId(Long productionOrderId);

    List<ProductionControlOrder> findByStatus(String status);

    List<ProductionControlOrder> findBySimalScheduleId(String simalScheduleId);

    List<ProductionControlOrder> findByPriority(String priority);
}
