# 🏭 LAB - Logistics Automation Bench

**Last Updated:** March 3, 2026

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](docker-compose.yml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.12-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://reactjs.org/)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=java&logoColor=white)](https://openjdk.java.net/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql&logoColor=white)](https://postgresql.org/)
[![Test Coverage](https://img.shields.io/badge/Coverage-84%25-brightgreen)](lab-backend/order-processing-service/target/site/jacoco/index.html)
[![Tests](https://img.shields.io/badge/Tests-1169%20Passing-success)](test-scenario-1.sh)
[![Production Ready](https://img.shields.io/badge/Status-Production%20Ready-brightgreen)]()

**Enterprise-Grade Manufacturing Execution System (MES)**

*A full-stack, microservice-based supply chain control platform that translates academic manufacturing research into production software. Built from ground up for a Master's thesis demonstrating digital transformation of manufacturing operations.*

[View Demo](https://lab.nji.io) • [Architecture](#️-system-architecture) • [Technical Stack](#-technology-stack) • [Documentation](#-comprehensive-documentation)

</div>

---

## 🎯 Project Overview

**LAB** (Logistics Automation Bench) is a comprehensive **Manufacturing Execution System** that digitizes and automates end-to-end supply chain operations for the LAB System. This project demonstrates the **practical application of academic research** in manufacturing systems, translating behavioral models and activity diagrams into a fully functional, production-ready software platform.

### 🎓 Academic Foundation

- **Origin**: Master's Thesis in Industrial Engineering & Supply Chain Management
- **Research Focus**: Digital transformation of manufacturing through microservice architecture
- **Contribution**: Behavioral workflow models (Activity Diagrams) defining complex supply chain interactions across 9 autonomous workstations
- **Validation**: Implements 4 distinct business scenarios from manufacturing process flows
- **Production Integration**: Ready for deployment in real manufacturing environments

### 💼 Business Value

This system addresses critical challenges in traditional manufacturing:


| **Problem** | **Solution** |
|-------------|--------------|
| ❌ Manual paper-based workflows | ✅ Fully digital order processing with real-time tracking |
| ❌ Limited production visibility | ✅ Live dashboards with 5-10 second auto-refresh |
| ❌ Data silos between workstations | ✅ Integrated microservices with RESTful APIs |
| ❌ Inventory discrepancies | ✅ Automated stock updates with transaction audit trails |
| ❌ Scalability constraints | ✅ Independently scalable microservices architecture |
| ❌ Manual scheduling inefficiencies | ✅ TEMPO scheduling engine integration with Gantt charts |

---

## 🏗️ System Architecture

### Microservices Design (7 Independent Services)

```
┌─────────────────┐
│   React SPA     │  ← Modern UI with role-based dashboards
│   (Vite + Nginx)│
└────────┬────────┘
         │
┌────────▼─────────────────────────────────────┐
│   API Gateway (Spring Cloud Gateway)         │  ← JWT validation, routing, CORS
└────────┬─────────────────────────────────────┘
         │
    ┌────┴────┬─────────┬──────────┬─────────┬──────────┬──────────┐
    │         │         │          │         │          │          │
┌───▼───┐ ┌──▼──┐ ┌────▼────┐ ┌───▼───┐ ┌───▼────┐ ┌───▼────┐ ┌──▼──────┐
│ User  │ │Master│ │Inventory│ │ Order │ │ Audit  │ │ TEMPO  │ │PostgreSQL│
│Service│ │ Data │ │ Service │ │Process│ │Service │ │Integr. │ │  (Ready) │
│ :8012 │ │:8013 │ │  :8014  │ │ :8015 │ │ :8017  │ │ :8016  │ └─────────┘
└───────┘ └──────┘ └─────────┘ └───────┘ └────────┘ └────────┘
   │         │          │           │          │          │
   └─────────┴──────────┴───────────┴──────────┴──────────┘
              H2 In-Memory Databases (Dev)
        (Audit uses H2 file-based for persistence)
          (Isolated per service - no shared DB)
```

### Key Architectural Principles

- ✅ **Service Isolation**: Each microservice has independent database (PostgreSQL for production, H2 options for dev)
- ✅ **Production Database**: PostgreSQL 15-alpine with 6 isolated databases, full persistence
- ✅ **API-Driven Communication**: Services communicate exclusively via REST APIs (no direct DB access)
- ✅ **Stateless Authentication**: JWT tokens with BCrypt-encrypted passwords
- ✅ **Single Entry Point**: All external traffic flows through nginx → API Gateway
- ✅ **Health Monitoring**: Spring Boot Actuator endpoints for each service
- ✅ **Container Orchestration**: Docker Compose for seamless multi-service deployment
- ✅ **Audit Trail**: Centralized audit-service tracks all business events (H2 file-based for compliance)

**Request Flow Example:**
```
User Login → nginx:80 → api-gateway:8011 → user-service:8012 → JWT Token
Order Creation → API Gateway → order-processing-service:8015 → inventory-service:8014
Order Confirmation → order-processing → AuditClient (async) → audit-service:8017 (compliance log)
```

---

## 💻 Technology Stack

### Backend (Java 21 + Spring Boot 3.4.12)

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Framework** | Spring Boot 3.4.12 | Microservices foundation |
| **Language** | Java 21 (LTS) | Core application logic |
| **API Gateway** | Spring Cloud Gateway | Request routing & security |
| **Authentication** | JWT (JJWT 0.12.6) | Stateless auth tokens |
| **Security** | Spring Security + BCrypt | Password encryption & RBAC |
| **Audit Logging** | Audit-service (:8017) | Centralized compliance tracking |
| **Database (Dev)** | H2 In-Memory / File | Development & testing |
| **Database (Prod)** | PostgreSQL 15-alpine | 6 isolated databases, production-ready |
| **Database (Audit)** | H2 File-based | Independent compliance storage |
| **ORM** | Spring Data JPA | Database abstraction |
| **HTTP Client** | RestTemplate | Inter-service communication |
| **Async Processing** | ThreadPoolTaskExecutor | Non-blocking audit logging |
| **Build Tool** | Maven | Dependency management |

### Frontend (React 18 + Vite)

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Framework** | React 18 | Modern UI components |
| **Build Tool** | Vite | Fast HMR & bundling |
| **HTTP Client** | Axios | API requests with interceptors |
| **Routing** | React Router v6 | SPA navigation |
| **State** | Context API | Global auth state |
| **Styling** | CSS Modules + Design System | 368 design tokens, reusable components |
| **Web Server** | Nginx | Production static file serving |

### DevOps & Infrastructure

- **Containerization**: Docker + Docker Compose
- **Reverse Proxy**: Nginx (port routing, load balancing ready)
- **CI/CD Ready**: Dockerfile per service, multi-stage builds
- **Logging**: SLF4J + Logback with structured logging
- **Code Quality**: SonarQube integration
- **Version Control**: Git with feature branch workflow

---

## ✨ Core Features

### 🔐 Advanced Authentication & Authorization

- **JWT-Based Authentication**: Secure, stateless token management with 1-hour expiration
- **9 Specialized Roles**: ADMIN, PLANT_WAREHOUSE, MODULES_SUPERMARKET, PRODUCTION_PLANNING, PRODUCTION_CONTROL, ASSEMBLY_CONTROL, PARTS_SUPPLY, MANUFACTURING, VIEWER
- **Workstation-Based Access**: Users assigned to specific factory workstations (WS-1 through WS-9)
- **Protected Routes**: Automatic token refresh, expiration handling, 401/403 auto-logout

### 🏭 Manufacturing Supply Chain (3-Tier Hierarchy)

**Entity Relationship Model:**
```
Product Variants (Final Products) → Stored in Plant Warehouse (WS-7)
    └── Composed of → Modules (Sub-assemblies) → Stored in Modules Supermarket (WS-8)
            └── Composed of → Parts (Raw Materials) → Stored in Parts Supply (WS-9)
```

**9 Workstations:**
- **WS-1**: Injection Molding (Manufacturing)
- **WS-2**: Parts Pre-Production (Manufacturing)
- **WS-3**: Part Finishing (Manufacturing)
- **WS-4**: Gear Assembly (Assembly Station)
- **WS-5**: Motor Assembly (Assembly Station)
- **WS-6**: Final Assembly (Assembly Station)
- **WS-7**: Plant Warehouse (Customer Fulfillment)
- **WS-8**: Modules Supermarket (Internal Warehouse)
- **WS-9**: Parts Supply Warehouse (Raw Materials)

### 📦 Order Processing (4 Business Scenarios)

Implements 4 distinct fulfillment workflows from thesis research:

1. **Scenario 1: Sunny Day** ✅ - Direct fulfillment from warehouse stock
2. **Scenario 2: Warehouse Order** ✅ - Missing products trigger module assembly
3. **Scenario 3: Full Production** ✅ - Missing modules trigger manufacturing chain
4. **Scenario 4: High Volume** ✅ - Large orders (≥ LOT_SIZE_THRESHOLD) bypass warehouse, go direct to production

**Recent Enhancements (February 2026):**
- ✅ **Production Order Linking**: Warehouse orders link to production via `productionOrderId` field, preventing cross-order interference
- ✅ **Automatic Completion**: Production orders auto-complete and trigger downstream processing (no manual submission)
- ✅ **Direct Fulfillment Bypass**: Orders with linked production skip stock checks (modules already reserved)
- ✅ **Frontend Smart Buttons**: Status-aware action buttons based on backend `triggerScenario` field
- ✅ **Configuration Externalization**: All settings externalized via `@ConfigurationProperties`
- ✅ **Spring Profiles**: `dev`, `prod`, `cloud` profiles for environment-specific configuration
- ✅ **Registry Deployment**: Server deployment from Docker registry (192.168.0.237:5010)
- ✅ **API Metrics & Monitoring** (Feb 11): Spring Boot Actuator + Micrometer Prometheus integration across all 7 services
- ✅ **Browser-Accessible Metrics**: All metrics exposed via API Gateway at `http://localhost:1011/api/metrics/*`

**Order State Machines:**
```
CustomerOrder:  PENDING → CONFIRMED → PROCESSING → COMPLETED → DELIVERED
WarehouseOrder: PENDING → CONFIRMED → FULFILLED (with productionOrderId link if needed)
ProductionOrder: PENDING → PLANNED → IN_PRODUCTION → COMPLETED (auto-triggers downstream)
```

### 📊 Real-Time Inventory Management

- **Multi-Location Tracking**: Independent stock per workstation
- **Transaction Audit Trail**: Immutable StockLedgerEntry for every movement (CREDIT/DEBIT/TRANSFER)
- **Low Stock Alerts**: Configurable thresholds with automatic notifications
- **Atomic Updates**: Database transactions ensure inventory consistency
- **Live Dashboard**: Auto-refresh every 5-10 seconds

### 📈 Observability & Monitoring (NEW - Feb 11, 2026)

- **Prometheus Metrics Export**: All services expose metrics in Prometheus format
- **Custom Business Metrics**: Domain-specific metrics (orders, inventory ops, auth events, etc.)
- **Request Tracing**: UUID-based correlation IDs for distributed tracing
- **Performance Monitoring**: HTTP request duration, JVM metrics, database connection pools
- **Browser-Accessible Endpoints**: Metrics available via API Gateway without authentication
  - Gateway: `http://localhost:1011/api/metrics/gateway`
  - User Service: `http://localhost:1011/api/metrics/user`
  - Inventory: `http://localhost:1011/api/metrics/inventory`
  - Orders: `http://localhost:1011/api/metrics/orders`
  - (+ masterdata, tempo, audit)
- **Integration Ready**: Prometheus scrape targets and Grafana dashboard templates available
- **Documentation**: See [BROWSER_METRICS_ACCESS.md](BROWSER_METRICS_ACCESS.md) for complete details

### 🗓️ Production Planning & Scheduling (P2.3 - Advanced)

- **Multiple Scheduling Strategies**: FIFO, Priority-based, Capacity-aware algorithms
- **TEMPO Integration**: External scheduling engine for production optimization
- **What-If Simulation**: Preview schedules before committing changes
- **Gantt Chart Visualization**: Interactive timeline with real-time WebSocket updates
- **Capacity Management**: Workstation utilization tracking and load balancing
- **Conflict Detection**: Automatic identification of scheduling overlaps
- **Manual Scheduling**: Drag-and-drop interface for production planners
- **Control Order Generation**: Auto-create ProductionControlOrder and AssemblyControlOrder from schedules
- **Real-Time Updates**: Task status changes broadcast via `/topic/schedules` WebSocket topic

**Scheduling API Endpoints:**
- `POST /api/scheduler/schedule` - Schedule production orders
- `POST /api/scheduler/simulate` - Run what-if simulation
- `GET /api/scheduler/capacity/{wsId}` - Get workstation capacity
- `GET /api/scheduler/strategies` - List available strategies

**Configuration:**
```properties
scheduler.strategy=capacity  # Options: fifo, priority, capacity
scheduler.capacity.default-max-concurrent=1
scheduler.capacity.planning-horizon-hours=168
```

See [P2.3_ADVANCED_SCHEDULING.md](_dev-docs/P2.3_ADVANCED_SCHEDULING.md) for comprehensive documentation.

### 📱 Role-Specific Dashboards

**9 Customized Interfaces** using standardized dashboard architecture (`StandardDashboardLayout` + unified dashboard wrappers):
- **Admin**: System KPIs, user management, configuration
- **Plant Warehouse**: Customer order intake & fulfillment
- **Modules Supermarket**: Internal warehouse request handling
- **Production Planning**: Factory-wide scheduling with Gantt charts
- **Production Control**: Manufacturing task dispatching
- **Assembly Control**: Assembly operation coordination
- **Parts Supply**: Raw materials distribution
- **Manufacturing**: Production line execution (WS-1, WS-2, WS-3)
- **Viewer**: Read-only monitoring dashboard

**Each dashboard includes:**
- 📊 Real-time statistics (Total, Pending, In Progress, Completed orders)
- 📋 Interactive order cards with status badges
- 📦 Live inventory display for assigned workstation
- ✅ One-click action buttons (Confirm, Fulfill, Start, Complete)
- 🔄 Auto-refresh with configurable intervals

---

## 🚀 Quick Start

### Prerequisites

- **Docker** 20.10+ & **Docker Compose** 2.0+
- **Java 21** (for local development)
- **Node.js 18+** (for frontend development)
- **Git**

### One-Command Deployment (Development)

```bash
# Clone repository
git clone <repository-url>
cd lab40

# Start all services (builds locally)
docker-compose up -d
```

### Server Deployment (Production)

For production servers using pre-built Docker images from registry:

```bash
# On your server (e.g., 192.168.0.237)
git clone -b prod <repository-url>
cd lab40/deploy

# First-time setup
./setup.sh

# Pull images and start
./update.sh
```

**Registry-based deployment:**
- Uses pre-built images from `192.168.0.237:5010`
- No source code compilation on server
- Quick updates: just `./update.sh`

**Access Application:**
- Frontend: `http://localhost:1011` (or `:80` if `NGINX_ROOT_PROXY_EXTERNAL_PORT=80`)
- API Gateway: `http://localhost:8011`

### Test Accounts

| Username | Password | Role | Workstation | Use Case |
|----------|----------|------|-------------|----------|
| `lab_admin` | `password` | ADMIN | - | System administration |
| `warehouse_operator` | `password` | PLANT_WAREHOUSE | Plant Warehouse (WS-7) | Customer order fulfillment |
| `modules_supermarket` | `password` | MODULES_SUPERMARKET | Modules Supermarket (WS-8) | Module warehouse operations |
| `production_planning` | `password` | PRODUCTION_PLANNING | - | Factory-wide scheduling |
| `production_control` | `password` | PRODUCTION_CONTROL | - | Manufacturing oversight |
| `assembly_control` | `password` | ASSEMBLY_CONTROL | - | Assembly coordination |
| `injection_molding` | `password` | MANUFACTURING | Injection Molding (WS-1) | Part manufacturing |
| `parts_preproduction` | `password` | MANUFACTURING | Parts Pre-Production (WS-2) | Part manufacturing |
| `part_finishing` | `password` | MANUFACTURING | Part Finishing (WS-3) | Part manufacturing |
| `gear_assembly` | `password` | MANUFACTURING | Gear Assembly (WS-4) | Module assembly |
| `motor_assembly` | `password` | MANUFACTURING | Motor Assembly (WS-5) | Module assembly |
| `final_assembly` | `password` | MANUFACTURING | Final Assembly (WS-6) | Product assembly |
| `parts_supply` | `password` | PARTS_SUPPLY | Parts Supply (WS-9) | Raw materials distribution |

---

## 📊 Monitoring & Observability

**LAB System includes comprehensive monitoring via Prometheus + Grafana (P0.2 Complete)**

### Access Dashboards

| Environment | Grafana URL | Prometheus URL | Credentials |
|-------------|-------------|----------------|-------------|
| **Local Dev (HTTPS)** | https://localhost:1443/grafana | http://localhost:9090 | admin / LifeAdmin2026!Secure |
| **Production** | https://lab.nji.io/grafana | https://lab.nji.io/prometheus | admin / LifeAdmin2026!Secure (Prometheus: prometheus-admin / Prom2026!Secure) |

### Pre-Configured Dashboards

1. **LAB System Overview** (10s refresh)
   - Service health & uptime
   - Request rates & error rates
   - JVM memory & CPU usage
   - Database connection pools

2. **LAB Business Operations** (5s refresh)
   - Order processing metrics
   - Inventory levels by workstation
   - Authentication success/failure rates
   - Active user sessions

3. **LAB Performance Metrics** (30s refresh)
   - Response time percentiles (P50, P95, P99)
   - Database query performance
   - Cache hit rates
   - Endpoint-specific latency

**Learn More:** See [Monitoring Guide](docs/monitoring/MONITORING_GUIDE.md) for detailed metric definitions, alert runbooks, and PromQL examples.

---

## 🧪 Testing

### Test Coverage

**Status (February 2026):** JaCoCo coverage reporting enabled across all services. **Phase 1-3 complete** (infrastructure + cache tests). Phase 4 in progress (comprehensive coverage to 70%).

| Service | Test Files | JaCoCo Enabled | Coverage Target |
|---------|------------|----------------|-----------------|
| order-processing-service | 36+ | ✅ | 85% (best in class) |
| inventory-service | 5 | ✅ | 75% |
| masterdata-service | 7 | ✅ | 80% |
| user-service | 7 | ✅ | 80% |
| tempo-service | 6 | ✅ | 70% |
| api-gateway | 2 | ✅ | 65% |
| audit-service | - | ✅ | 60% |

**Total Test Files:** 54 across all services  
**Overall Target:** 70%+ coverage by March 2026

### Running Tests

```bash
# Run all tests for a service
cd lab-backend/order-processing-service
./mvnw test

# Run tests with coverage report (JaCoCo)
./mvnw clean test jacoco:report
# View: target/site/jacoco/index.html

# Run all backend tests with coverage
cd lab-backend
./mvnw clean test jacoco:report

# Run specific test class
./mvnw test -Dtest="CustomerOrderServiceTest"

# Run scenario validation scripts
./tests/scenario-1.sh  # Direct fulfillment
./tests/scenario-2.sh  # Warehouse order
./tests/scenario-3.sh  # Full production
./tests/scenario-4.sh  # High-volume production
```

### Test Categories

| Category | Description | Location |
|----------|-------------|----------|
| **Unit Tests** | Service layer logic, mock dependencies | `src/test/java/**/service/` |
| **Integration Tests** | Spring context + H2 database | `src/test/java/**/integration/` |
| **E2E Tests** | Full scenario workflows | `src/test/java/**/integration/Scenario*E2ETest.java` |
| **Cache Tests** | Redis behavior (hit/miss/eviction) | **26 tests** (user: 8, inventory: 8, tempo: 10) |
| **Controller Tests** | REST endpoint validation | `src/test/java/**/controller/` |
| **Scenario Scripts** | Shell-based validation | `test-scenario-*.sh` |

---

## 📚 Documentation

**Complete documentation is organized in the [docs/](docs/) folder.**

### Quick Links

| Category | Key Documents |
|----------|---------------|
| **Getting Started** | [README.md](README.md) • [copilot-instructions.md](.github/copilot-instructions.md) |
| **Architecture** | [Business Scenarios](docs/architecture/BUSINESS_SCENARIOS.md) • [Microservices Overview](docs/architecture/MicroServices.md) |
| **Deployment** | [Deployment Guide](docs/deployment/DEPLOYMENT_GUIDE.md) • [Server Updates](docs/deployment/SERVER_UPDATE_GUIDE.md) |
| **Database** | [Database Guide](docs/database/DATABASE_GUIDE.md) (PostgreSQL + H2 profiles) |
| **Development** | [Dashboard Components](docs/development/DASHBOARD_COMPONENTS.md) • [Testing Guide](docs/development/TESTING.md) |
| **Features** | [Advanced Scheduling](docs/features/ADVANCED_SCHEDULING.md) • [Product BOM](docs/features/PRODUCT_BOM.md) |
| **Monitoring** | [Monitoring Guide](docs/monitoring/MONITORING_GUIDE.md) • [Metrics Access](docs/monitoring/BROWSER_METRICS_ACCESS.md) |
| **Security** | [Security Hardening](docs/security/SECURITY_HARDENING.md) |
| **Roadmap** | [System Roadmap](docs/ROADMAP.md) (All P0/P1/P2 milestones complete) |

### AI Agent References

For AI agents and detailed technical patterns, see [.github/copilot-refs/](.github/copilot-refs/):
- [API Reference](.github/copilot-refs/API_REFERENCE.md)
- [Backend Patterns](.github/copilot-refs/BACKEND_PATTERNS.md)
- [Frontend Patterns](.github/copilot-refs/FRONTEND_PATTERNS.md)
- [Order Processing Rules](.github/copilot-refs/ORDER_PROCESSING_RULES.md)
- [Roles & Workstations](.github/copilot-refs/ROLES_AND_WORKSTATIONS.md)
- [Troubleshooting](.github/copilot-refs/TROUBLESHOOTING.md)
- [Dev Workflow](.github/copilot-refs/DEV_WORKFLOW.md)

---

## 🔧 Development Workflow

### Local Backend Development

```bash
cd lab-backend/<service>
./mvnw spring-boot:run
```

### Local Frontend Development (with Hot Reload)

```bash
# Start backend services only
docker-compose up -d api-gateway user-service masterdata-service inventory-service order-processing-service tempo-service

# Run frontend in dev mode
cd lab-frontend
npm install
npm run dev  # Access at http://localhost:5173
```

### Rebuild After Code Changes

```bash
# Rebuild specific service
docker-compose build --no-cache <service> && docker-compose up -d <service>

# Rebuild frontend (CRITICAL for seeing CSS/component changes)
docker-compose build --no-cache frontend && docker-compose up -d frontend
# Then hard refresh browser: Ctrl+Shift+R (Windows/Linux) or Cmd+Shift+R (Mac)
```

---

## 🎨 Design System

**368 CSS Design Tokens** in centralized [variables.css](lab-frontend/src/styles/variables.css):
- 🎨 Color palette (primary, secondary, success, danger, warning, info)
- 📏 Spacing scale (4px baseline grid)
- 🔤 Typography system (Inter font family)
- 🎭 Shadows & elevations
- ⏱️ Animation timings
- 🖼️ Border radius variants

**10+ Reusable Components** with CSS Modules:
- `Button` (8 variants × 3 sizes)
- `Card` (elevated, outlined, flat)
- `StatCard` (dashboard metrics)
- `Table` (sortable, striped, hoverable)
- `Badge` (status indicators)
- `Alert` (success, error, warning, info)
- `StandardDashboardLayout` (shared dashboard foundation)
- `WorkstationDashboard` / `WarehouseDashboard` / `ControlDashboard` (role/workstation wrappers)

---

## 📊 Key Technical Achievements

### 1. **Microservice Isolation**
- 8 backend microservices: user, masterdata, inventory, order-processing, tempo, audit, websocket + api-gateway (entry point)
- Each service has an independent database (PostgreSQL in production, H2 in development)
- Services communicate exclusively via REST APIs over Docker DNS
- Demonstrated proper bounded context separation

### 2. **Security Implementation**
- JWT with 1-hour expiration
- BCrypt password hashing
- API Gateway-level authentication filter
- Role-based route protection in frontend

### 3. **Data Consistency**
- RestTemplate inter-service calls with error handling
- Transactional inventory updates
- Audit trail for all stock movements

### 4. **Audit & Compliance (Phase 0.3 - Feb 2026)**
- Centralized audit-service for cross-service event tracking
- Async audit logging via AuditClient (non-blocking business operations)
- Persistent storage with H2 file-based database
- Graceful failure handling (audit errors don't fail business transactions)
- 20+ indexed fields for compliance reporting

### 5. **Frontend Architecture**
- Context API for global state (auth)
- Axios interceptors for automatic JWT injection
- CSS Modules for scoped styling
- Standardized dashboard architecture (`DashboardHeader`, `BaseDashboard`, wrapper dashboards)

### 6. **Operational Readiness**
- Health check endpoints (`/actuator/health`)
- Structured logging (SLF4J)
- Docker Compose orchestration
- Multi-stage Dockerfile builds

---

## 🛣️ Future Enhancements

**Recently Completed (February–March 2026):**
- [x] **PostgreSQL Migration (P0.1)**: Production-grade persistence with isolated databases per service (Feb 12)
- [x] **API Metrics & Monitoring (P0.2)**: Prometheus metrics + Grafana dashboards across all microservices (Feb 11-17)
- [x] **Audit Service Extraction (P0.3)**: Centralized audit microservice for cross-service compliance logging (Feb 9)
- [x] **Security Hardening (P0.3)**: HTTPS/TLS, rate limiting, Grafana/Prometheus authentication, CORS (Feb 12-17)
- [x] **Redis Caching Enhancement (P2.1)**: Expanded Redis caching to all 5 data services with custom TTLs (Feb 17)
- [x] **Test Infrastructure (P2.2)**: JaCoCo coverage on 6 services, 26 Redis cache integration tests (Feb 17)
- [x] **CI/CD Pipeline (P1.2)**: GitHub Actions for PR validation and automated deployment (Feb 18)
- [x] **Config Governance (P1: phase1-config-governance)**: Gateway route hardening, secret management (Feb)
- [x] **Structured Logging & SLO Baseline (P1: phase1-observability-hardening)**: Correlation IDs, SLO metrics (Feb)
- [x] **Async Event Backbone (P2: phase2-event-backbone)**: Redis Pub/Sub, thread pool hardening (Feb)
- [x] **Resilience Hardening (P2: phase2-resilience-hardening)**: Circuit breaker/retry properly wired, fault injection tests (Feb)
- [x] **CI Supply Chain Controls (P2: phase2-ci-supply-chain-hardening)**: Docker/CI build optimization (Feb)
- [x] **Advanced Scheduling (P2.3)**: FIFO/Priority/Capacity strategies, per-workstation strategy cascade, Gantt charts (Feb 26)
- [x] **Product BOM Configuration (P3.6)**: GUI for BOM editing, versioning, what-if analysis (Feb 21)
- [x] **Analytics Read Model (P3.3)**: KPI projection pipeline, analytics REST endpoints (Mar 4)
- [x] **Scripts Reorganization**: `scripts/`, `tests/`, `scripts/server/` directory structure (Mar 4)
- [x] **Registry Deployment Pipeline**: All 10 services pushed to `192.168.0.237:5010`; server running confirmed (Mar 4)
- [x] **Server Config Sync**: `docker-compose-server.yml`, `.env.server`, `update.sh` deployed to production server (Mar 4)

**Planned (Nice to Have):**
- [ ] **Kubernetes Deployment**: Helm charts for cloud-native scaling
- [ ] **Kafka Event Streaming**: Real-time event-driven architecture
- [ ] **OAuth2/OIDC**: Enterprise SSO integration
- [ ] **Mobile App**: React Native companion app for shop floor
- [ ] **AI-Powered Scheduling**: ML optimization for production planning

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Master's Thesis Supervisors**: For guidance on manufacturing system design
- **Spring Boot Team**: Excellent microservices framework
- **React Community**: Vibrant ecosystem and tooling
- **LAB System**: Inspiration and use case validation
- **Open Source Community**: Libraries and tools that made this possible

---

## 📧 Contact

**Author**: Nji S. Chifen
**LinkedIn**: www.linkedin.com/in/njisama
**Email**: mail@nji.io 
**Portfolio**: https://nji.io

**For Academic/Research Inquiries**: See [docs/architecture/MicroServices.md](docs/architecture/MicroServices.md) and [docs/architecture/BUSINESS_SCENARIOS.md](docs/architecture/BUSINESS_SCENARIOS.md) for technical and workflow details.

---

<div align="center">

**⭐ If you find this project impressive, please star it! ⭐**

Built with passion for manufacturing innovation and software engineering excellence.

[⬆ Back to Top](#-lab---logistics-automation-bench)

</div># Test deploy
