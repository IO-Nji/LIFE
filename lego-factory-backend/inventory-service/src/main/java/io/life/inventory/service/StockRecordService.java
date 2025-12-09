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

		StockRecord record;
		if (existing.isPresent()) {
			record = existing.get();
			record.setQuantity(quantity);
			record.setLastUpdated(LocalDateTime.now());
		} else {
			record = new StockRecord();
			record.setWorkstationId(workstationId);
			record.setItemType(itemType);
			record.setItemId(itemId);
			record.setQuantity(quantity);
			record.setLastUpdated(LocalDateTime.now());
		}

		StockRecord saved = repository.save(record);
		return toDto(saved);
	}

	public StockRecordDto save(StockRecordDto dto) {
		StockRecord record = new StockRecord();
		record.setWorkstationId(dto.getWorkstationId());
		record.setItemType(dto.getItemType());
		record.setItemId(dto.getItemId());
		record.setQuantity(dto.getQuantity());
		record.setLastUpdated(LocalDateTime.now());

		StockRecord saved = repository.save(record);
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
