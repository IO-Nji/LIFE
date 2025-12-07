# 📦 Inventory Management - Quick Reference

## Quick Start (30 seconds)

1. **Login:** Use credentials `lego_admin` / `lego_Pass123`
2. **Navigate:** Click "📦 Inventory Management" in the top navigation menu
3. **View:** Overview tab shows all inventory at a glance
4. **Update:** Switch to "Manage Stock" tab, select workstation, click Edit, update quantity, Save

## Two Tabs Explained

### 📊 Overview Tab
**What you see:**
- Total items across all workstations
- Count of low-stock items (≤5 qty)
- List of workstations with inventory summaries
- Alert section for items needing attention

**When to use it:**
- Quick status check on inventory health
- Identify which items need restocking
- Get system-wide inventory picture

### 🔧 Manage Stock Tab
**What you see:**
- All inventory items for selected workstation
- Quantity, item type, and status for each item
- Edit and Save buttons for updates

**When to use it:**
- Update specific item quantities
- Fix incorrect inventory counts
- Adjust stock after physical inventory check

## Status Indicator Colors

| Color | Status | Qty Range | Action |
|-------|--------|-----------|--------|
| 🟢 Green | In Stock | > 20 | No action needed |
| 🟠 Orange | Medium | 6-20 | Monitor closely |
| 🔴 Red | Low Stock | 1-5 | **Reorder soon** |
| 🔴 Red | Out of Stock | 0 | **Urgent: Reorder** |

## Common Tasks

### Check Low Stock Items
1. Go to Overview tab
2. Look for "Low Stock Alert" section (red header)
3. Review table of all items with qty ≤ 5
4. Click "Update Stock" to quickly jump to that item

### Update One Item's Quantity
1. Go to Manage Stock tab
2. Find the item in the table
3. Click "Edit" button
4. Type new quantity
5. Click "Save"
6. Watch for green success notification

### Switch Between Workstations
1. Go to Manage Stock tab
2. Use dropdown at top right: "Switch Workstation:"
3. Select different workstation from list
4. Table updates automatically with new workstation's inventory

### Bulk View All Inventory
1. Go to Overview tab
2. Scroll to "All Workstations Inventory Summary"
3. See each workstation as a clickable card
4. Click any workstation card → goes to Manage Stock tab for that WS

## What the Numbers Mean

**Total Items in Stock:** Sum of all quantities across all items in all workstations
- Example: If WS1 has 50 items and WS2 has 30 items → Total = 80

**Item Types:** Number of different kinds of items (not quantity)
- Example: LEGO_BRICK, GEAR, MOTOR, CONNECTOR = 4 item types

**Low Stock Items:** Count of individual items where quantity ≤ 5
- Example: 3 items are low stock = "3" shown in card

## Keyboard Shortcuts (When Editing)

| Key | Action |
|-----|--------|
| Tab | Move to next field |
| Enter | Save changes |
| Escape | Cancel edit |

## Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Can't find Inventory menu item | Make sure you're logged in as admin (lego_admin) |
| Quantity won't save | Check that number is ≥ 0 (no negative numbers allowed) |
| Page shows "No inventory items" | That workstation has no items yet; try another workstation |
| Data didn't refresh | Click the other tab and back, or refresh page (F5) |

## Pro Tips

1. **Use Overview first** - Get the big picture before diving into details
2. **Check low stock regularly** - Review the alert section at least daily
3. **Batch updates** - Group related updates together for efficiency
4. **Verify numbers** - Cross-check against physical inventory periodically
5. **Take notes** - Use the notification area to see what you just changed

## Contact Info

For technical support or bugs:
- Check error messages shown in red notification banner
- Verify internet connection to backend (API Gateway on port 8080)
- Ensure admin account hasn't been locked or disabled

## Related Pages

- **Admin Dashboard:** View overall system statistics
- **User Management:** Manage admin and other user accounts
- **Warehouse Management:** Configure workstations and locations
- **Plant Warehouse:** View inventory from operator perspective
- **Modules Supermarket:** View module-specific inventory
- **Parts Supply Warehouse:** View parts warehouse inventory

---

**Last Updated:** December 7, 2025  
**Status:** ✅ Production Ready
