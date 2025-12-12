# LEGO Sample Factory Control System

A comprehensive **microservice-based manufacturing control platform** that digitizes and automates the supply chain operations of the LEGO Sample Factory. The system replaces manual, paper-heavy processes with intelligent digital workflows that manage production orders, inventory, workstation assignments, and real-time operational dashboards.

## Overview

LIFE (LEGO Integrated Factory Execution) is a production-ready prototype designed to:

- **Manage multi-stage manufacturing**: From order creation through modules assembly to final product delivery
- **Track inventory in real-time**: Stock management across multiple workstations and warehouse locations
- **Optimize production scheduling**: Intelligent workstation assignment and task sequencing
- **Enable role-based operations**: Specialized dashboards for plant warehouse, modules supermarket, and manufacturing teams
- **Provide system observability**: Comprehensive error handling, structured logging, and performance monitoring

## System Architecture

LIFE uses a **6-tier microservice architecture** with an API Gateway as the central routing layer:
┌────────────────────────────────────────────────────────────┐
│           React Frontend (Port 5173/5174)                  │
│  ┌──────────────┬──────────────┬─────────────────────────┐ │
│  │ Dashboard    │ Products     │ Workstation Pages       │ │
│  │ (Multi-role) │ Catalog      │ (Plant WH, Modules SM)  │ │
│  └──────────────┴──────────────┴─────────────────────────┘ │
└──────────────────────────--─┬──────────────────────────────┘
                              │
             ┌───────────────────▼─────────────┐
             │      API Gateway (Port 8011)    │
             │      - Route all requests       │
             │      - CORS support             │
             │      - Load balancing           │
             └───┬──────┬──────┬───────┬────┬──┘
                 │      │      │       │    │
           ┌────▼──┐┌─▼────┐┌──▼──┐┌─▼──-┐ ┌──▼──┐
           │ User  ││Master││Stock││Order│ │Simal│
           │Service││data  ││ mgmt││Proc.│ │Integ│
           │ 8012  ││ 8013 ││8014 ││ 8015│ │ 8016│
           └───────┘└──────┘└─────┘└───-─┘ └─────┘

**Backend Services**:

- **User Service** (Port 8012): Authentication, authorization, user management
- **Masterdata Service** (Port 8013): Product catalog, modules, parts, workstations
- **Inventory Service** (Port 8014): Stock tracking and workstation inventory
- **Order Processing Service** (Port 8015): Customer orders, fulfillment, warehouse operations
- **SimAL Integration Service** (Port 8016): Production scheduling and simulation

**Persistence**: Each microservice maintains its own H2 file-based database for complete data isolation.

## Technology Stack

- **Backend**: Java 21, Spring Boot 3.4.2, Spring Cloud Gateway 2024.0.0, Spring Security, Maven
- **Database**: H2 (file-based, one per service) for data isolation and easy deployment
- **Frontend**: React 18+, Vite, Axios, React Router
- **Tools**: Visual Studio Code, Node.js, npm

## Project Structure

```plaintext
lego-sample-factory/
├── api-gateway/                    # API Gateway service (Port 8011)
│   ├── src/main/
│   │   ├── java/com/lego/factory/gateway/
│   │   └── resources/
│   │       ├── application.yml     # Gateway routing configuration
│   │       └── application-{env}.yml
│   └── pom.xml
├── user-service/                   # User management service (Port 8012)
│   ├── src/main/
│   │   ├── java/com/lego/factory/user/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── data.sql           # Initial user data
│   │       └── schema.sql
│   └── pom.xml
├── masterdata-service/            # Product catalog service (Port 8013)
│   ├── src/main/
│   │   ├── java/com/lego/factory/masterdata/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── data.sql           # Product variants, modules, parts
│   │       └── schema.sql
│   └── pom.xml
├── inventory-service/             # Stock management service (Port 8014)
│   ├── src/main/
│   │   ├── java/com/lego/factory/inventory/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── data.sql           # Initial stock records
│   │       └── schema.sql
│   └── pom.xml
├── order-processing-service/      # Order management service (Port 8015)
│   ├── src/main/
│   │   ├── java/com/lego/factory/orders/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── schema.sql
│   └── pom.xml
├── simal-integration-service/     # Production scheduling service (Port 8016)
│   ├── src/main/
│   │   ├── java/com/lego/factory/simal/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── schema.sql
│   └── pom.xml
├── lego-factory-frontend/         # React frontend application
│   ├── src/
│   │   ├── components/            # Reusable UI components
│   │   ├── pages/                 # Route-specific pages
│   │   ├── services/              # API communication
│   │   └── utils/                 # Helper functions
│   ├── package.json
│   └── vite.config.js
├── logs/                          # Centralized log storage
│   ├── application.log            # General application logs
│   ├── error.log                  # Error-specific logs
│   └── debug.log                  # Debug-level logs
├── data/                          # H2 database files
│   ├── user-service.mv.db
│   ├── masterdata-service.mv.db
│   └── [other-service-dbs]
└── README.md


## Current Features & Status

### ✅ Implemented Features

#### Authentication & Authorization

- JWT-based authentication for all API requests
- Role-based access control (ADMIN, PLANT_WAREHOUSE, MODULES_SUPERMARKET, MANUFACTURING_OPERATOR)
- User management dashboard (create, update, delete, assign workstations)
- Automatic session management with localStorage

#### Product & Inventory Management

- Product variants catalog with pricing and production time estimates
- Modular component structure (products → modules → parts)
- Real-time inventory tracking by workstation
- Stock record management with item type classification

#### Order Processing & Fulfillment

- Customer order creation with multiple order items
- Order status lifecycle (PENDING → CONFIRMED → PROCESSING → COMPLETED/CANCELLED)
- Warehouse order management for inter-warehouse transfers
- Fulfill/reject operations with automatic inventory adjustments

#### Workstation Operations

- Multi-role workstation dashboards:
  - **Admin Dashboard**: System-wide KPIs, user management, workstation configuration
  - **Plant Warehouse**: Incoming customer orders, fulfillment actions
  - **Modules Supermarket**: Warehouse request handling, inventory fulfillment
  - **Manufacturing Workstations**: Task execution pages for manufacturing and assembly
- Real-time order/task updates with 15-30 second auto-refresh

#### Production Scheduling (SimAL)

- Intelligent workstation allocation based on work type
- Task sequencing with ISO 8601 timestamps
- Order-to-schedule linking with realistic time estimates

#### Error Handling & Observability

- Global exception handlers with standardized JSON error responses
- Structured logging with rolling file appenders (application.log, error.log, debug.log)
- Frontend toast notifications for user-facing error feedback
- Stack trace logging for debugging

#### UI/UX Features

- Compact, responsive grid layouts for product and order displays
- Color-coded status badges and item-type indicators
- Expandable component details (products show modules, modules show parts)
- Mobile-friendly design with adaptive font sizes and spacing
- Reduced header height (60% of original) for better screen utilization

## Setup & Running the Application

### Prerequisites

- **Java 21** (Eclipse Adoptium or equivalent)
- **Node.js 18+** and npm
- **PowerShell** or Bash (for running Maven and npm commands)

### Build All Services

From the project root directory, build all backend services:

```powershell
cd "e:\My Documents\DEV\Java\Project\LIFE"

# Build each service
cd user-service; .\mvnw clean package -DskipTests; cd ..
cd masterdata-service; .\mvnw clean package -DskipTests; cd ..
cd inventory-service; .\mvnw clean package -DskipTests; cd ..
cd order-processing-service; .\mvnw clean package -DskipTests; cd ..
cd simal-integration-service; .\mvnw clean package -DskipTests; cd ..
cd api-gateway; .\mvnw clean package -DskipTests; cd ..


## Configuration Management

### Environment-Specific Configuration

Each service uses **Spring Boot profiles** for environment-specific configuration:

- **application.yml**: Base configuration shared across all environments
- **application-dev.yml**: Development environment settings
- **application-prod.yml**: Production environment settings
- **application-test.yml**: Testing environment settings

### Key Configuration Areas

**Database Configuration**:
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/${spring.application.name}
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

**Service Discovery** (API Gateway):

spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8012
          predicates:
            - Path=/api/users/**,/api/auth/**

**Security Configuration**:

jwt:
  secret: ${JWT_SECRET:default-secret-key}
  expiration: 86400000  # 24 hours

**Logging Configuration**:
```yaml
logging:
  level:
    com.lego.factory: DEBUG
    org.springframework.security: DEBUG
  file:
    name: ./logs/application.log
```

### Environment Variables

Set these environment variables for production deployments:

- `JWT_SECRET`: Custom JWT signing key
- `SPRING_PROFILES_ACTIVE`: Active profile (dev, prod, test)
- `DATABASE_PATH`: Custom database file location
- `LOG_LEVEL`: Application logging level



### Start Backend Services

Open **separate terminals** for each service, in this order:

1. **User Service** (Port 8012) - Required first for authentication:

```powershell
cd "e:\My Documents\DEV\Java\Project\LIFE\user-service"
.\mvnw spring-boot:run
```

1. **Masterdata Service** (Port 8013) - Required before gateway:

```powershell
cd "e:\My Documents\DEV\Java\Project\LIFE\masterdata-service"
.\mvnw spring-boot:run
```

1. **Inventory Service** (Port 8014):

```powershell
cd "e:\My Documents\DEV\Java\Project\LIFE\inventory-service"
.\mvnw spring-boot:run
```

1. **Order Processing Service** (Port 8015):

```powershell
cd "e:\My Documents\DEV\Java\Project\LIFE\order-processing-service"
.\mvnw spring-boot:run
```

1. **SimAL Integration Service** (Port 8016):

```powershell
cd "e:\My Documents\DEV\Java\Project\LIFE\simal-integration-service"
.\mvnw spring-boot:run
```

1. **API Gateway** (Port 8011) - Routes all requests:

```powershell
cd "e:\My Documents\DEV\Java\Project\LIFE\api-gateway"
.\mvnw spring-boot:run
```

### Start Frontend

In a new terminal, start the React development server:

```powershell
cd "e:\My Documents\DEV\Java\Project\LIFE\lego-factory-frontend"
npm install  # First time only
npm run dev
```

The frontend will be available at `http://localhost:5173`


## Health Monitoring & Observability

### Health Check Endpoints

All services expose **Spring Boot Actuator** health endpoints for monitoring and load balancer integration:

**Health Check URLs**:
- API Gateway: `http://localhost:8011/actuator/health`
- User Service: `http://localhost:8012/actuator/health`
- Masterdata Service: `http://localhost:8013/actuator/health`
- Inventory Service: `http://localhost:8014/actuator/health`
- Order Processing Service: `http://localhost:8015/actuator/health`
- SimAL Integration Service: `http://localhost:8016/actuator/health`

**Health Response Format**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 21474836480,
        "threshold": 10485760
      }
    }
  }
}
```

### Available Health Indicators

- **Database Health**: H2 connection status and query validation
- **Disk Space**: Available storage monitoring
- **Custom Business Logic**: Service-specific health checks
- **Memory Usage**: JVM heap and non-heap memory status

### Additional Actuator Endpoints

Enable additional monitoring endpoints by adding to `application.yml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

**Available Endpoints**:
- `/actuator/info` - Application information and build details
- `/actuator/metrics` - Application metrics (requests, memory, etc.)
- `/actuator/prometheus` - Prometheus-formatted metrics
- `/actuator/loggers` - Runtime log level management

### Production Monitoring

For production deployments, integrate with:

- **Load Balancers**: Use health endpoints for automatic failover
- **Monitoring Tools**: Prometheus, Grafana, or Application Insights
- **Alerting**: Set up alerts for service DOWN status or high error rates
- **Log Aggregation**: Centralize logs using ELK stack or similar

// ...existing code...

### Default Test Accounts

- **Admin Account**: `legoAdmin` / `legoPass`
  - Full system access, user management, workstation configuration

- **Plant Warehouse**: `warehouseOperator` / `warehousePass`
  - Access to plant warehouse operations and customer order fulfillment

- **Modules Supermarket**: `modulesSupermarketOp` / `modulesPass`
  - Warehouse order management and module inventory fulfillment


### Default Test Accounts

- **Admin Account**: `legoAdmin` / `legoPass`
  - Full system access, user management, workstation configuration

- **Plant Warehouse**: `warehouseOperator` / `warehousePass`
  - Access to plant warehouse operations and customer order fulfillment

- **Modules Supermarket**: `modulesSupermarketOp` / `modulesPass`
  - Warehouse order management and module inventory fulfillment

## API Endpoints Summary

All endpoints are routed through the API Gateway at `http://localhost:8011`

**Authentication**: `POST /api/auth/login` — Submit username/password, receive JWT token

**User Management** (Admin-only):

- `GET /api/users` — List all users
- `POST /api/users` — Create new user
- `PUT /api/users/{id}` — Update user
- `DELETE /api/users/{id}` — Delete user

**Master Data** (All authenticated users):

- `GET /api/masterdata/product-variants` — Product catalog
- `GET /api/masterdata/modules` — Manufacturing modules
- `GET /api/masterdata/parts` — Component parts
- `GET /api/masterdata/workstations` — Workstation configuration

**Inventory Management**:

- `GET /api/stock/records` — All stock records
- `GET /api/stock/by-workstation/{workstationId}` — Workstation inventory
- `PUT /api/stock/records/{id}` — Update stock

**Order Processing** (Plant Warehouse role):

- `POST /api/customer-orders` — Create order
- `GET /api/customer-orders` — List orders
- `PATCH /api/customer-orders/{id}/status` — Update status

**Warehouse Orders** (Modules Supermarket role):

- `GET /api/warehouse-orders` — Pending orders
- `POST /api/warehouse-orders/{id}/fulfill` — Fulfill order
- `POST /api/warehouse-orders/{id}/reject` — Reject order

**Production Scheduling**:

- `POST /api/simal/production-order` — Submit production order
- `GET /api/simal/scheduled-orders` — View schedules
