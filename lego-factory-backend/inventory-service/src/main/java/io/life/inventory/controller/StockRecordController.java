package io.life.inventory.controller;

import io.life.inventory.dto.StockRecordDto;
import io.life.inventory.service.StockRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockRecordController {

	private final StockRecordService service;

	@GetMapping
	public ResponseEntity<List<StockRecordDto>> getAllStockRecords() {
		return ResponseEntity.ok(service.findAll());
	}

	@GetMapping("/{id}")
	public ResponseEntity<StockRecordDto> getStockRecordById(@PathVariable Long id) {
		StockRecordDto dto = service.findById(id);
		if (dto != null) {
			return ResponseEntity.ok(dto);
		}
		return ResponseEntity.notFound().build();
	}

	@GetMapping("/workstation/{workstationId}")
	public ResponseEntity<List<StockRecordDto>> getStockByWorkstation(@PathVariable Long workstationId) {
		return ResponseEntity.ok(service.getStockByWorkstationId(workstationId));
	}

	@GetMapping("/workstation/{workstationId}/item")
	public ResponseEntity<StockRecordDto> getStockByWorkstationAndItem(
			@PathVariable Long workstationId,
			@RequestParam String itemType,
			@RequestParam Long itemId) {
		StockRecordDto dto = service.getStockByWorkstationAndItem(workstationId, itemType, itemId);
		if (dto != null) {
			return ResponseEntity.ok(dto);
		}
		return ResponseEntity.notFound().build();
	}

	@PostMapping("/update")
	public ResponseEntity<StockRecordDto> updateStock(
			@RequestParam Long workstationId,
			@RequestParam String itemType,
			@RequestParam Long itemId,
			@RequestParam Integer quantity) {
		StockRecordDto updated = service.updateStock(workstationId, itemType, itemId, quantity);
		return ResponseEntity.ok(updated);
	}

	@PostMapping
	public ResponseEntity<StockRecordDto> createStockRecord(@RequestBody StockRecordDto dto) {
		StockRecordDto created = service.save(dto);
		return ResponseEntity.ok(created);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteStockRecord(@PathVariable Long id) {
		service.deleteById(id);
		return ResponseEntity.noContent().build();
	}

}
