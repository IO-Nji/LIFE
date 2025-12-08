# Microservices Details

## Service Catalog

### User Service
**Purpose**: Authentication, authorization, user management
**Database**: PostgreSQL
**Key Endpoints**:
- `POST /api/users/login` - User authentication
- `POST /api/users/register` - User registration
- `GET /api/users/profile` - User profile retrieval

**Configuration**:
```properties
spring.datasource.url=jdbc:postgresql://postgres-db:5432/user_db
spring.jpa.hibernate.ddl-auto=update
```

### Masterdata Service
**Purpose**: Reference data management (workstations, item definitions)
**Database**: H2 (file-based)
**Key Endpoints**:
- `GET /api/masterdata/workstations` - List all workstations
- `GET /api/masterdata/items` - List item definitions

**Configuration**:
```properties
spring.datasource.url=jdbc:h2:file:/app/data/masterdata_db;DB_CLOSE_ON_EXIT=FALSE
```

### Inventory Service
**Purpose**: Stock level management, inventory transactions
**Database**: H2 (file-based)
**Key Endpoints**:
- `GET /api/inventory/stock` - Current stock levels
- `POST /api/inventory/transactions` - Record inventory movement

### Order Processing Service
**Purpose**: Order lifecycle management
**Database**: H2 (file-based)
**Key Endpoints**:
- `POST /api/orders` - Create new order
- `GET /api/orders/{id}` - Retrieve order details
- `PUT /api/orders/{id}/status` - Update order status

### SIMAL Integration Service
**Purpose**: Production scheduling system mock
**Database**: H2 (file-based)
**Key Endpoints**:
- `GET /api/simal/schedules` - Production schedules
- `POST /api/simal/production-orders` - Submit production orders

### API Gateway
**Purpose**: Request routing, load balancing, cross-cutting concerns
**Database**: None (stateless)

**Route Configuration**:
```properties
spring.cloud.gateway.routes[0].id=users_route
spring.cloud.gateway.routes[0].uri=lb://user-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/users/**

spring.cloud.gateway.routes[1].id=inventory_route
spring.cloud.gateway.routes[1].uri=lb://inventory-service
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/inventory/**
```

## Inter-Service Communication

### Communication Patterns
1. **Synchronous HTTP**: Frontend → API Gateway → Services
2. **Service Discovery**: Docker Compose service names as hostnames
3. **Load Balancing**: Spring Cloud Gateway round-robin

### Security Model
- **JWT Authentication**: Tokens issued by user-service
- **API Gateway Validation**: Central token validation point
- **Service Authorization**: Services trust validated requests from gateway

## Deployment Configuration

Each service follows the same Docker pattern:
```dockerfile
FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```