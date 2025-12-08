# LEGO Factory Management System

A microservices-based application for managing LEGO factory operations including inventory, orders, and production scheduling.

## Quick Start

1. **Prerequisites**: Docker, Docker Compose, Java 17+, Node.js 18+
2. **Clone repository**: `git clone <repository-url>`
3. **Environment setup**: Copy `.env.example` to `.env` and configure variables
4. **Start services**: `docker-compose up -d`
5. **Access application**: Navigate to `http://localhost` (or your server IP)

## Architecture Overview

- **Frontend**: React 18+ with Vite bundler
- **API Gateway**: Spring Cloud Gateway for request routing
- **6 Microservices**: Domain-driven Spring Boot services
- **Databases**: PostgreSQL (users) + H2 file-based (other services)
- **Deployment**: Docker Compose with GitLab CI/CD

## Documentation Structure

- [`architecture/`](architecture/) - System design and service boundaries
- [`development/`](development/) - Local development guides
- [`deployment/`](deployment/) - Deployment and DevOps setup
- [`api/`](api/) - API documentation and examples

## Key Services

| Service | Purpose | Database |
|---------|---------|----------|
| user-service | Authentication, JWT tokens | PostgreSQL |
| masterdata-service | Workstations, item definitions | H2 |
| inventory-service | Stock levels, transactions | H2 |
| order-processing-service | Order management | H2 |
| simal-integration-service | Production scheduling | H2 |
| api-gateway | Request routing | None |

## Technology Stack

- **Backend**: Spring Boot 3.x, Spring Cloud Gateway, Spring Security
- **Frontend**: React 18+, Vite, Axios, Context API
- **Databases**: PostgreSQL 15+, H2 Database
- **Infrastructure**: Docker, Docker Compose, GitLab CI/CD
- **Build Tools**: Maven, npm/yarn