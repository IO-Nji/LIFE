# Accessing the Modules Supermarket Dashboard

## Test Account Credentials

A test account has been automatically created for the Modules Supermarket operator:

- **Username**: `modulesSupermarketOp`
- **Password**: `modulesPass`
- **Role**: MODULES_SUPERMARKET
- **Assigned Workstation**: Workstation 8 (Modules Supermarket)

## Steps to Access the Dashboard

### 1. Start All Services

Ensure all microservices are running:
- User Service (port 8012)
- Masterdata Service (port 8013)
- Inventory Service (port 8014)
- Order Processing Service (port 8015)
- API Gateway (port 8011)
- Frontend (Vite dev server, port 5173 or 5174)

### 2. Navigate to Frontend

Open your browser and go to:
```
http://localhost:5173
```
(or the appropriate Vite dev server port)

### 3. Login with Modules Supermarket Credentials

Click **Login** and enter:
- **Username**: `modulesSupermarketOp`
- **Password**: `modulesPass`

### 4. Access Modules Supermarket Dashboard

After login, you should see a navigation menu with:
- Home
- Dashboard
- Products
- **Modules Supermarket** ← Click here
- Log out

Click on **"Modules Supermarket"** to access the dashboard.

## Dashboard Features

### Incoming Warehouse Orders
The dashboard displays warehouse orders that the Modules Supermarket needs to fulfill:

- **Order Number**: Unique identifier (e.g., WO-A1B2C3D4)
- **Source Order ID**: The customer order that triggered this warehouse order
- **Status**: Current status (PENDING, PROCESSING, FULFILLED, REJECTED, CANCELLED)
- **Scenario**: How the order was created (SCENARIO_2, SCENARIO_3)
- **Items**: Products/modules needed with requested and fulfilled quantities
- **Created Date**: When the order was created

### Filter by Status
Use the dropdown to filter orders:
- All Orders
- Pending
- Processing
- Fulfilled
- Rejected
- Cancelled

### Actions

**Fulfill Order**: 
- Click the green "Fulfill" button for PENDING or PROCESSING orders
- This deducts items from the Modules Supermarket inventory
- Updates the order status to FULFILLED
- Requires MODULES_SUPERMARKET role

**Cancel Order**:
- Click the red "Cancel" button to cancel an incomplete order
- Updates the order status to CANCELLED

### Statistics
At the bottom, you'll see a summary:
- Total Orders
- Pending Orders
- Processing Orders
- Fulfilled Orders

### Auto-Refresh
The dashboard automatically fetches new orders every 30 seconds. You can also click the **"Refresh"** button to manually update.

## How to Create Warehouse Orders (Testing)

To see warehouse orders appear in the Modules Supermarket dashboard, you need to:

1. **Login as Plant Warehouse Operator**
   - Username: `warehouseOperator`
   - Password: `warehousePass`

2. **Navigate to Warehouse Dashboard**
   - Click on "Warehouse" in the navigation menu

3. **Create a Customer Order**
   - Select products and quantities
   - Click "Create Order"

4. **Fulfill the Order**
   - Click the "Fulfill" button on the order
   - The system will:
     - Check inventory at Plant Warehouse (workstation 7)
     - If items are not available (Scenario 2 or 3), a WarehouseOrder is created
     - The order will appear in Modules Supermarket dashboard

5. **Switch back to Modules Supermarket**
   - Logout and login as `modulesSupermarketOp`
   - Navigate to Modules Supermarket dashboard
   - You'll see the warehouse order waiting to be fulfilled

## Fulfillment Workflow

```
Plant Warehouse Operator              Modules Supermarket Operator
         |                                        |
         | Creates Customer Order                 |
         | POST /api/customer-orders              |
         |---->                                   |
         |                                        |
         | Fulfills Order                         |
         | PUT /customer-orders/{id}/fulfill      |
         |---->                                   |
         |                       Check Stock      |
         |                       If unavailable:  |
         |                       Create WarehouseOrder
         |                                        |
         |                             Dashboard shows
         |                             incoming warehouse order
         |                                        |
         |                                | Clicks Fulfill
         |                  PUT /warehouse-orders/{id}/fulfill-modules
         |                                        |
         |                                | Stock deducted from
         |                                | Modules Supermarket
         |                                | WarehouseOrder marked FULFILLED
         |                                        |
         | <-----                                 |
         | Stock available to complete
         | original customer order
```

## Troubleshooting

### Modules Supermarket Navigation Link Not Visible

If you don't see the "Modules Supermarket" link after login:
1. Make sure you're logged in as `modulesSupermarketOp` (not another user)
2. Check browser console (F12) for errors
3. Verify the role is being returned correctly in the JWT token

### No Warehouse Orders Appearing

1. Create a customer order as Plant Warehouse operator first
2. Fulfill that order (this creates the WarehouseOrder)
3. Refresh the Modules Supermarket dashboard
4. Make sure workstations 7 and 8 exist in Masterdata Service

### Cannot Fulfill Orders

Check that:
1. Order status is PENDING or PROCESSING
2. You have the MODULES_SUPERMARKET role
3. Modules Supermarket inventory has stock of the requested items
4. No API errors in browser console

## Related Documentation

- See `DAY_11_WAREHOUSE_ORDERS.md` for technical implementation details
- See `DAY_10_FULFILLMENT.md` for the fulfillment service that creates warehouse orders
- Plant Warehouse page documentation in `PlantWarehousePage` component

---

**Last Updated**: December 5, 2025  
**Status**: Ready for Testing
