**Overall Approach:** Relational database schema: The design prioritizes clarity, normalization, and supports the entities and relationships identified in your thesis and development plan.

---
## Conceptual

**1. `users` Table**
- **Purpose:** Stores user authentication and role information.
- `id` (PK, BIGINT, AUTO_INCREMENT)
- `username` (VARCHAR(255), UNIQUE, NOT NULL)
- `password` (VARCHAR(255), NOT NULL) - _Hashed password_
- `workstation_id` (BIGINT, NULLABLE) - FK to `workstations.id` (can be null for ADMIN or users not tied to a specific physical workstation)

**2. `user_roles` Table**
- **Purpose:** Stores the roles assigned to each user (many-to-many relationship).
- `user_id` (PK, FK to `users.id`, BIGINT)
- `roles` (PK, VARCHAR(50), NOT NULL) - _Stores `UserRole` enum names (e.g., 'ADMIN', 'PLANT_WAREHOUSE')_

**3. `workstations` Table**
- **Purpose:** Defines all physical or logical workstations in the factory.
- `id` (PK, BIGINT, AUTO_INCREMENT)
- `name` (VARCHAR(100), UNIQUE, NOT NULL) - _e.g., 'Plant Warehouse', 'Injection Molding'_
- `type` (VARCHAR(50), NOT NULL) - _e.g., 'Supply', 'Manufacturing', 'Control', 'Planning', 'Assembly'_
- `description` (TEXT, NULLABLE)

**4. `product_variants` Table**
- **Purpose:** Defines the final products (drills) that the factory produces.
- `id` (PK, BIGINT, AUTO_INCREMENT)
- `name` (VARCHAR(100), UNIQUE, NOT NULL) - _e.g., 'Variant A'_
- `description` (TEXT, NULLABLE)
- `voltage_power` (VARCHAR(50), NOT NULL) - _e.g., '220V/720W'_
- `gear` (VARCHAR(50), NOT NULL) - _e.g., '2 Gear'_
- `colour` (VARCHAR(50), NOT NULL) - _e.g., 'RED'_

**5. `modules` Table**
- **Purpose:** Defines intermediate assembled components (modules) required for product variants.
- `id` (PK, BIGINT, AUTO_INCREMENT)
- `name` (VARCHAR(100), UNIQUE, NOT NULL) - _e.g., 'Motor Assembly', 'Red Casing'_
- `description` (TEXT, NULLABLE)

**6. `parts` Table**
- **Purpose:** Defines the raw materials or basic LEGO components.
- `id` (PK, BIGINT, AUTO_INCREMENT)
- `name` (VARCHAR(100), UNIQUE, NOT NULL) - _e.g., 'LEGO Brick 2x4 Red', 'Motor Unit'_
- `description` (TEXT, NULLABLE)

**7. `product_variant_modules` Table (Junction Table)**
- **Purpose:** Links `product_variants` to the `modules` they are composed of (Many-to-Many).
- `product_variant_id` (PK, FK to `product_variants.id`, BIGINT)
- `module_id` (PK, FK to `modules.id`, BIGINT)
- `quantity` (INT, NOT NULL) - _How many of this module are needed for this variant._

**8. `module_parts` Table (Junction Table)**
- **Purpose:** Links `modules` to the `parts` they are composed of (Many-to-Many).
- `module_id` (PK, FK to `modules.id`, BIGINT)
- `part_id` (PK, FK to `parts.id`, BIGINT)
- `quantity` (INT, NOT NULL) - _How many of this part are needed for this module._

**9. `stock_records` Table**
- **Purpose:** Tracks the quantity of each `item` (ProductVariant, Module, Part) at each `workstation` (acting as a storage location).
- `id` (PK, BIGINT, AUTO_INCREMENT)
- `workstation_id` (FK to `workstations.id`, BIGINT, NOT NULL)
- `item_type` (VARCHAR(50), NOT NULL) - _ENUM: 'PRODUCT_VARIANT', 'MODULE', 'PART'_
- `item_id` (BIGINT, NOT NULL) - _ID of the item (references `product_variants.id`, `modules.id`, or `parts.id` based on `item_type`)_
- `quantity` (INT, NOT NULL, DEFAULT 0)
- `last_updated` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP)
- **(Unique Constraint): `(workstation_id, item_type, item_id)`**

**10. `orders` Table (Base/Parent Table)**
- **Purpose:** Abstract base for all order types.
- `id` (PK, BIGINT, AUTO_INCREMENT)
- `dtype` (VARCHAR(31), NOT NULL) - _Discriminator column for Hibernate inheritance, e.g., 'CustomerOrder', 'WarehouseOrder'_
- `order_number` (VARCHAR(50), UNIQUE, NOT NULL) - _Logical order ID_
- `order_date` (TIMESTAMP, NOT NULL)
- `status` (VARCHAR(50), NOT NULL) - _ENUM: 'PENDING', 'PLANNED', 'IN_PROGRESS', 'FULFILLED', 'CANCELLED', etc._
- `estimated_start_time` (TIMESTAMP, NULLABLE)
- `estimated_finish_time` (TIMESTAMP, NULLABLE)
- `actual_start_time` (TIMESTAMP, NULLABLE)
- `actual_finish_time` (TIMESTAMP, NULLABLE)
- `originating_workstation_id` (FK to `workstations.id`, BIGINT, NULLABLE) - _Who initiated this order_
- `destination_workstation_id` (FK to `workstations.id`, BIGINT, NULLABLE) - _Who is the primary recipient/responsible_

**11. `order_items` Table**
- **Purpose:** Details the specific items and quantities within an order.
- `id` (PK, BIGINT, AUTO_INCREMENT)
- `order_id` (FK to `orders.id`, BIGINT, NOT NULL)
- `item_type` (VARCHAR(50), NOT NULL) - _ENUM: 'PRODUCT_VARIANT', 'MODULE', 'PART'_
- `item_id` (BIGINT, NOT NULL) - _ID of the item (references `product_variants.id`, `modules.id`, or `parts.id`)_
- `quantity` (INT, NOT NULL)

**12. `customer_orders` Table**
- **Purpose:** Specific details for customer orders. (Inherits from `orders`).
- `id` (PK, FK to `orders.id`, BIGINT)
- `customer_name` (VARCHAR(255), NULLABLE) - _Could be anonymous or specific_

**13. `warehouse_orders` Table**
- **Purpose:** Specific details for internal warehouse restock orders. (Inherits from `orders`).
- `id` (PK, FK to `orders.id`, BIGINT)

**14. `production_orders` Table**
- **Purpose:** Specific details for manufacturing/assembly requests. (Inherits from `orders`).
- `id` (PK, FK to `orders.id`, BIGINT)
- `simal_request_id` (VARCHAR(255), NULLABLE) - _Correlation ID with SimAL if applicable_

**15. `supply_orders` Table**
- **Purpose:** Specific details for requests to Parts Supply Warehouse. (Inherits from `orders`).
- `id` (PK, FK to `orders.id`, BIGINT)