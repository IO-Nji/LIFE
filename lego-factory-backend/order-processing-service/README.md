# Order Processing Service

The Order Processing Service is a core microservice in the LIFE (Lightweight Integrated Factory Engine) system. It handles the management and processing of all order types within the manufacturing ecosystem.

## Features

### Order Types Managed
- **Manufacturing Orders**: Primary production orders for manufactured items
- **Assembly Orders**: Orders for component assembly at assembly workstations
- **Production Control Orders**: Quality control and inspection orders at production control stations
- **Supplier Orders**: External supplier orders for component procurement

### Core Capabilities
- RESTful APIs for order management and status tracking
- Comprehensive order lifecycle management (CREATED → IN_PROGRESS → COMPLETED/HALTED)
- State transition validation for safe order status changes
- Operator notes and defect tracking
- Quality assurance integration
- Async order processing with thread pooling
- Global exception handling with consistent error responses

## Architecture

### Directory Structure
```
order-processing-service/
├── src/
│   ├── main/
│   │   ├── java/io/life/order/
│   │   │   ├── controller/          # REST API endpoints
│   │   │   ├── service/             # Business logic
│   │   │   ├── repository/          # Data access layer
│   │   │   ├── entity/              # JPA entities
│   │   │   ├── dto/                 # Data transfer objects
│   │   │   ├── exception/           # Custom exceptions
│   │   │   ├── util/                # Utility classes
│   │   │   └── config/              # Spring configuration
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## REST API Endpoints

### Manufacturing Orders
- `GET /api/manufacturing-orders` - List all manufacturing orders
- `GET /api/manufacturing-orders/{id}` - Get order by ID
- `GET /api/manufacturing-orders/status/{status}` - Filter by status
- `POST /api/manufacturing-orders` - Create new order
- `PUT /api/manufacturing-orders/{id}` - Update order
- `POST /api/manufacturing-orders/{id}/start` - Start production
- `POST /api/manufacturing-orders/{id}/complete` - Complete order
- `POST /api/manufacturing-orders/{id}/halt` - Halt production
- `PATCH /api/manufacturing-orders/{id}/notes` - Update operator notes
- `PATCH /api/manufacturing-orders/{id}/materials` - Update material info

### Assembly Orders
- `GET /api/assembly-orders` - List all assembly orders
- `GET /api/assembly-orders/{id}` - Get order by ID
- `GET /api/assembly-orders/workstation/{workstationId}` - Orders for workstation
- `GET /api/assembly-orders/workstation/{workstationId}/active` - Active orders only
- `POST /api/assembly-orders/{id}/start` - Start assembly
- `POST /api/assembly-orders/{id}/complete` - Complete assembly
- `POST /api/assembly-orders/{id}/halt` - Halt assembly
- `PATCH /api/assembly-orders/{id}/notes` - Update operator notes
- `PATCH /api/assembly-orders/{id}/sequence` - Update assembly sequence
- `PATCH /api/assembly-orders/{id}/quality-checks` - Update quality metrics

### Production Control Orders
- `GET /api/production-control-orders` - List all control orders
- `GET /api/production-control-orders/{id}` - Get order by ID
- `GET /api/production-control-orders/workstation/{workstationId}` - Orders for station
- `GET /api/production-control-orders/workstation/{workstationId}/active` - Active only
- `POST /api/production-control-orders/{id}/start` - Start inspection
- `POST /api/production-control-orders/{id}/complete` - Complete inspection
- `POST /api/production-control-orders/{id}/halt` - Halt inspection
- `PATCH /api/production-control-orders/{id}/notes` - Update notes
- `PATCH /api/production-control-orders/{id}/defects` - Update defect info

### Supplier Orders
- `GET /api/supplier-orders` - List all supplier orders
- `GET /api/supplier-orders/{id}` - Get order by ID
- `GET /api/supplier-orders/status/{status}` - Filter by status
- `GET /api/supplier-orders/supplier/{supplierId}` - Orders from supplier
- `POST /api/supplier-orders` - Create new supplier order
- `PUT /api/supplier-orders/{id}` - Update order
- `POST /api/supplier-orders/{id}/send` - Mark as sent
- `POST /api/supplier-orders/{id}/receive-partial` - Record partial receipt
- `POST /api/supplier-orders/{id}/receive-complete` - Record complete receipt
- `POST /api/supplier-orders/{id}/cancel` - Cancel order
- `PATCH /api/supplier-orders/{id}/delivery-info` - Update delivery details

## Order Status Workflow

### Manufacturing/Assembly/Control Orders
```
CREATED → ASSIGNED → IN_PROGRESS → COMPLETED
                  ↓
                CANCELLED

IN_PROGRESS ↔ HALTED → IN_PROGRESS or CANCELLED
```

### Supplier Orders
```
CREATED → SENT → PARTIALLY_RECEIVED → RECEIVED
           ↓
        CANCELLED (from any state)

PARTIALLY_RECEIVED ↔ PARTIALLY_RECEIVED → RECEIVED
```

## Data Models

### Manufacturing Order Entity
- `id` - Unique identifier
- `manufacturingOrderNumber` - Unique order number
- `productId` - Associated product
- `quantity` - Items to manufacture
- `status` - Current order status
- `assignedWorkstationId` - Workstation assignment
- `startDate` - Production start date
- `expectedCompletionDate` - Target completion
- `actualCompletionDate` - Actual completion
- `materialAllocated` - Material quantity allocated
- `defectsFound` - Number of defects identified
- `operatorNotes` - Operator comments
- `createdAt` - Creation timestamp
- `updatedAt` - Last update timestamp

### Assembly Order Entity
- `id` - Unique identifier
- `assemblyOrderNumber` - Unique order number
- `productId` - Product being assembled
- `quantity` - Items to assemble
- `status` - Current order status
- `assignedWorkstationId` - Workstation assignment
- `startDate` - Assembly start
- `expectedCompletionDate` - Target completion
- `actualCompletionDate` - Actual completion
- `currentAssemblyStep` - Current step in sequence
- `totalAssemblySteps` - Total steps required
- `qualityChecksPassed` - QC checks passed
- `qualityChecksFailed` - QC checks failed
- `operatorNotes` - Operator comments

### Production Control Order Entity
- `id` - Unique identifier
- `controlOrderNumber` - Unique order number
- `productId` - Product being controlled
- `quantity` - Items to inspect
- `status` - Current order status
- `assignedWorkstationId` - Control station
- `startDate` - Inspection start
- `expectedCompletionDate` - Target completion
- `actualCompletionDate` - Actual completion
- `defectsFound` - Defects identified
- `defectsReworked` - Defects corrected
- `reworkRequired` - Rework needed flag
- `operatorNotes` - Inspector comments

### Supplier Order Entity
- `id` - Unique identifier
- `supplierOrderNumber` - Unique order number
- `supplierId` - Supplier identifier
- `manufacturingOrderId` - Parent manufacturing order
- `itemId` - Item being ordered
- `quantity` - Quantity ordered
- `status` - Current order status
- `orderDate` - Order placement date
- `expectedDeliveryDate` - Expected delivery
- `actualDeliveryDate` - Actual delivery
- `quantityReceived` - Quantity received
- `supplierNotes` - Supplier comments
- `deliveryAddress` - Delivery location

## Configuration

### Application Properties
```properties
# Server
spring.application.name=order-processing-service
server.port=8082

# Database (H2 for development)
spring.datasource.url=jdbc:h2:file:./data/life_orders
spring.datasource.driverClassName=org.h2.Driver

# Order Number Prefixes
app.order.manufacturing.prefix=MFG
app.order.assembly.prefix=ASM
app.order.control.prefix=CTL
app.order.supplier.prefix=SUP

# Async Processing
spring.task.execution.pool.core-size=10
spring.task.execution.pool.max-size=20
```

## Running the Service

### Prerequisites
- Java 11+
- Maven 3.6+
- MySQL 8.0+ (for production) or H2 (for development)

### Building
```bash
mvn clean package
```

### Running
```bash
# Development (from root project)
mvn spring-boot:run -pl order-processing-service

# Or run JAR directly
java -jar order-processing-service/target/order-processing-service-1.0.0.jar
```

## Testing

```bash
mvn test -pl order-processing-service
```

## Exception Handling

The service provides consistent error responses with the following custom exceptions:

- `EntityNotFoundException` - When requested entity is not found
- `InvalidOrderStateException` - When state transition is invalid
- `InvalidOperationException` - When operation is not allowed
- `InsufficientQuantityException` - When quantity check fails
- `OrderProcessingException` - General order processing errors

All exceptions are handled by `GlobalExceptionHandler` which returns standardized error responses.

## Integration Points

The service integrates with:
- **Workstation Service** - For workstation and operator information
- **Inventory Service** - For material allocation and tracking
- **Quality Service** - For quality check integration
- **Notification Service** - For order status updates

## Performance Considerations

- Async processing with configurable thread pool
- Connection pooling for database access
- Query optimization with proper indexing
- Scheduled cleanup of old order records
- Pagination support for list endpoints (default: 20 items/page)

## Security

- CORS enabled for cross-origin requests
- Input validation on all endpoints
- SQL injection prevention through parameterized queries
- Rate limiting (recommended at API Gateway level)

## Future Enhancements

- [ ] Batch order creation
- [ ] Order templates and workflows
- [ ] Advanced filtering and search
- [ ] Order analytics and reporting
- [ ] Webhook notifications
- [ ] Order auto-scheduling
- [ ] Supply chain integration
- [ ] Cost tracking and billing

## Support

For issues or questions, please contact the development team or create an issue in the project repository.
