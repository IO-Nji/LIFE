-- ============================================================================
-- INVENTORY SERVICE DATABASE - Stock Records
-- ============================================================================
-- Initialize stock records for all workstations with starter inventory

INSERT INTO stock_records (workstation_id, item_type, item_id, quantity, last_updated) VALUES
-- Plant Warehouse (WS-7) - Starting with 50 of each product variant
(7, 'PRODUCT_VARIANT', 1, 50, NOW()),
(7, 'PRODUCT_VARIANT', 2, 50, NOW()),
(7, 'PRODUCT_VARIANT', 3, 50, NOW()),
(7, 'PRODUCT_VARIANT', 4, 50, NOW()),
-- Modules Supermarket (WS-8) - Starting with modules for assembly
(8, 'MODULE', 1, 100, NOW()),
(8, 'MODULE', 2, 100, NOW()),
(8, 'MODULE', 3, 100, NOW()),
(8, 'MODULE', 4, 100, NOW()),
-- Parts Supply Warehouse (WS-9) - Starting with parts inventory
(9, 'PART', 1, 500, NOW()),
(9, 'PART', 2, 500, NOW()),
(9, 'PART', 3, 500, NOW()),
(9, 'PART', 4, 500, NOW()),
(9, 'PART', 5, 500, NOW()),
-- Manufacturing stations (WS-1, 2, 3) - Start with basic parts for production
(1, 'PART', 1, 100, NOW()),
(1, 'PART', 2, 100, NOW()),
(2, 'PART', 1, 100, NOW()),
(2, 'PART', 3, 100, NOW()),
(3, 'PART', 2, 100, NOW()),
(3, 'PART', 4, 100, NOW()),
-- Assembly stations (WS-4, 5, 6) - Start with modules for assembly
(4, 'MODULE', 1, 50, NOW()),
(4, 'MODULE', 3, 50, NOW()),
(5, 'MODULE', 2, 50, NOW()),
(5, 'MODULE', 4, 50, NOW()),
(6, 'MODULE', 1, 50, NOW()),
(6, 'MODULE', 2, 50, NOW());
