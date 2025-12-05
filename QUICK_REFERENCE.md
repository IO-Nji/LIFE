# Quick Reference - SimAL Control Order Integration

## TL;DR

SimAL Integration Service now automatically creates production and assembly control orders when schedules are generated.

## Key Endpoints

### Create Control Orders from Schedule
```bash
POST /api/simal/scheduled-orders/{scheduleId}/create-control-orders?productionOrderId={id}
```

### Batch Create Control Orders
```bash
POST /api/simal/create-control-orders/batch
Body: {
  "scheduleIds": ["SCHED-1", "SCHED-2"],
  "productionOrderIds": [1, 2]
}
```

## Workstation Assignments (Automatic)

| Workstation | Type | Order Type | Status After Creation |
|---|---|---|---|
| WS-1 | Manufacturing | ProductionControlOrder | ASSIGNED |
| WS-2 | Manufacturing | ProductionControlOrder | ASSIGNED |
| WS-3 | Assembly | AssemblyControlOrder | ASSIGNED |
| WS-4 | Assembly | AssemblyControlOrder | ASSIGNED |

## Basic Workflow

```
1. Submit Order → /api/simal/production-order → Get scheduleId
2. Create Control Orders → /api/simal/scheduled-orders/{scheduleId}/create-control-orders
3. Workstations fetch orders → /api/{production|assembly}-control-orders/workstation/{id}
4. Operators manage work → /start, /notes, /complete
```

## What Gets Generated

### ProductionControlOrder
- **Number**: PCO-2024-XXXXX
- **Status**: ASSIGNED
- **For**: Manufacturing workstations (WS-1, WS-2)
- **Contains**: Production instructions, quality checkpoints, timeline

### AssemblyControlOrder
- **Number**: ACO-2024-XXXXX
- **Status**: ASSIGNED
- **For**: Assembly workstations (WS-3, WS-4)
- **Contains**: Assembly instructions, quality standards, timeline

## Configuration Files

### SimAL Service (Port 8016)
```properties
order-processing.api.base-url=http://localhost:8015/api
```

### Services Required Running
- Order Processing Service (8015)
- SimAL Integration Service (8016)
- API Gateway (8011)

## Example: Full Workflow

```bash
# 1. Submit order to SimAL
curl -X POST http://localhost:8011/api/simal/production-order \
  -H "Content-Type: application/json" \
  -d '{
    "orderNumber": "CO-001",
    "customerName": "ACME",
    "priority": "HIGH",
    "lineItems": [{
      "itemId": "I1",
      "itemName": "Part A",
      "quantity": 100,
      "workstationType": "MANUFACTURING",
      "estimatedDuration": 120
    }, {
      "itemId": "I2",
      "itemName": "Part B",
      "quantity": 100,
      "workstationType": "ASSEMBLY",
      "estimatedDuration": 90
    }]
  }'

# 2. Create control orders (use scheduleId from response)
curl -X POST "http://localhost:8011/api/simal/scheduled-orders/SCHED-1704960000000/create-control-orders?productionOrderId=1"

# 3. Production workstation sees their orders
curl http://localhost:8011/api/production-control-orders/workstation/1

# 4. Start production
curl -X POST http://localhost:8011/api/production-control-orders/1/start \
  -H "Content-Type: application/json" \
  -d '{"operatorId": "OP-001"}'

# 5. Complete production
curl -X POST http://localhost:8011/api/production-control-orders/1/complete

# 6. Assembly workstation sees their orders
curl http://localhost:8011/api/assembly-control-orders/workstation/2

# 7. Start assembly
curl -X PUT http://localhost:8011/api/assembly-control-orders/1/start \
  -H "Content-Type: application/json" \
  -d '{"operatorId": "OP-002"}'

# 8. Complete assembly
curl -X PUT http://localhost:8011/api/assembly-control-orders/1/complete
```

## Status Progression

### ProductionControlOrder
```
ASSIGNED → IN_PROGRESS → COMPLETED
```

### AssemblyControlOrder
```
ASSIGNED → IN_PROGRESS → COMPLETED
(or REWORK if defects found)
```

## New Code Locations

| Component | File | Lines | Purpose |
|---|---|---|---|
| ControlOrderIntegrationService | service/ControlOrderIntegrationService.java | 256 | Orchestrates schedule → control orders |
| SimalController | controller/SimalController.java | +130 | New endpoints for control order creation |
| ProductionControlOrderController | controller/ProductionControlOrderController.java | +80 | Receive control orders from SimAL |
| AssemblyControlOrderController | controller/AssemblyControlOrderController.java | +85 | Receive control orders from SimAL |

## Error Scenarios

| Error | Cause | Solution |
|---|---|---|
| 404 Schedule Not Found | Invalid scheduleId | Check scheduleId from submission response |
| 400 Bad Request | Invalid timestamp format | Use ISO 8601: YYYY-MM-DDTHH:mm:ss |
| 500 Server Error | Service unavailable | Ensure order-processing-service is running on 8015 |
| Control orders not appearing | Workstation not assigned correctly | Check workstationType in production order (MANUFACTURING vs ASSEMBLY) |

## Debug Tips

1. **Check logs**: `logging.level.io.life=DEBUG`
2. **Verify services**: All 6 services running on ports 8011-8016
3. **Test connectivity**: `curl http://localhost:8015/api/production-control-orders`
4. **Verify schedule**: `curl http://localhost:8016/api/simal/scheduled-orders`

## Next Phase

Production and Assembly Control dashboards (React frontend)

---

For complete details, see `SIMAL_INTEGRATION_GUIDE.md`
