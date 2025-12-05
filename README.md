# LEGO Sample Factory Control System

A microservice-based prototype that digitizes control flows for the LEGO Sample Factory. The platform replaces paper-heavy processes with services that manage gateway routing, user management, reference data, inventory tracking, and workstation role assignments, accompanied by a React frontend for operator dashboards.

## Implemented Modules

- **api-gateway** (Spring Cloud Gateway, port 8011)
  - Central routing layer with CORS support for frontend applications on ports 5173 and 5174.
  - Routes traffic to all downstream microservices with proper path handling.
- **user-service** (Spring Boot, port 8012)
  - Full authentication and authorization with JWT token generation.
  - User management (create, read, update, delete) with role-based access control.
  - Supports user-to-workstation assignments for role-scoped operations.
  - Backed by H2 file-based database (`data/user_service.db`).
- **masterdata-service** (Spring Boot, port 8013)
  - Reference data management for products, modules, parts, and workstations.
  - REST endpoints for product variants, manufacturing modules, and parts catalogs.
  - Workstation entity with type classification (MANUFACTURING, ASSEMBLY, WAREHOUSE).
  - Backed by H2 file-based database (`data/masterdata_service.db`).
  - Automatic data seeding (4 products, 4 modules, 5 parts, 9 workstations).
- **inventory-service** (Spring Boot, port 8014)
  - Stock record management with workstation and item tracking.
  - REST endpoints for stock CRUD operations and workstation inventory queries.
  - Backed by H2 file-based database (`data/inventory_service.db`).
- **lego-factory-frontend** (React + Vite)
  - Multi-page SPA with authentication, user management, and product catalog.
  - Dynamic user-workstation assignment UI for administrators.
  - JWT token interceptor for API calls with automatic session management.

## Technology Stack

- **Backend:** Java 21, Spring Boot 3.4.2, Spring Cloud Gateway 2024.0.0, Maven Wrapper, H2 databases (file-based).
- **Frontend:** React 18, Vite tooling, Axios for API calls, React Router for navigation, LocalStorage for session persistence.
- **Tooling:** Visual Studio Code workspace, npm package manager, Node.js runtime.

## Default Accounts

- `legoAdmin` / `legoPass` — provisioned automatically in the user-service with the `ADMIN` role for local development. Use this account when signing in from the frontend to manage additional users.

## API Documentation

### User-Service REST API (Port 8012, routed via `/api/auth/**` and `/api/users/**`)

- `POST /api/auth/login` — authenticate user and issue JWT token.
- `GET /api/users/me` — fetch currently authenticated user's profile with workstationId.
- `GET /api/users` *(ADMIN)* — list all users with role and workstation assignments.
- `GET /api/users/{id}` *(ADMIN)* — fetch specific user by ID.
- `POST /api/users` *(ADMIN)* — create new user with role and optional workstation.
- `PUT /api/users/{id}` *(ADMIN)* — update user role, workstation, or credentials.
- `DELETE /api/users/{id}` *(ADMIN)* — delete user account.

### Masterdata-Service REST API (Port 8013, routed via `/api/masterdata/**`)

- `GET /api/masterdata/product-variants` — list all product variants with pricing and lead times.
- `GET /api/masterdata/modules` — list manufacturing modules with classifications.
- `GET /api/masterdata/parts` — list individual parts with costs.
- `GET /api/masterdata/workstations` — list all workstations with type and status.

### Inventory-Service REST API (Port 8014, routed via `/api/stock/**`)

- `GET /api/stock/records` — list all stock records.
- `POST /api/stock/records` — create new stock record.
- `GET /api/stock/records/{id}` — fetch specific stock record.
- `PUT /api/stock/records/{id}` — update stock record quantity and details.
- `DELETE /api/stock/records/{id}` — delete stock record.
- `GET /api/stock/by-workstation/{workstationId}` — get all stock at a specific workstation.
- `PUT /api/stock/update/{recordId}` — quick update endpoint for stock quantity changes.

## Service Startup Order

### 1. **User Service** (Port 8012)

Provides JWT authentication required by all other services.

```powershell
cd 'e:\My Documents\DEV\Java\Project\LIFE\user-service'
.\mvnw spring-boot:run
```

### 2. **Masterdata Service** (Port 8013)

Supplies reference data for products, modules, parts, and workstations. Must be ready before Gateway.

```powershell
cd 'e:\My Documents\DEV\Java\Project\LIFE\masterdata-service'
.\mvnw spring-boot:run
```

### 3. **Inventory Service** (Port 8014)

Manages stock records. Must be ready before Gateway routes `/api/stock/**` requests.

```powershell
cd 'e:\My Documents\DEV\Java\Project\LIFE\inventory-service'
.\mvnw spring-boot:run
```

### 4. **API Gateway** (Port 8011)

Routes all frontend requests to downstream services. Requires all backend services to be running.

```powershell
cd 'e:\My Documents\DEV\Java\Project\LIFE\api-gateway'
.\mvnw spring-boot:run
```

### 5. **Frontend** (Port 5173 or 5174)

React development server. Communicates with Gateway at `http://localhost:8011`.

```powershell
cd 'e:\My Documents\DEV\Java\Project\LIFE\lego-factory-frontend'
npm run dev
```

## Database Files

Each service maintains its own H2 file-based database in the project root `data/` directory:
- `data/user_service.db` — User accounts and authentication tokens.
- `data/masterdata_service.db` — Products, modules, parts, and workstations.
- `data/inventory_service.db` — Stock records and workstation inventory.

**Note:** These files are automatically created on first run. To reset data, stop all services and delete the `data/` directory.

## Recent Implementation (Day 7)

- ✅ **Workstation Management**: Added Workstation entity, repository, service, and REST endpoints.
- ✅ **User-Workstation Assignments**: Users can now be assigned to specific workstations for role-scoped operations.
- ✅ **JWT Enhancement**: Tokens now include `workstationId` claim for context in production operations.
- ✅ **Admin UI**: React component for creating users and managing role/workstation assignments.
- ✅ **CORS Configuration**: Gateway properly handles preflight requests from frontend on ports 5173 and 5174.

## Project Roadmap

**Week 2 (Days 8-10)**: Customer order creation, fulfillment logic, and warehouse management.
**Week 3 (Days 11-15)**: Production planning, SimAL integration, and schedule optimization.
**Week 4 (Days 16-20)**: Workstation execution, real-time status updates, and production dashboards.
**Week 5 (Days 21-25)**: Prototype refinement and stakeholder showcase.
