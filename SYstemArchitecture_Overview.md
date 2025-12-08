# System Architecture Overview

## High-Level Architecture

The LEGO Factory system follows a microservices architecture with clear service boundaries and responsibilities.

```
┌─────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend  │───▶│   API Gateway   │───▶│  Microservices  │
│   (React)   │    │ (Spring Cloud)  │    │  (Spring Boot)  │
└─────────────┘    └─────────────────┘    └─────────────────┘
                            │                       │
                            ▼                       ▼
                   ┌─────────────────┐    ┌─────────────────┐
                   │   Load Balancer │    │   Databases     │
                   │  (Round Robin)  │    │ (PostgreSQL/H2) │
                   └─────────────────┘    └─────────────────┘
```

## Design Principles

1. **Domain-Driven Design**: Each service owns a specific business domain
2. **Database per Service**: No shared databases between microservices
3. **API Gateway Pattern**: Single entry point for frontend requests
4. **Stateless Services**: All services are stateless for horizontal scaling
5. **Event-Driven Communication**: Future consideration for async messaging

## Service Boundaries

### Core Business Services
- **User Service**: Identity and access management
- **Masterdata Service**: Reference data (workstations, items)
- **Inventory Service**: Stock management and tracking
- **Order Processing**: Order lifecycle management

### Integration Services
- **SIMAL Integration**: External production system mock
- **API Gateway**: Cross-cutting concerns (routing, auth, logging)

## Data Architecture

### Database Strategy
- **PostgreSQL**: User service (relational, ACID compliance needed)
- **H2 File-based**: Other services (lightweight, embedded, persistent)

### Data Consistency
- **Strong Consistency**: Within service boundaries
- **Eventual Consistency**: Cross-service operations (future implementation)