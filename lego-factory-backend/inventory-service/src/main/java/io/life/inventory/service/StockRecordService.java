package io.life.inventory.service;

import io.life.inventory.entity.StockRecord;
import io.life.inventory.repository.StockRecordRepository;
import io.life.inventory.dto.StockRecordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockRecordService {

	private final StockRecordRepository repository;

	public List<StockRecordDto> findAll() {
		return repository.findAll().stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	public StockRecordDto findById(Long id) {
		return repository.findById(id)
				.map(this::toDto)
				.orElse(null);
	}

	public List<StockRecordDto> getStockByWorkstationId(Long workstationId) {
		return repository.findByWorkstationId(workstationId).stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	public StockRecordDto getStockByWorkstationAndItem(Long workstationId, String itemType, Long itemId) {
		return repository.findByWorkstationIdAndItemTypeAndItemId(workstationId, itemType, itemId)
				.map(this::toDto)
				.orElse(null);
	}

	public StockRecordDto updateStock(Long workstationId, String itemType, Long itemId, Integer quantity) {
		Optional<StockRecord> existing = repository.findByWorkstationIdAndItemTypeAndItemId(
				workstationId, itemType, itemId);

		StockRecord stockRecord;
		if (existing.isPresent()) {
			stockRecord = existing.get();
			stockRecord.setQuantity(quantity);
			stockRecord.setLastUpdated(LocalDateTime.now());
		} else {
			stockRecord = new StockRecord();
			stockRecord.setWorkstationId(workstationId);
			stockRecord.setItemType(itemType);
			stockRecord.setItemId(itemId);
			stockRecord.setQuantity(quantity);
			stockRecord.setLastUpdated(LocalDateTime.now());
		}

		StockRecord saved = repository.save(stockRecord);
		return toDto(saved);
	}

	public StockRecordDto save(StockRecordDto dto) {
		StockRecord stockRecord = new StockRecord();
		stockRecord.setWorkstationId(dto.getWorkstationId());
		stockRecord.setItemType(dto.getItemType());
		stockRecord.setItemId(dto.getItemId());
		stockRecord.setQuantity(dto.getQuantity());
		stockRecord.setLastUpdated(LocalDateTime.now());

		StockRecord saved = repository.save(stockRecord);
		return toDto(saved);
	}

	public void deleteById(Long id) {
		repository.deleteById(id);
	}

	private StockRecordDto toDto(StockRecord entity) {
		return new StockRecordDto(
				entity.getId(),
				entity.getWorkstationId(),
				entity.getItemType(),
				entity.getItemId(),
				entity.getQuantity(),
				entity.getLastUpdated()
		);
	}

}
