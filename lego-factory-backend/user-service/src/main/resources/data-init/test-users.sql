-- ============================================================================
-- USER SERVICE DATABASE - Users and Roles
-- ============================================================================
-- Default admin account: legoAdmin / legoPass (auto-seeded)
-- Default warehouse: warehouseOperator / warehousePass (auto-seeded, WS-7)
-- Default modules supermarket: modulesSupermarketOp / modulesPass (auto-seeded, WS-8)

-- INSERT ADDITIONAL TEST USERS
-- ...existing code...
-- ...existing code...
INSERT INTO users (username, password_hash, role, workstation_id) VALUES
('production_planning_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'PRODUCTION_PLANNING', NULL),
('production_control_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'PRODUCTION_CONTROL', 1),
('assembly_control_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'ASSEMBLY_CONTROL', 4),
('injection_molding_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'MANUFACTURING_WORKSTATION', 1),
('parts_preproduction_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'MANUFACTURING_WORKSTATION', 2),
('part_finishing_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'MANUFACTURING_WORKSTATION', 3),
('gear_assembly_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'ASSEMBLY_WORKSTATION', 4),
('motor_assembly_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'ASSEMBLY_WORKSTATION', 5),
('final_assembly_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'ASSEMBLY_WORKSTATION', 6),
('parts_supply_warehouse_op', '$2a$10$abcdefghijklmnopqrstuvwxyz1234567890', 'PARTS_SUPPLY_WAREHOUSE', 9),
('legoAdmin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeU6FqOXXK7f.y/TkiN7j7B3dT9Z0K6Gi', 'ADMIN', NULL);