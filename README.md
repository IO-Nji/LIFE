# 🏭 LIFE - LEGO Integrated Factory Execution System

<div align="center">

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](docker-compose.yml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.12-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://reactjs.org/)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=java&logoColor=white)](https://openjdk.java.net/)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-orange)]()

**Enterprise-Grade Manufacturing Execution System (MES)**

*A full-stack, microservice-based supply chain control platform that translates academic manufacturing research into production software. Built from ground up for a Master's thesis demonstrating digital transformation of manufacturing operations.*

[View Demo](#-quick-start) • [Architecture](#-system-architecture) • [Technical Stack](#-technology-stack) • [Documentation](#-comprehensive-documentation)

</div>

---

## 🎯 Project Overview

**LIFE** (LEGO Integrated Factory Execution) is a comprehensive **Manufacturing Execution System** that digitizes and automates end-to-end supply chain operations for the LEGO Sample Factory. This project demonstrates the **practical application of academic research** in manufacturing systems, translating behavioral models and activity diagrams into a fully functional, production-ready software platform.

### 🎓 Academic Foundation

- **Origin**: Master's Thesis in Industrial Engineering & Supply Chain Management
- **Research Focus**: Digital transformation of manufacturing through microservice architecture
- **Contribution**: Behavioral workflow models (Activity Diagrams) defining complex supply chain interactions across 9 autonomous workstations
- **Validation**: Implements 4 distinct business scenarios from manufacturing process flows
- **Production Integration**: Ready for deployment in real manufacturing environments

### 💼 Business Value

This system addresses critical challenges in traditional manufacturing:

### 💼 Business Value

This system addresses critical challenges in traditional manufacturing:

| **Problem** | **Solution** |
|-------------|--------------|
| ❌ Manual paper-based workflows | ✅ Fully digital order processing with real-time tracking |
| ❌ Limited production visibility | ✅ Live dashboards with 5-10 second auto-refresh |
| ❌ Data silos between workstations | ✅ Integrated microservices with RESTful APIs |
| ❌ Inventory discrepancies | ✅ Automated stock updates with transaction audit trails |
| ❌ Scalability constraints | ✅ Independently scalable microservices architecture |
| ❌ Manual scheduling inefficiencies | ✅ SimAL scheduling engine integration with Gantt charts |

---

## 🏗️ System Architecture

### Microservices Design (6 Independent Services)

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
    ┌────┴────┬─────────┬──────────┬─────────┬──────────┐
    │         │         │          │         │          │
┌───▼───┐ ┌──▼──┐ ┌────▼────┐ ┌───▼───┐ ┌───▼────┐ ┌──▼──────┐
│ User  │ │Master│ │Inventory│ │ Order │ │ SimAL  │ │PostgreSQL│
│Service│ │ Data │ │ Service │ │Process│ │Integr. │ │  (Ready) │
│ :8012 │ │:8013 │ │  :8014  │ │ :8015 │ │ :8016  │ └─────────┘
└───────┘ └──────┘ └─────────┘ └───────┘ └────────┘
   │         │          │           │          │
   └─────────┴──────────┴───────────┴──────────┘
              H2 In-Memory Databases
          (Isolated per service - no shared DB)
```

### Key Architectural Principles

- ✅ **Service Isolation**: Each microservice has independent database (H2 in-memory for dev)
- ✅ **API-Driven Communication**: Services communicate exclusively via REST APIs (no direct DB access)
- ✅ **Stateless Authentication**: JWT tokens with BCrypt-encrypted passwords
- ✅ **Single Entry Point**: All external traffic flows through nginx → API Gateway
- ✅ **Health Monitoring**: Spring Boot Actuator endpoints for each service
- ✅ **Container Orchestration**: Docker Compose for seamless multi-service deployment

**Request Flow Example:**
```
User Login → nginx:80 → api-gateway:8011 → user-service:8012 → JWT Token
Order Creation → API Gateway → order-processing-service:8015 → inventory-service:8014
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
| **Database (Dev)** | H2 In-Memory | Per-service isolation |
| **Database (Prod)** | PostgreSQL-ready | Scalable persistence |
| **ORM** | Spring Data JPA | Database abstraction |
| **HTTP Client** | RestTemplate | Inter-service communication |
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

1. **Scenario 1: Sunny Day** - Direct fulfillment from warehouse stock
2. **Scenario 2: Warehouse Order** - Missing products trigger module assembly
3. **Scenario 3: Full Production** - Missing modules trigger manufacturing chain
4. **Scenario 4: High Volume** - Large orders bypass warehouse, go direct to production

**Order State Machines:**
```
CustomerOrder:  PENDING → CONFIRMED → PROCESSING → COMPLETED → DELIVERED
WarehouseOrder: PENDING → PROCESSING → AWAITING_PRODUCTION → FULFILLED
ProductionOrder: PENDING → PLANNED → IN_PRODUCTION → COMPLETED
```

### 📊 Real-Time Inventory Management

- **Multi-Location Tracking**: Independent stock per workstation
- **Transaction Audit Trail**: Immutable StockLedgerEntry for every movement (CREDIT/DEBIT/TRANSFER)
- **Low Stock Alerts**: Configurable thresholds with automatic notifications
- **Atomic Updates**: Database transactions ensure inventory consistency
- **Live Dashboard**: Auto-refresh every 5-10 seconds

### 🗓️ Production Planning & Scheduling

- **SimAL Integration**: External scheduling engine for production optimization
- **Gantt Chart Visualization**: Interactive timeline with task dependencies
- **Manual Scheduling**: Drag-and-drop interface for production planners
- **Control Order Generation**: Auto-create ProductionControlOrder and AssemblyControlOrder from schedules
- **Real-Time Updates**: Actual vs. estimated time tracking

### 📱 Role-Specific Dashboards

**9 Customized Interfaces** using standardized DashboardLayout component:
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

### One-Command Deployment

```bash
# Clone repository
git clone <repository-url>
cd lego-sample-factory

# Start all services (Linux/Mac)
docker-compose up -d

# Start all services (Windows PowerShell - includes health checks)
.\start-factory.ps1
```

**Access Application:**
- Frontend: `http://localhost:1011` (or `:80` if `NGINX_ROOT_PROXY_EXTERNAL_PORT=80`)
- API Gateway: `http://localhost:8011`

### Test Accounts

| Username | Password | Role | Workstation | Use Case |
|----------|----------|------|-------------|----------|
| `lego_admin` | `password` | ADMIN | - | System administration |
| `warehouse_operator` | `password` | PLANT_WAREHOUSE | Plant Warehouse (WS-7) | Customer order fulfillment |
| `modules_supermarket` | `password` | MODULES_SUPERMARKET | Modules Supermarket (WS-8) | Module warehouse operations |
| `production_planning` | `password` | PRODUCTION_PLANNING | - | Factory-wide scheduling |
| `production_control` | `password` | PRODUCTION_CONTROL | Injection Molding (WS-1) | Manufacturing oversight |
| `assembly_control` | `password` | ASSEMBLY_CONTROL | Gear Assembly (WS-4) | Assembly coordination |

---

## 📚 Comprehensive Documentation

| Document | Purpose |
|----------|---------|
| [PROJECT_TECHNICAL_OVERVIEW.md](PROJECT_TECHNICAL_OVERVIEW.md) | Academic research context, thesis background, PhD proposal material |
| [README.architecture.md](README.architecture.md) | System architecture diagrams, data models, API specifications |
| [README.devops.md](README.devops.md) | Deployment, configuration, troubleshooting, operations guide |
| [BusinessScenarios.md](BusinessScenarios.md) | 4 order fulfillment scenarios with step-by-step workflows |
| [TESTING_GUIDE.md](TESTING_GUIDE.md) | End-to-end testing procedures with test accounts |
| [IMPLEMENTATION_ROADMAP.md](_dev-docs/ai-helper-documents/IMPLEMENTATION_ROADMAP.md) | Feature development plan (3 stages) |
| [.github/copilot-instructions.md](.github/copilot-instructions.md) | Comprehensive AI agent onboarding (550+ lines) |

---

## 🔧 Development Workflow

### Local Backend Development

```bash
cd lego-factory-backend/<service>
./mvnw spring-boot:run
```

### Local Frontend Development (with Hot Reload)

```bash
# Start backend services only
docker-compose up -d api-gateway user-service masterdata-service inventory-service order-processing-service simal-integration-service

# Run frontend in dev mode
cd lego-factory-frontend
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

**368 CSS Design Tokens** in centralized [variables.css](lego-factory-frontend/src/styles/variables.css):
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
- `DashboardLayout` (standardized 9-page pattern)

---

## 📊 Key Technical Achievements

### 1. **Microservice Isolation**
- Each service has independent H2 database (no shared schema)
- Services communicate exclusively via REST APIs
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

### 4. **Frontend Architecture**
- Context API for global state (auth)
- Axios interceptors for automatic JWT injection
- CSS Modules for scoped styling
- DashboardLayout pattern for consistency

### 5. **Operational Readiness**
- Health check endpoints (`/actuator/health`)
- Structured logging (SLF4J)
- Docker Compose orchestration
- Multi-stage Dockerfile builds

---

## 🛣️ Future Enhancements

- [ ] **Kubernetes Deployment**: Helm charts for cloud-native scaling
- [ ] **PostgreSQL Migration**: Production-grade persistence layer
- [ ] **Redis Caching**: Session store & API response caching
- [ ] **Kafka Event Streaming**: Real-time event-driven architecture
- [ ] **Prometheus + Grafana**: Advanced monitoring & alerting
- [ ] **OAuth2/OIDC**: Enterprise SSO integration
- [ ] **Mobile App**: React Native companion app for shop floor
- [ ] **AI-Powered Scheduling**: ML optimization for production planning
- [ ] **IoT Integration**: Sensor data ingestion from factory equipment
- [ ] **Advanced Analytics**: PowerBI/Tableau dashboards

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- **Master's Thesis Supervisors**: For guidance on manufacturing system design
- **Spring Boot Team**: Excellent microservices framework
- **React Community**: Vibrant ecosystem and tooling
- **LEGO Sample Factory**: Inspiration and use case validation
- **Open Source Community**: Libraries and tools that made this possible

---

## 📧 Contact

**Author**: Nji S. Chifen
**LinkedIn**: www.linkedin.com/in/njisama
**Email**: mail@nji.io 
**Portfolio**: https://nji.io

**For Academic/Research Inquiries**: See [PROJECT_TECHNICAL_OVERVIEW.md](PROJECT_TECHNICAL_OVERVIEW.md) for detailed research contributions and PhD proposal materials.

---

<div align="center">

**⭐ If you find this project impressive, please star it! ⭐**

Built with passion for manufacturing innovation and software engineering excellence.

[⬆ Back to Top](#-life---lego-integrated-factory-execution-system)

</div>
