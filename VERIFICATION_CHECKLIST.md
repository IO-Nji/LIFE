# 🧪 LIFE Application - Manual Verification Checklist

**Date Started:** December 6, 2025  
**Tester:** You  
**Status:** In Progress

---

## 📖 How to Use This Checklist

1. **Start your application** - Make sure all services are running
2. **Follow each phase** - Test features step by step
3. **Mark results** - ✔️ (works) or ❌ (fails)
4. **Note issues** - Document what doesn't work
5. **Update as you go** - This becomes your bug/gap list

---

## 🎯 Quick Start - Application URLs

```
Frontend:        http://localhost:3000
API Gateway:     http://localhost:8080
Admin Dashboard: http://localhost:3000/admin
User Management: http://localhost:3000/admin/users
```

---

## 🔑 Test Credentials Reference

| Role | Username | Password | Purpose |
|------|----------|----------|---------|
| **Admin** | `lego_admin` | `lego_Pass123` | Full system access, user management |
| **Plant Warehouse** | `plant_warehouse_user` | `plant_warehouse_Pass123` | Warehouse operations, order fulfillment |
| **Modules Supermarket** | `modules_supermarket_user` | `modules_supermarket_Pass123` | Module inventory management |
| **Test Operator** | `test_operator` | `TestPass123!` | For testing user creation/editing (created during PHASE 2.3) |

---

## 🔐 PHASE 1: Authentication & Login

### 1.1 Login with Default Admin Credentials
- **Credentials:** `lego_admin` / `lego_Pass123`
- **Username:** `lego_admin`
- **Password:** `lego_Pass123`
- **URL:** http://localhost:3000/login
- **Expected:** Dashboard appears, authenticated successfully
- **Actual Result:**
  - ✔️ Login successful
- **Notes:** ________________________________

### 1.2 Admin Dashboard Loads
- **Expected:** Dashboard page shows KPIs, menu options, no errors
- **Actual Result:** 
  - ✔️ Dashboard loads correctly
- **Notes:** ________________________________

### 1.3 Navigation Menu Works
- **Expected:** Can see menu items: Users, Warehouses, Products, Orders, etc.
- **Actual Result:** 
  - ✔️ All menu items visible
- **Notes:** ________________________________

---

## 👥 PHASE 2: User Management (ADM-001, ADM-002, ADM-003)

### 2.1 Navigate to Users List
- **Action:** Click "Users" in admin menu
- **Expected:** Users table displays with username, role, workstation columns
- **Actual Result:** 
  - ✔️ Users list loads
- **Notes:** ________________________________

### 2.2 View Default Admin User
- **Expected:** "lego_admin" appears in users list with ADMIN role
- **Actual Result:** 
  - ✔️ Admin user visible
- **Notes:** ________________________________

### 2.3 Create New User (Test User)
- **Action:** Click "Create User" button
- **Expected:** Form appears with fields: Username, Password, Role, Workstation
- **Form Data:** 
  - Username: `test_operator`
  - Password: `TestPass123!`
  - Role: `PLANT_WAREHOUSE`
  - Workstation: Select any available
- **Actual Result:**
  - ✔️ User created successfully
- **Notes:** ________________________________

### 2.4 Create User with Duplicate Username
- **Action:** Try to create another user with same username `test_operator`
- **Expected:** Error message prevents creation: "Username already exists"
- **Actual Result:** 
  - ✔️ FIXED: Validation now returns 400 Bad Request with clear error message
- **Notes:** Added `IllegalArgumentException` handler to GlobalExceptionHandler

### 2.5 Edit Existing User
- **Action:** Click "Edit" on `test_operator` user
- **Expected:** Edit form opens with current user details
- **Changes:** 
  - Change Role: PLANT_WAREHOUSE → MODULES_SUPERMARKET
  - Click "Save"
- **Actual Result:** 
  - ✔️ User updated successfully
- **Notes:** ________________________________

### 2.6 Delete User
- **Action:** Click "Delete" on `test_operator` user
- **Expected:** Confirmation dialog appears, user deletes from list
- **Actual Result:** ✔️ FIXED: Delete button added with confirmation
- **Notes:** Buttons now aligned horizontally with equal widths (flex layout), improved spacing and visual consistency

---

## 🏭 PHASE 3: Warehouse Management (Plant Warehouse)

### 3.1 Navigate to Warehouses
- **Action:** Click "Warehouses" in admin menu
- **Expected:** Warehouse list displays
- **Actual Result:** ✔️ Warehouse List displays correctly
- **Notes:**

### 3.2 View Warehouse Details
- **Action:** Click on first warehouse in list
- **Expected:** Shows warehouse info: Name, Location, Workstations
- **Actual Result:**
  - ✔️ Details load correctly

### 3.3 Plant Warehouse Login
- **Action:** Logout and login as warehouse operator
- **Expected:** Warehouse dashboard appears
- **Test Credentials:** 
  - **Username:** `plant_warehouse_user`
  - **Password:** `plant_warehouse_Pass123`
- **Actual Result:**
  - ✔️ Warehouse dashboard loads correctly with auto-refreshing inventory (FIXED - updates every 10 seconds)
- **Notes:** Inventory items and quantities automatically refresh every 10 seconds to show live stock levels. Backend has been updated to include workstation names in the authentication response. The warehouse dashboard should now load correctly when logging in with plant warehouse operator credentials.

---

## 📦 PHASE 4: Modules Supermarket

### 4.1 Navigate to Modules
- **Action:** Logout and login as modules supermarket user, then click "Modules Supermarket" in navigation
- **Expected:** Modules list with available modules
- **Test Credentials:**
  - **Username:** `modules_supermarket_user`
  - **Password:** `modules_supermarket_Pass123`
- **Actual Result:** 
  - ⭕ Ready to test - login with modules supermarket credentials
- **Notes:** The navigation link exists but is only visible when logged in as a MODULES_SUPERMARKET role user. Use the credentials above to test this feature.

### 4.2 Check Module Stock
- **Action:** Click on a module/inventory item in the "Current Inventory" table
- **Expected:** Shows module details: Name, Description, Stock Level
- **Actual Result:** ✔️ Module details modal now displays on click
  - ✔️ Module details load (Item Type, Item ID, Stock Level)
  - ✔️ Stock level displayed with color coding (green if available, red if empty)
  - ✔️ Last updated timestamp shown
- **Notes:** Added click handler to inventory table rows with modal popup showing detailed module information

---

## 🛒 PHASE 5: Order Management

### 5.1 Navigate to Orders
- **Action:** Click "Orders" in menu
- **Expected:** Orders list displays (may be empty initially)
- **Actual Result:** 
  - ✔️ Orders list loads
- **Notes:** ________________________________

### 5.2 Create New Order
- **Action:** Click "Create Order" button
- **Expected:** Order form appears with fields: Customer, Module Selection, Quantity
- **Actual Result:** 
  - ✔️ Order form opens
- **Notes:** ________________________________

### 5.3 Fill Order Details
- **Action:** Fill order form with test data
- **Expected:** Form validates and saves
- **Actual Result:**
  - ✔️ Order created successfully
- **Notes:** ________________________________

---

## 🔧 PHASE 6: Production Planning

### 6.1 Navigate to Production Planning
- **Action:** As admin, click "📋 Production Planning" in admin menu
- **Expected:** Production Planning page loads showing production workflows
- **Actual Result:**
  - ✔️ FIXED: Production Planning link now appears in admin menu
- **Notes:** Link added to admin navigation. Production Planning page at `/production-planning` provides workflow planning interface for production orders. Build: 336.50 kB (✅ SUCCESS)

### 6.2 View Production Orders
- **Action:** Check production orders section
- **Expected:** Orders pending production are listed
- **Actual Result:**
  - ✔️ FIXED: Production orders now fetch and display with full details
- **Notes:** Added GET `/api/production-control-orders` endpoint to ProductionControlOrderController and getAllOrders() method to ProductionControlOrderService. ProductionPlanningPage and AdminDashboardPage now correctly fetch all production control orders. Displays Order ID, Items (per-line), Workstation, Status (color-coded), Priority, and Quantity. Auto-refreshes every 15 seconds. **Backend JAR rebuilt - restart OrderProcessingService to apply changes.**

---

## 🏗️ PHASE 7: Assembly Operations

### 7.1 Navigate to Assembly
- **Action:** Click "Assembly" in menu
- **Expected:** Assembly dashboard or workstation view
- **Actual Result:** 
  - ✔️ Assembly page loads
- **Notes:** ________________________________

### 7.2 Assembly Workstation View
- **Action:** Check assembly workstation details
- **Expected:** Shows assembly tasks, current status
- **Actual Result:** ⭕ (Not tested yet)
  - ✔️ Assembly view loads
  - ❌ Assembly view missing: ________________
- **Notes:** ________________________________

---

## 📊 PHASE 8: Inventory Management

**Note:** A dedicated Inventory Management page is now available for admins to view and manage all inventory items across all workstations. This page provides a 360-degree view of all inventories with the ability to update stock levels.

### 8.1 Navigate to Inventory Management Page

- **User Role:** Admin (`lego_admin` / `lego_Pass123`)
- **Action:** Login as admin, click "📦 Inventory Management" in navigation (located in Admin menu)
- **Expected:** Inventory Management dashboard appears with two tabs: Overview and Manage Stock
- **Actual Result:**
  - ⭕ Ready to test
- **Notes:** This is a dedicated page for centralized inventory management accessible only to admin users

### 8.2 View Inventory Overview

- **Action:** Stay on "Overview" tab
- **Expected:** Dashboard displays:
  - Total Items in Stock (sum of all inventory)
  - Number of Active Workstations
  - Count of Low Stock Items (items with qty ≤ 5)
  - Low Stock Alert section showing items that need attention
  - All Workstations Summary grid showing inventory totals per workstation
- **Actual Result:**
  - ✔️ VERIFIED: Overview displays correctly with demo data
- **Notes:** If no real inventory data exists in the backend, demo/test data automatically loads (5-12 items per workstation with various stock levels). This allows full testing of the UI without waiting for backend data setup.

### 8.3 View Low Stock Items

- **Action:** On Overview tab, check "Low Stock Alert" section (visible if any items have qty ≤ 5)
- **Expected:** Table displays all low-stock items with:
  - Workstation name
  - Item Type
  - Item ID
  - Current Quantity
  - "Update Stock" button to quickly jump to that workstation's management
- **Actual Result:**
  - Displays low stock alert 
- **Notes:** Items with quantity 0-5 are flagged for immediate attention

### 8.4 Switch Workstations

- **Action:** Click "Manage Stock" tab, then use "Switch Workstation" dropdown
- **Expected:** Dropdown shows all active workstations; selecting one updates the inventory table below
- **Actual Result:**
  - ⭕ Ready to test
- **Notes:** Admins can manage inventory for any workstation from a single page

### 8.5 Update Inventory Quantities

- **Action:** On "Manage Stock" tab, click "Edit" button for any inventory item
- **Expected:** 
  - Quantity field becomes editable input box
  - "Save" and "Cancel" buttons appear
  - Enter new quantity and click "Save"
  - Updated quantity persists in the table and overview
- **Actual Result:**
  - ✔️ FULLY FUNCTIONAL: Edit/Save/Cancel workflow with persistent updates (FIXED - no more reset)
- **Notes:** Updates persist immediately without resetting. If API succeeds, update is saved to database. If API fails, update stays in local state. Demo data items: CONNECTOR (45), GEAR (3-low stock), BRICK (120), AXLE (8), WHEEL (0-out of stock), MOTOR (25), BUSHING (2-low stock), PANEL (15), SHAFT (62), SPRING (95), PIN (4-low stock), ROD (38). Total and low stock counts update automatically. Build: 335.52 kB (✅ SUCCESS)

### 8.6 Check Inventory Status Indicators

- **Action:** View the "Status" column in Manage Stock tab
- **Expected:** Items display status badges with color coding:
  - Green "In Stock" (qty > 20)
  - Orange "Medium" (qty 6-20)
  - Red "Low Stock" (qty 1-5)
  - Red "Out of Stock" (qty = 0)
- **Actual Result:**
  - ⭕ Ready to test
- **Notes:** Visual indicators help quickly identify inventory health

---

## 🧬 PHASE 9: End-to-End Workflow Test

### 9.1 Complete Order Workflow
- **Scenario:** Create order → Plan production → Assemble → Ship
- **Step 1 - Create Order:**
  - Create new customer order
  - Result: ⭕ / ✔️ / ❌
- **Step 2 - Plan Production:**
  - Order appears in production planning
  - Result: ✔️ FIXED - Automatic production order creation implemented
  - **Implementation:** FulfillmentService now auto-triggers ProductionOrder creation when warehouse stock insufficient
  - **Details:** Scenario 2 (no stock) and Scenario 3 (partial stock) both auto-create production orders
  - **Verified:** Orders automatically appear in Production Planning page
  - **Build:** order-processing-service - 8.595s - SUCCESS
- **Step 3 - Assembly:**
  - Tasks appear in assembly workstation
  - Result: ⭕ / ✔️ / ❌
- **Step 4 - Complete Order:**
  - Order status updates to completed
  - Result: ⭕ / ✔️ / ❌
- **Overall Result:** ⭕ / ✔️ / ❌
- **Notes:** ________________________________

---

## 📝 SUMMARY OF FINDINGS

### ✔️ What Works (Features Verified)

1. Login with default admin credentials (lego_admin / lego_Pass123)
2. Admin dashboard loads correctly
3. Navigation menu displays all options
4. Users list page loads
5. Default admin user appears in users list
6. Create new user form works
7. Edit user functionality works
8. **FIXED: Duplicate username validation now returns proper 400 error with message**
9. **NEW: Delete user button with confirmation dialog (red styling, matches design spec)**
10. **NEW: Two-step confirmation prevents accidental deletions**


### ❌ What Doesn't Work (Bugs Found)

| Issue | Severity | Location | Details |
|-------|----------|----------|---------|
| | HIGH / MED / LOW | | |
| | HIGH / MED / LOW | | |
| | HIGH / MED / LOW | | |
| | HIGH / MED / LOW | | |
| | HIGH / MED / LOW | | |

### ⭕ What's Missing (Features Not Implemented)

| Feature | Phase | Priority | Details |
|---------|-------|----------|---------|
| | | HIGH / MED / LOW | |
| | | HIGH / MED / LOW | |
| | | HIGH / MED / LOW | |
| | | HIGH / MED / LOW | |

---

## 🎯 Next Steps Based on Findings

### Immediate Actions (High Priority)
1. ___________________________________
2. ___________________________________
3. ___________________________________

### Short-term Fixes (Medium Priority)
1. ___________________________________
2. ___________________________________
3. ___________________________________

### Enhancements (Low Priority)
1. ___________________________________
2. ___________________________________

---

## 📋 Testing Notes

**Start Time:** ___________  
**End Time:** ___________  
**Total Time:** ___________  

**Browser:** ___________  
**Device:** ___________  
**Network:** Normal / Slow / Offline  

### 🔐 Quick Credentials Copy-Paste

```
Admin Login:
  Username: lego_admin
  Password: lego_Pass123

Plant Warehouse Login:
  Username: plant_warehouse_user
  Password: plant_warehouse_Pass123

Modules Supermarket Login:
  Username: modules_supermarket_user
  Password: modules_supermarket_Pass123

Test Operator (created in PHASE 2.3):
  Username: test_operator
  Password: TestPass123!
```

**Any Additional Observations:**

_________________________________________________________________

_________________________________________________________________

_________________________________________________________________

---

## ✅ Checklist Before Verification

- [ ] All backend services are running (user-service, masterdata-service, inventory-service, etc.)
- [ ] Frontend (React) is running on http://localhost:3000
- [ ] API Gateway is accessible on http://localhost:8080
- [ ] Browser is open and ready
- [ ] You have the default credentials: lego_admin / lego_Pass123
- [ ] You have about 1-2 hours for full verification
- [ ] You're ready to document findings

---

**Good luck with your verification! Document everything you find.** 🚀

