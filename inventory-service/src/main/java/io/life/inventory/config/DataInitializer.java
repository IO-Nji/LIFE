package io.life.inventory.config;

import io.life.inventory.entity.StockRecord;
import io.life.inventory.repository.StockRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

/**
 * DataInitializer for Inventory Service.
 * Seeds initial stock records for workstations with all product variants.
 * 
 * This ensures that when the Plant Warehouse (or other workstations) fetches inventory,
 * there is test data available to display.
 * 
 * NOTE: Data is persisted in the H2 file-based database and will NOT be re-seeded on restart.
 * To reset inventory data, delete the ./data/inventory_db.mv.db file.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

	private final StockRecordRepository stockRecordRepository;

	/**
	 * Seed initial stock records ONLY on first application startup.
	 * Once data is seeded, it is persisted in the file-based H2 database and will
	 * NOT be re-initialized on subsequent application restarts.
	 * 
	 * Creates inventory for:
	 * - Plant Warehouse (ID 7): All product variants
	 * - Modules Supermarket (ID 8): Base modules inventory
	 * 
	 * To reset data: Delete ./data/inventory_db.mv.db file and restart application.
	 */
	@Override
	public void run(String... args) throws Exception {
		long totalRecords = stockRecordRepository.count();
		
		if (totalRecords == 0) {
			log.info("No stock records found. Seeding initial inventory data...");

			// Plant Warehouse (WS-7) - Stock for all product variants
			// Product variants from MasterdataService:
			// 1: Technic Truck Yellow, 2: Technic Truck Red, 3: Creator House, 4: Friends Cafe, 5-7: Others
			seedPlantWarehouseStock();

			// Modules Supermarket (WS-8) - Base module inventory
			seedModulesSupermarketStock();

			log.info("✓ Stock records seeded successfully. Data will persist across application restarts.");
		} else {
			log.info("✓ Stock records exist ({} records). Skipping initialization to preserve existing data.", totalRecords);
		}
	}

	/**
	 * Seed Plant Warehouse (WS-7) with initial stock of product variants.
	 * Each product variant gets a default quantity for testing.
	 * 
	 * Products (from masterdata-service):
	 * ID 1: Technic Truck Yellow
	 * ID 2: Technic Truck Red
	 * ID 3: Creator House
	 * ID 4: Friends Cafe
	 */
	private void seedPlantWarehouseStock() {
		Long plantWarehouseId = 7L;

		// Product variant stock levels (itemType = "PRODUCT")
		// Quantities set to reasonable defaults for testing fulfillment scenarios
		createStockRecord(plantWarehouseId, "PRODUCT", 1L, 15);  // Technic Truck Yellow
		createStockRecord(plantWarehouseId, "PRODUCT", 2L, 12);  // Technic Truck Red
		createStockRecord(plantWarehouseId, "PRODUCT", 3L, 8);   // Creator House
		createStockRecord(plantWarehouseId, "PRODUCT", 4L, 6);   // Friends Cafe

		log.info("Seeded Plant Warehouse (WS-7) with product variant inventory");
	}

	/**
	 * Seed Modules Supermarket (WS-8) with initial module inventory.
	 * Modules are components used in manufacturing.
	 */
	private void seedModulesSupermarketStock() {
		Long modulesSupermarketId = 8L;

		// Module inventory
		createStockRecord(modulesSupermarketId, "MODULE", 1L, 50);  // Chassis Module
		createStockRecord(modulesSupermarketId, "MODULE", 2L, 40);  // Electrical Module
		createStockRecord(modulesSupermarketId, "MODULE", 3L, 45);  // Wheel Assembly
		createStockRecord(modulesSupermarketId, "MODULE", 4L, 30);  // Control Panel

		log.info("Seeded Modules Supermarket (WS-8) with module inventory");
	}

	/**
	 * Helper method to create and save a stock record.
	 */
	private void createStockRecord(Long workstationId, String itemType, Long itemId, Integer quantity) {
		StockRecord record = new StockRecord();
		record.setWorkstationId(workstationId);
		record.setItemType(itemType);
		record.setItemId(itemId);
		record.setQuantity(quantity);
		record.setLastUpdated(LocalDateTime.now());

		stockRecordRepository.save(record);
		log.debug("Created stock record: WS-{} | {} #{} | Qty: {}",
				workstationId, itemType, itemId, quantity);
	}
}
