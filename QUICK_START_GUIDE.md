# Order Processing Service - Quick Start Guide

## 🚀 5-Minute Setup

### Step 1: Prerequisites
- Java 11+ installed
- Maven 3.6+ installed
- Git installed
- Terminal/Command Prompt access

### Step 2: Navigate to Service Directory
```bash
cd "E:\My Documents\DEV\Java\Project\LIFE\order-processing-service"
```

### Step 3: Build the Service
```bash
mvn clean package
```

### Step 4: Run the Service
```bash
mvn spring-boot:run
```

### Step 5: Verify It's Running
```
Expected output:
...
Tomcat started on port(s): 8082 (http)
Started OrderProcessingServiceApplication in 5.123 seconds
```

## ✅ Service is Ready!

Access the service at: `http://localhost:8082`

---

## 📡 Quick API Tests

### Test 1: Create a Manufacturing Order
```bash
curl -X POST http://localhost:8082/api/manufacturing-orders \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 100,
    "expectedCompletionDate": "2024-01-20",
    "assignedWorkstationId": 5
  }'
```

**Expected Response** (Status 201):
```json
{
  "id": 1,
  "manufacturingOrderNumber": "MFG-20240115-1001",
  "productId": 1,
  "quantity": 100,
  "status": "CREATED",
  "assignedWorkstationId": 5,
  "createdAt": "2024-01-15T10:30:45.123456",
  "updatedAt": "2024-01-15T10:30:45.123456"
}
```

### Test 2: Get All Manufacturing Orders
```bash
curl http://localhost:8082/api/manufacturing-orders
```

**Expected Response** (Status 200):
```json
[
  {
    "id": 1,
    "manufacturingOrderNumber": "MFG-20240115-1001",
    "productId": 1,
    "quantity": 100,
    "status": "CREATED",
    ...
  }
]
```

### Test 3: Start Production
```bash
curl -X POST http://localhost:8082/api/manufacturing-orders/1/start
```

**Expected Response** (Status 200):
```json
{
  "id": 1,
  "manufacturingOrderNumber": "MFG-20240115-1001",
  "status": "IN_PROGRESS",
  "startDate": "2024-01-15T10:31:00.000000",
  ...
}
```

### Test 4: Update Operator Notes
```bash
curl -X PATCH http://localhost:8082/api/manufacturing-orders/1/notes \
  -H "Content-Type: application/json" \
  -d '{"notes": "Production running smoothly, no issues"}'
```

**Expected Response** (Status 200):
```json
{
  "id": 1,
  "operatorNotes": "Production running smoothly, no issues",
  ...
}
```

### Test 5: Complete Production
```bash
curl -X POST http://localhost:8082/api/manufacturing-orders/1/complete
```

**Expected Response** (Status 200):
```json
{
  "id": 1,
  "manufacturingOrderNumber": "MFG-20240115-1001",
  "status": "COMPLETED",
  "actualCompletionDate": "2024-01-15T10:35:00.000000",
  ...
}
```

---

## 🧪 Testing the Service

### Run All Unit Tests
```bash
mvn test
```

### Run With Coverage Report
```bash
mvn test jacoco:report
# View report at: target/site/jacoco/index.html
```

### Run Specific Test
```bash
mvn test -Dtest=ManufacturingOrderControllerTest
```

---

## 📊 Database Access (Development)

### H2 Console
When service is running, access: `http://localhost:8082/h2-console`

**Credentials:**
- JDBC URL: `jdbc:h2:file:./data/life_orders`
- User: `sa`
- Password: (leave blank)

### View Tables
```sql
SELECT * FROM manufacturing_orders;
SELECT * FROM assembly_orders;
SELECT * FROM production_control_orders;
SELECT * FROM supplier_orders;
```

---

## 🛠️ Common Commands

### Build Only (No Tests)
```bash
mvn clean package -DskipTests
```

### Run with Debug Logging
```bash
mvn spring-boot:run -Dlogging.level.io.life.order=DEBUG
```

### Clean Build Artifacts
```bash
mvn clean
```

### View Dependencies
```bash
mvn dependency:tree
```

---

## 📖 API Endpoints by Type

### Manufacturing Orders
```
GET    /api/manufacturing-orders              - List all
GET    /api/manufacturing-orders/{id}         - Get one
POST   /api/manufacturing-orders              - Create
POST   /api/manufacturing-orders/{id}/start   - Start
POST   /api/manufacturing-orders/{id}/complete - Complete
```

### Assembly Orders
```
GET    /api/assembly-orders                   - List all
GET    /api/assembly-orders/{id}              - Get one
POST   /api/assembly-orders                   - Create
POST   /api/assembly-orders/{id}/start        - Start
POST   /api/assembly-orders/{id}/complete     - Complete
```

### Production Control Orders
```
GET    /api/production-control-orders         - List all
GET    /api/production-control-orders/{id}    - Get one
POST   /api/production-control-orders         - Create
POST   /api/production-control-orders/{id}/start - Start
POST   /api/production-control-orders/{id}/complete - Complete
```

### Supplier Orders
```
GET    /api/supplier-orders                   - List all
GET    /api/supplier-orders/{id}              - Get one
POST   /api/supplier-orders                   - Create
POST   /api/supplier-orders/{id}/send         - Send
POST   /api/supplier-orders/{id}/receive-partial - Receive
```

---

## ⚠️ Troubleshooting

### Port Already in Use
```bash
# Find process using port 8082
netstat -ano | findstr :8082

# Kill process (Windows)
taskkill /PID <PID> /F

# Or use different port
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
```

### Build Fails
```bash
# Clean and retry
mvn clean package

# Check Java version
java -version
# Should show Java 11 or higher
```

### Tests Fail
```bash
# Run with verbose output
mvn test -X

# Check H2 database
# Clean data directory if needed
rmdir /s data
```

### Service Won't Start
```bash
# Check logs
# Look for error messages in console output

# Verify database file isn't locked
# Close H2 console if open

# Delete data directory and restart
rmdir /s data
mvn spring-boot:run
```

---

## 🎯 Common Workflows

### Complete Manufacturing Order Flow
```bash
# 1. Create order
curl -X POST http://localhost:8082/api/manufacturing-orders \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 100, "expectedCompletionDate": "2024-01-20", "assignedWorkstationId": 5}'

# 2. Start production (assume ID=1)
curl -X POST http://localhost:8082/api/manufacturing-orders/1/start

# 3. Update notes
curl -X PATCH http://localhost:8082/api/manufacturing-orders/1/notes \
  -H "Content-Type: application/json" \
  -d '{"notes": "Running well"}'

# 4. Record defects
curl -X PATCH http://localhost:8082/api/manufacturing-orders/1/defects \
  -H "Content-Type: application/json" \
  -d '{"defectsFound": 2, "reworkRequired": true}'

# 5. Complete order
curl -X POST http://localhost:8082/api/manufacturing-orders/1/complete
```

### Create Supplier Order Flow
```bash
# 1. Create supplier order
curl -X POST http://localhost:8082/api/supplier-orders \
  -H "Content-Type: application/json" \
  -d '{"supplierId": 1, "itemId": 5, "quantity": 500, "expectedDeliveryDate": "2024-01-20"}'

# 2. Send to supplier (assume ID=1)
curl -X POST http://localhost:8082/api/supplier-orders/1/send

# 3. Receive partial shipment
curl -X POST http://localhost:8082/api/supplier-orders/1/receive-partial \
  -H "Content-Type: application/json" \
  -d '{"quantityReceived": 250, "receivedDate": "2024-01-18"}'

# 4. Receive remaining shipment
curl -X POST http://localhost:8082/api/supplier-orders/1/receive-partial \
  -H "Content-Type: application/json" \
  -d '{"quantityReceived": 250, "receivedDate": "2024-01-19"}'

# 5. Mark as complete
curl -X POST http://localhost:8082/api/supplier-orders/1/receive-complete
```

---

## 📚 Documentation References

- **Full Documentation**: See `README.md`
- **API Reference**: See `ORDER_PROCESSING_API_REFERENCE.md`
- **Testing Guide**: See `ORDER_PROCESSING_TESTING_GUIDE.md`
- **Implementation Details**: See `DAY_14_COMPLETION_SUMMARY.md`
- **Service Overview**: See `SERVICE_INDEX.md`

---

## 🎓 Next Steps

1. **Understand the Architecture**
   - Read `README.md` for full details
   - Review source code structure

2. **Run the Tests**
   - Execute `mvn test` to verify everything works
   - Check test coverage with `mvn jacoco:report`

3. **Explore the APIs**
   - Use curl or Postman to test endpoints
   - Try the workflows above

4. **Integrate with Other Services**
   - Connect with Workstation Service
   - Connect with Inventory Service
   - Set up message bus communication

5. **Deploy to Production**
   - Build JAR file: `mvn clean package`
   - Deploy to application server
   - Configure MySQL database
   - Set up monitoring and logging

---

## 💡 Tips & Tricks

### Use Postman for Testing
1. Import the service URL: `http://localhost:8082`
2. Create a collection for each order type
3. Save common requests as templates
4. Use environment variables for IDs

### Enable Debug Mode
Add to `application.properties`:
```properties
logging.level.io.life.order=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Custom Port
Run with: `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"`

### Run in Background (PowerShell)
```powershell
Start-Process -NoNewWindow -FilePath "cmd" -ArgumentList "/c mvn spring-boot:run"
```

---

## ✨ Summary

You now have a fully functional Order Processing Service that:
- ✅ Manages 4 order types
- ✅ Provides REST APIs for all operations
- ✅ Validates state transitions
- ✅ Tracks operator notes and defects
- ✅ Handles errors gracefully
- ✅ Uses H2 database for development
- ✅ Ready for production deployment

**Start building!** 🚀
