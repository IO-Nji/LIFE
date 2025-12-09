package io.life.inventory.repository;

import io.life.inventory.entity.StockRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Long> {

	List<StockRecord> findByWorkstationId(Long workstationId);

	Optional<StockRecord> findByWorkstationIdAndItemTypeAndItemId(
			Long workstationId,
			String itemType,
			Long itemId
	);

	List<StockRecord> findByItemTypeAndItemId(String itemType, Long itemId);

}
