
-- ============================================================================
-- ORDER PROCESSING SERVICE DATABASE - Sample Customer Orders
-- ============================================================================
-- These orders will flow through the system for end-to-end testing

INSERT INTO customer_orders (order_number, order_date, status, workstation_id, notes, created_at, updated_at) VALUES
('ORD-TEST001', NOW(), 'PENDING', 7, 'Test order 1 - Technic Truck', NOW(), NOW()),
('ORD-TEST002', NOW(), 'PENDING', 7, 'Test order 2 - Creator House', NOW(), NOW()),
('ORD-TEST003', NOW(), 'PENDING', 7, 'Test order 3 - Friends Cafe', NOW(), NOW());

-- Order items for test orders
INSERT INTO order_items (customer_order_id, item_type, item_id, quantity, notes) VALUES
-- Order 1: 2 Technic Trucks (Yellow and Red)
(1, 'PRODUCT_VARIANT', 1, 2, 'Yellow variants'),
(1, 'PRODUCT_VARIANT', 4, 1, 'Red variant'),
-- Order 2: 3 Creator Houses
(2, 'PRODUCT_VARIANT', 2, 3, 'Multi-unit order'),
-- Order 3: 2 Friends Cafes + 1 Creator House
(3, 'PRODUCT_VARIANT', 3, 2, 'Cafe units'),
(3, 'PRODUCT_VARIANT', 2, 1, 'House unit');