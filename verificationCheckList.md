# 🚀 LIFE Application - Step-by-Step Verification & Testing Plan

> A methodical checklist for verifying application requirements, testing existing features, and documenting missing functionality based on the thesis requirements.

---

## 📋 Verification Approach

This plan follows the user stories and workflow scenarios from `checkList.md` in a structured, step-by-step manner. Each phase focuses on specific roles and their core functionality, building from basic to complex workflows.

**Methodology:**
1. Verify each user story by testing the application
2. Document status (✔️ Pass / ❌ Fail / ⭕ Not Started)
3. Log issues and missing features
4. Prepare implementation roadmap for missing functionality

---

## 🔐 Phase 1: Authentication & Authorization

**Objective:** Verify user login, registration, and role-based access control.

### User Stories to Test
* **ADM-001** ⭕ Create a new user (Administrator)
* **ADM-002** ⭕ View the users list (Administrator)
* **ADM-003** ⭕ Modify an existing user (Administrator)

### Verification Steps

#### Step 1.1: Admin Login
- [ ] **Action:** Navigate to login page
- [ ] **Expected:** Login form displays with username/password fields
- [ ] **Verification:** 
  - Enter credentials for admin account (legoAdmin / legoPass)
  - Click "Login"
  - Verify: Admin dashboard appears and user is authenticated
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:** 

#### Step 1.2: Verify Admin Dashboard Access
- [ ] **Action:** Check if admin dashboard is visible
- [ ] **Expected:** Dashboard with KPIs, user management options, and workstation configuration
- [ ] **Verification:**
  - Confirm dashboard title and layout match admin role
  - Verify access to user management features
  - Check for any error messages or broken features
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 1.3: Create New User (ADM-001)
- [ ] **Action:** Locate "Create User" or "Add User" button/link
- [ ] **Expected:** Form with fields: Username, Password, Department, Role, Workstation
- [ ] **Verification (Success Case):**
  - Click "Create User"
  - Fill in valid data (e.g., testuser / TestPass123!)
  - Assign role (e.g., PLANT_WAREHOUSE)
  - Assign workstation
  - Click "Save"
  - Verify: Confirmation message displays and user appears in user list
- [ ] **Verification (Failure Case):**
  - Try to create user with existing username
  - Try weak password
  - Verify: Error messages are displayed appropriately
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 1.4: View Users List (ADM-002)
- [ ] **Action:** Navigate to Users List page
- [ ] **Expected:** Table showing all users with columns: Username, Role, Department, Workstation, Actions
- [ ] **Verification:**
  - Confirm all created users are visible
  - Verify role assignments are correct
  - Check if workstation assignments display correctly
  - Test pagination if more than 10 users exist
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 1.5: Modify Existing User (ADM-003)
- [ ] **Action:** Click "Edit" on a user in the users list
- [ ] **Expected:** User edit form with editable fields: Password, Department, Role, Workstation
- [ ] **Verification (Success Case):**
  - Change role from PLANT_WAREHOUSE to MODULES_SUPERMARKET
  - Reassign workstation
  - Click "Save"
  - Verify: Confirmation message and list reflects changes
- [ ] **Verification (Delete Case - if applicable):**
  - Click "Delete" on a user
  - Verify: Confirmation dialog appears
  - Confirm deletion
  - Verify: User no longer appears in list
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 1.6: Test Role-Based Access Control
- [ ] **Action:** Logout and login as different role (e.g., PLANT_WAREHOUSE user)
- [ ] **Expected:** Role-specific dashboard appears (not admin dashboard)
- [ ] **Verification:**
  - Confirm user cannot access admin features
  - Verify correct workstation-specific interface appears
  - Check that user can only see their assigned workstation's data
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

---

## 📦 Phase 2: Plant Warehouse - Core Operations

**Objective:** Verify Plant Warehouse user can view orders, check stock, and manage inventory.

### User Stories to Test
* **PLW-001** ⭕ View customer orders
* **PLW-002** ⭕ View warehouse stock records
* **PLW-003** ⭕ Modify stock records
* **PLW-004** ⭕ Create a Warehouse Order
* **PLW-005** ⭕ View Warehouse Orders

### Verification Steps

#### Step 2.1: Plant Warehouse Login
- [ ] **Action:** Login as Plant Warehouse user (warehouseOperator / warehousePass)
- [ ] **Expected:** Plant Warehouse dashboard displays with order list
- [ ] **Verification:**
  - Confirm login successful
  - Verify Plant Warehouse dashboard layout
  - Check for Customer Orders section
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 2.2: View Customer Orders (PLW-001)
- [ ] **Action:** Navigate to "Customer Orders" or "Incoming Orders" section
- [ ] **Expected:** Table showing pending customer orders with: Order ID, Product, Quantity, Status, Date
- [ ] **Verification:**
  - Confirm orders are displayed
  - Check if orders can be sorted/filtered
  - Verify order details can be expanded/clicked
  - Confirm status shows correctly (PENDING, CONFIRMED, PROCESSING, etc.)
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 2.3: View Warehouse Stock Records (PLW-002)
- [ ] **Action:** Navigate to "Stock Records" or "Inventory" section
- [ ] **Expected:** List showing stock levels for all product variants with: Variant Name, Current Quantity, Reorder Level, Location
- [ ] **Verification:**
  - Confirm all product variants are listed
  - Check if quantities display correctly
  - Verify reorder thresholds are shown
  - Test search/filter by variant name
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 2.4: Modify Stock Records (PLW-003)
- [ ] **Action:** Select a stock record and click "Edit" or "Adjust Quantity"
- [ ] **Expected:** Form to adjust quantity with fields: Current Qty, Adjustment Amount, Reason
- [ ] **Verification (Increase Stock):**
  - Increase stock by 10 units
  - Add reason (e.g., "Received from production")
  - Click "Save"
  - Verify: Confirmation message and updated quantity displays
- [ ] **Verification (Decrease Stock):**
  - Decrease stock for fulfilling order
  - Verify: System prevents reducing below zero
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 2.5: Scenario 1 - Direct Fulfillment (Sunny Day)
- [ ] **Precondition:** Stock exists for required products
- [ ] **Step 1:** Select a Customer Order (PLW-001)
- [ ] **Step 2:** Click "Check Stock" (PLW-002)
- [ ] **Step 3:** Verify sufficient stock exists
- [ ] **Step 4:** Click "Fulfill Order"
- [ ] **Expected Outcome:** Order marked as FULFILLED, stock records updated (decreased)
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 2.6: Create Warehouse Order (PLW-004)
- [ ] **Action:** Select a Customer Order with insufficient stock
- [ ] **Expected:** "Create Warehouse Order" button/option appears
- [ ] **Verification:**
  - Click button
  - Form shows: Destination (Modules Supermarket or Production Planning), Required Quantity
  - Select destination based on lot size rule
  - Add items to warehouse order
  - Click "Send Order"
  - Verify: Order appears in "Warehouse Orders" list with status "PENDING"
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

#### Step 2.7: View Warehouse Orders (PLW-005)
- [ ] **Action:** Navigate to "Warehouse Orders" section
- [ ] **Expected:** List of all warehouse orders created by this Plant Warehouse
- [ ] **Verification:**
  - Confirm all created orders are listed
  - Check status column (PENDING, FULFILLED, CANCELLED)
  - Verify order details can be viewed
  - Test cancel functionality if available
- [ ] **Status:** ⭕ / ✔️ / ❌
- [ ] **Notes:**

---

## 🏢 Phase 3: Modules Supermarket Operations
Day 2: Modules Supermarket & Initial Production Planning
Morning (4 hours):

08:00 - 11:00: Plant Warehouse - Order Creation & Modules Supermarket Input.
User Story Testing: Test PLW-004 (Create a Warehouse Order - focused on sending it to Modules Supermarket). Test PLW-005 (View Warehouse Orders).
Workflow Testing: Continue testing "Customer Order Fulfillment - Scenario 2: Low Stock in Plant Warehouse, Modules Supermarket has Stock" (Section 2.3), up to "Modules Supermarket User receives the Warehouse Order."
Expected: Successful creation and display of a Warehouse Order.
11:00 - 12:00: Modules Supermarket - Core Functions.
User Story Testing: Test MSS-001 (View Module Stock Record), MSS-002 (Modify Module Stock Record - simple stock adjustment).
Expected: Visibility of module stock and ability to make basic adjustments.
Afternoon (4 hours):

12:00 - 15:30: Modules Supermarket - Order Creation & Production Planning Input.
User Story Testing: Test MSS-003 (Create a Production Order) when module stock is insufficient. Test MSS-004 (View Production Orders).
Workflow Testing: Continue Scenario 2, up to "Modules Supermarket User fulfills the Warehouse Order (MSS-002) by sending modules to Final Assembly." (If Final Assembly receipt is implemented).
Expected: Successful creation and display of a Production Order, and its receipt by Production Planning.
15:30 - 16:00: Documentation & Daily Review.
Update checklists, log bugs.
Deliverable: Updated checklists, bug log.
Day 3: Production Planning, Production Control & Manufacturing Workstations
Morning (4 hours):

08:00 - 11:00: Production Planning & Integration.
User Story Testing: Test PRP-001 (View Production Orders - focusing on those from Modules Supermarket).
Key Verification: How does SimAL.Scheduler integrate? Does PRP-002 (Modify Production Order - update status/times) provide any functional interaction with SimAL.Scheduler or merely display its output? If SimAL.Scheduler is a separate system, verify the data exchange points.
Expected: Production orders visible, indication of planning status.
11:00 - 12:00: Production Control - Order Creation.
User Story Testing: Test PRC-001 (View Planned Production Control Orders). Test functionality related to creating Production Supply Orders (ProductionSupplyOrder in Table 27) and Production Orders for manufacturing.
Expected: Production Control sees planned orders and can initiate new orders.
Afternoon (4 hours):

12:00 - 15:30: Manufacturing Workstations - Process Flow.
User Story Testing: Test IM-001, PPP-001, PAF-001 (View orders and start production/pre-production/finishing). Test IM-002, PPP-002, PAF-002 (Update actual 'Start'/'Finish' times). Verify PRC-002 (Production Control updates) as a result.
Expected: Workstations can receive, acknowledge, start, and complete tasks, and update times.
15:30 - 16:00: Documentation & Daily Review.
Update checklists, log bugs.
Deliverable: Updated checklists, bug log.
Day 4: Parts Supply Warehouse, Assembly Control & Assembly Workstations
Morning (4 hours):

08:00 - 10:00: Parts Supply Warehouse.
User Story Testing: Test PSW-001 (View Assembly Supply Orders) and PSW-002 (View Production Supply Orders). How does the system facilitate "supplying parts"? Is it a confirmation, a list, or a tracking mechanism?
Expected: Parts Supply Warehouse can view orders and mark parts as supplied.
10:00 - 12:00: Assembly Control - Order Creation.
User Story Testing: Test ASC-001 (View Planned Assembly Control Orders). Test functionality related to creating Assembly Supply Orders (ProductionSupplyOrder in Table 28 – note: ProductionSupplyOrder ID is used for Assembly Supply in your Table 28, might be a typo in thesis or intended reuse) and Assembly Orders for assembly workstations.
Expected: Assembly Control sees planned orders and can initiate new orders.
Afternoon (4 hours):

12:00 - 15:30: Assembly Workstations - Process Flow.
User Story Testing: Test GEA-001, MOA-001, FIA-001 (View orders and start assembly). Test GEA-002, MOA-002, FIA-002 (Update actual 'Start'/'Finish' times). Verify ASC-002 (Assembly Control updates) as a result.
Expected: Assembly Workstations can receive, acknowledge, start, and complete tasks, and update times.
15:30 - 16:00: Documentation & Daily Review.
Update checklists, log bugs.
Deliverable: Updated checklists, bug log.
Day 5: End-to-End Workflow, Reporting & Gap Analysis
Morning (4 hours):

08:00 - 11:00: End-to-End Workflow Testing.
Attempt to run through a full "Customer Order Fulfillment - Scenario 3" (Section 2.4) and/or "Scenario 4" (Section 2.5) if the implemented features allow for simulating these complex, multi-step processes.
This is crucial for identifying integration issues and gaps in the overall data flow.
Expected: Simulate as much of the full supply chain as possible within the current application.
11:00 - 12:00: Bug Triage & Prioritization.
Review all bugs logged throughout the week.
Categorize them by severity (e.g., Critical, High, Medium, Low) and priority.
Deliverable: Consolidated and prioritized bug list.
Afternoon (4 hours):

12:00 - 14:00: Gap Analysis & Missing Feature Definition.
Go through the entire User Stories Checklist and Workflow Document. Mark all items that are "Not Started" or where the current application provides no corresponding functionality.
For each major missing feature, briefly describe:
What functionality is missing.
Why it's important (based on thesis context).
Expected behavior if it were implemented.
Deliverable: Comprehensive list of identified missing features/services.
14:00 - 16:00: Final Report Preparation.
Summarize the testing conducted.
Present the current state of the application (e.g., percentage of user stories/workflows tested and passed).
Provide the consolidated bug list.
Present the detailed list of missing features/services.
Deliverable: Initial Test Report.
Average Time for Verification and Implementation of Missing Services/Features
This section addresses your request for time estimates. It's important to differentiate between verification (testing) and implementation (development).

Average Time for Verification (Testing) of an Implemented Feature:

Simple User Story (e.g., "View an Order"): 15-30 minutes. This involves navigating to a screen, checking data display, and confirming UI elements.
Complex User Story (e.g., "Create a Production Order"): 30-60 minutes. This involves data input, interaction with backend logic, and verifying output/status changes.
Workflow Scenario (End-to-End Run): 1-3 hours. This covers orchestrating multiple user stories and ensuring the data flows correctly between different roles/modules.
Overall Average per User Story: Based on the above, for testing and documenting a single user story, a reasonable average is 45 minutes to 1 hour.
Average Time for Identifying & Scoping a Missing Feature/Service:

During this one-week testing phase, you will primarily be identifying what's missing.
Initial Identification: This happens continuously as you go through the checklists and mark items as "Not Started." This is part of the daily testing effort.
Detailed Scoping/Definition (per major missing feature): Once a feature is identified as missing, defining its requirements clearly (what it needs to do, expected inputs/outputs, business rules) typically takes 1-3 hours per feature, done by a tester/business analyst. This output then serves as input for the development team.
Average Time for Implementation (Development) of a Missing Feature/Service:

Disclaimer: Estimating development time without knowledge of your team's size, skill set, technology stack, current architectural complexity, and specific feature details is highly speculative. These are general guidelines for developer effort and are not part of the testing plan itself, but inform subsequent development planning.
Simple Feature (e.g., adding a basic CRUD screen for a simple entity): 1-2 days (8-16 hours) of developer time.
Moderately Complex Feature (e.g., integrating with an external API like SimAL.Scheduler for a specific call, implementing a complex business rule): 3-5 days (24-40 hours) of developer time.
Highly Complex Feature (e.g., a complete new module with UI, backend logic, database schema changes, and multiple integrations): 1-4 weeks (40-160 hours) or more of developer time.