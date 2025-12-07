# 🧪 Demo Data Testing Guide - Inventory Management

## Overview

The Inventory Management page now includes **automatic demo/test data** that loads when:
1. The backend API has no inventory data
2. The API endpoint fails to respond
3. You want to test the UI without setting up backend inventory first

This allows you to test the Edit, Save, and Update functionality immediately.

## Demo Data Available

### Workstation 1 Demo Items
| Item ID | Item Type | Qty | Status |
|---------|-----------|-----|--------|
| 101 | CONNECTOR | 45 | 🟢 In Stock |
| 102 | GEAR | 3 | 🔴 Low Stock |
| 103 | BRICK | 120 | 🟢 In Stock |
| 104 | AXLE | 8 | 🟠 Medium |
| 105 | WHEEL | 0 | 🔴 Out of Stock |

### Workstation 2 Demo Items
| Item ID | Item Type | Qty | Status |
|---------|-----------|-----|--------|
| 201 | MOTOR | 25 | 🟢 In Stock |
| 202 | BUSHING | 2 | 🔴 Low Stock |
| 203 | PANEL | 15 | 🟠 Medium |
| 204 | SHAFT | 62 | 🟢 In Stock |

### Workstation 3 Demo Items
| Item ID | Item Type | Qty | Status |
|---------|-----------|-----|--------|
| 301 | SPRING | 95 | 🟢 In Stock |
| 302 | PIN | 4 | 🔴 Low Stock |
| 303 | ROD | 38 | 🟢 In Stock |

## Quick Testing Steps

### 1. Login and Navigate
```
1. Login as: lego_admin / lego_Pass123
2. Click "📦 Inventory Management" in menu
3. Wait for page to load (should show demo data)
```

### 2. Test Overview Tab
```
1. Stay on Overview tab
2. Verify you see:
   ✔️ Total Items count (shows sum of all demo items)
   ✔️ Active Workstations count (3 workstations)
   ✔️ Low Stock Items count (3 items: GEAR, BUSHING, PIN)
   ✔️ Low Stock Alert table with red flag items
   ✔️ All Workstations Summary grid
```

### 3. Test Edit Functionality
```
1. Click "Manage Stock" tab
2. Workstation dropdown should show all 3 workstations
3. Select "Workstation 1" (if not already selected)
4. Inventory table appears with 5 demo items
5. Click [Edit] button on any item (e.g., GEAR with qty=3)
6. Quantity field becomes editable input box
7. Type new value (e.g., 15)
8. Click [Save]
9. See success notification: "Item 102 quantity updated to 15"
10. Verify table updated and GEAR no longer in Low Stock
11. Switch to Overview tab - should see updated count
```

### 4. Test Cancel Functionality
```
1. On Manage Stock tab, click [Edit] on an item
2. Type a new quantity
3. Click [Cancel] instead of Save
4. Quantity reverts to original value
5. No success notification shown
```

### 5. Test Validation
```
1. Click [Edit] on any item
2. Try to enter -5 or negative number
3. Click [Save]
4. Should see error: "Quantity cannot be negative"
5. Change to 0 or positive number
6. Try saving again - should work
```

### 6. Test Low Stock Alerts
```
1. Go to Overview tab
2. Look for "Low Stock Alert" section
3. Should show 3 items with qty ≤ 5:
   - GEAR (102): qty 3
   - BUSHING (202): qty 2
   - PIN (302): qty 4
4. Click "Update Stock" button on any
5. Should jump to Manage Stock tab for that workstation
6. Edit the item's quantity to > 5
7. Save and return to Overview
8. Low stock item should disappear from alert
```

### 7. Test Workstation Switching
```
1. On Manage Stock tab
2. Use dropdown: "Switch Workstation"
3. Select different workstation
4. Table updates showing items for that workstation
5. Edit items in this workstation
6. Switch to another workstation - items update
```

## Expected Results for Each Test

### Test 1: Overview Loads
- ✅ Page shows all stats
- ✅ Demo data present
- ✅ Low stock section visible
- ✅ Workstations grid shows all 3 workstations
- ✅ Totals: ~370 items across all workstations

### Test 2: Items Display
- ✅ Manage Stock tab shows items
- ✅ Each item shows: Item Type, Item ID, Qty, Status, Edit button
- ✅ Status badges color-coded correctly

### Test 3: Edit Workflow
- ✅ Click Edit → input appears
- ✅ Type new value → input accepts number
- ✅ Click Save → success notification
- ✅ Table updates immediately
- ✅ Overview totals refresh

### Test 4: Cancel Works
- ✅ Click Edit → input appears
- ✅ Type new value
- ✅ Click Cancel → reverts to original
- ✅ No notification shown

### Test 5: Validation Works
- ✅ Negative numbers rejected
- ✅ Error message shown
- ✅ Zero and positive numbers accepted

### Test 6: Alerts Update
- ✅ Low stock items listed in alert
- ✅ Update button navigates correctly
- ✅ Updated items disappear from alert

### Test 7: Workstation Switch
- ✅ Dropdown shows all workstations
- ✅ Table updates on selection
- ✅ Items differ per workstation

## Troubleshooting

### No items appearing?
- Check browser console (F12) for errors
- Ensure you logged in as admin
- Clear cache and refresh page
- Try different workstation from dropdown

### Edit button not working?
- Make sure you're on "Manage Stock" tab
- Check that a workstation is selected
- Items table should be visible first

### Save not working?
- Check if quantity is empty or invalid
- Try entering 0 or a positive number
- Check browser console for API errors

### Demo data not showing?
- This is intentional fallback when backend has no data
- Check API Gateway is running on http://localhost:8080
- If real data exists in backend, demo data won't be used

## Browser Console Warnings

You may see this in browser console (F12):
```
"No inventory data from API for WS-1, using demo data for testing"
```

This is **normal and expected** when:
- Backend inventory service has no data
- Or the API endpoint isn't returning results

The demo data automatically activates as a fallback for testing.

## When to Use Demo Data

### ✅ Use demo data for:
- Testing UI functionality
- Testing edit/save/cancel workflows
- Testing workstation switching
- Testing low stock alerts
- Demonstrating features
- Development and testing

### ❌ Don't use demo data for:
- Production testing with real data
- Performance testing with actual inventory
- Acceptance testing with business data

## Notes

1. **Demo data is LOCAL ONLY** - Changes only update in browser memory
2. **Refresh resets changes** - Refreshing page reloads demo data
3. **Real data takes precedence** - If backend has actual inventory, it's used instead
4. **No database updates** - Demo updates don't persist to backend

## Summary

The demo data feature allows you to:
- ✔️ Test the entire UI without backend setup
- ✔️ Verify Edit/Save/Cancel functionality
- ✔️ Check status indicators and alerts
- ✔️ Test workstation switching
- ✔️ Validate form inputs

Now you can test Inventory Management immediately upon deployment!

---

**Test Demo Data Feature:** ✅ Ready to test
**Build Status:** ✅ Success (335.37 kB bundle)
**Last Updated:** December 7, 2025
