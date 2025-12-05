package io.life.order.repository;

import io.life.order.entity.AssemblyControlOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssemblyControlOrderRepository extends JpaRepository<AssemblyControlOrder, Long> {

    Optional<AssemblyControlOrder> findByControlOrderNumber(String controlOrderNumber);

    List<AssemblyControlOrder> findByAssignedWorkstationId(Long workstationId);

    List<AssemblyControlOrder> findByAssignedWorkstationIdAndStatus(Long workstationId, String status);

    List<AssemblyControlOrder> findBySourceProductionOrderId(Long productionOrderId);

    List<AssemblyControlOrder> findByStatus(String status);

    List<AssemblyControlOrder> findBySimalScheduleId(String simalScheduleId);

    List<AssemblyControlOrder> findByPriority(String priority);
}
