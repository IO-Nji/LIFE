# Data Flow and Integration Patterns

## Request Flow Architecture

```
Client Request → Nginx → API Gateway → Microservice → Database
     ↑                                        ↓
     └─────────── Response ←──────────────────┘
```

## Frontend-Backend Communication

### Authentication Flow
1. **Login Request**: Frontend → `/api/users/login` → User Service
2. **JWT Token**: User Service generates and returns JWT
3. **Token Storage**: Frontend stores JWT in memory/localStorage
4. **Authenticated Requests**: Frontend includes `Authorization: Bearer <token>` header

### API Request Pattern
```javascript
// Frontend API calls use relative paths
const response = await axios.get('/api/inventory/stock', {
  headers: { Authorization: `Bearer ${token}` }
});
```

### API Gateway Routing
```
/api/users/**     → user-service:8080
/api/inventory/** → inventory-service:8080
/api/orders/**    → order-processing-service:8080
/api/masterdata/** → masterdata-service:8080
/api/simal/**     → simal-integration-service:8080
```

## Inter-Service Communication

### Service Discovery
Services communicate using Docker Compose service names:
```java
// Example: Order service calling inventory service
@Value("${inventory.service.url:http://inventory-service:8080}")
private String inventoryServiceUrl;

// RestTemplate call
ResponseEntity<StockLevel> response = restTemplate.getForEntity(
    inventoryServiceUrl + "/api/inventory/stock/{itemId}", 
    StockLevel.class, 
    itemId
);
```

### Database Access Patterns

#### PostgreSQL (User Service)
- **Connection Pool**: HikariCP with connection pooling
- **Transactions**: Spring @Transactional for ACID compliance
- **Schema Management**: Flyway/Liquibase for migrations

#### H2 Databases (Other Services)
- **File-based Storage**: `/app/data/{service}_db` persistent storage
- **Auto-reconnect**: `AUTO_SERVER=TRUE` for concurrent access
- **Embedded Mode**: No network overhead, direct file access

## Data Consistency Strategies

### Within Service Boundaries
- **ACID Transactions**: Database-level consistency
- **Optimistic Locking**: Version fields for concurrent updates
- **Validation**: Bean Validation annotations

### Cross-Service Operations
- **Choreography Pattern**: Services publish events, others react
- **Saga Pattern**: Distributed transaction management (future)
- **Eventual Consistency**: Accept temporary inconsistency for availability

## Security Data Flow

### JWT Token Validation
```
1. Frontend → API Gateway (with JWT)
2. API Gateway validates JWT signature
3. API Gateway extracts user context
4. API Gateway forwards request with user context
5. Service trusts validated request
```

### Service-to-Service Security
- **Internal Network**: Docker network isolation
- **No External Access**: Services only accessible via API Gateway
- **Trusted Context**: Services trust requests from API Gateway

## Error Handling Flow

### Standard Error Response
```json
{
  "timestamp": "2024-12-08T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid inventory item ID",
  "path": "/api/inventory/stock/invalid-id"
}
```

### Error Propagation
1. **Service Error**: Microservice throws business exception
2. **Error Handler**: `@ControllerAdvice` catches and formats error
3. **HTTP Response**: Appropriate status code and error message
4. **API Gateway**: Forwards error response to frontend
5. **Frontend Handling**: Display user-friendly error message

## Performance Considerations

### Caching Strategy
- **Database Level**: Query result caching
- **Application Level**: Spring Cache annotations
- **HTTP Level**: Browser caching headers

### Connection Management
- **Connection Pooling**: Configured per service
- **Timeout Configuration**: Read/write timeouts for external calls
- **Circuit Breaker**: Resilience4j for service failures (future)