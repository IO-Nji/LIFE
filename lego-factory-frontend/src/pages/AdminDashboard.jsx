import { useState, useEffect } from "react";
import axios from "axios";
import { useAuth } from "../context/AuthContext";

function AdminDashboard() {
  const { session } = useAuth();
  const [workstations, setWorkstations] = useState([]);
  const [selectedWorkstationId, setSelectedWorkstationId] = useState(null);
  const [workstationInventory, setWorkstationInventory] = useState({});
  const [workstationOrders, setWorkstationOrders] = useState({});
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState("overview");
  const [error, setError] = useState(null);
  const [systemStats, setSystemStats] = useState({
    totalOrders: 0,
    pendingOrders: 0,
    processingOrders: 0,
    completedOrders: 0,
    totalWorkstations: 0,
  });

  useEffect(() => {
    if (session?.user?.role === "ADMIN") {
      fetchWorkstations();
    } else {
      setError("Access denied. Admin role required to view this dashboard.");
    }
  }, [session]);

  // Separate effect for polling after workstations are loaded
  useEffect(() => {
    if (workstations.length > 0) {
      // Set up polling interval - refresh every 15 seconds
      const pollInterval = setInterval(() => {
        fetchAllWorkstationsData(workstations);
      }, 15000); // 15 seconds
      return () => clearInterval(pollInterval);
    }
  }, [workstations]);

  useEffect(() => {
    if (selectedWorkstationId) {
      fetchWorkstationData(selectedWorkstationId);
    }
  }, [selectedWorkstationId]);

  const fetchWorkstations = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get("/api/masterdata/workstations");
      const stations = Array.isArray(response.data) ? response.data : [];
      setWorkstations(stations);
      setSystemStats(prev => ({
        ...prev,
        totalWorkstations: stations.length
      }));
      
      if (stations.length > 0) {
        setSelectedWorkstationId(stations[0].id);
      }

      // Await the data fetch before setting loading to false
      await fetchAllWorkstationsData(stations);
    } catch (err) {
      setError("Failed to load workstations: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const fetchAllWorkstationsData = async (stations) => {
    const inventoryMap = {};
    const ordersMap = {};

    for (const station of stations) {
      try {
        const invResponse = await axios.get(`/api/inventory/workstation/${station.id}`);
        inventoryMap[station.id] = Array.isArray(invResponse.data) ? invResponse.data : [];
      } catch (err) {
        console.error(`Failed to fetch inventory for WS-${station.id}:`, err);
        inventoryMap[station.id] = [];
      }

      try {
        const ordersResponse = await axios.get(`/api/customer-orders/workstation/${station.id}`);
        ordersMap[station.id] = Array.isArray(ordersResponse.data) ? ordersResponse.data : [];
      } catch (err) {
        console.error(`Failed to fetch orders for WS-${station.id}:`, err);
        ordersMap[station.id] = [];
      }
    }

    setWorkstationInventory(inventoryMap);
    setWorkstationOrders(ordersMap);
    calculateSystemStats(ordersMap);
  };

  const fetchWorkstationData = async (workstationId) => {
    try {
      const invResponse = await axios.get(`/api/inventory/workstation/${workstationId}`);
      setWorkstationInventory(prev => ({
        ...prev,
        [workstationId]: Array.isArray(invResponse.data) ? invResponse.data : []
      }));
    } catch (err) {
      console.error(`Failed to fetch inventory for WS-${workstationId}:`, err);
    }

    try {
      const ordersResponse = await axios.get(`/api/customer-orders/workstation/${workstationId}`);
      setWorkstationOrders(prev => ({
        ...prev,
        [workstationId]: Array.isArray(ordersResponse.data) ? ordersResponse.data : []
      }));
    } catch (err) {
      console.error(`Failed to fetch orders for WS-${workstationId}:`, err);
    }
  };

  const calculateSystemStats = (ordersMap) => {
    let total = 0;
    let pending = 0;
    let processing = 0;
    let completed = 0;

    Object.values(ordersMap).forEach(orderList => {
      orderList.forEach(order => {
        total++;
        if (order.status === "PENDING") pending++;
        else if (order.status === "PROCESSING") processing++;
        else if (order.status === "COMPLETED") completed++;
      });
    });

    setSystemStats(prev => ({
      ...prev,
      totalOrders: total,
      pendingOrders: pending,
      processingOrders: processing,
      completedOrders: completed,
    }));
  };

  const getWorkstationName = (id) => {
    const station = workstations.find(s => s.id === id);
    return station?.name || `Workstation ${id}`;
  };

  const getTotalInventoryQuantity = (workstationId) => {
    const inventory = workstationInventory[workstationId] || [];
    return inventory.reduce((sum, item) => sum + (item.quantity || 0), 0);
  };

  const renderOverviewTab = () => (
    <div className="overview-tab">
      <h3>System Statistics</h3>
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-value">{systemStats.totalWorkstations}</div>
          <div className="stat-label">Total Workstations</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{systemStats.totalOrders}</div>
          <div className="stat-label">Total Orders</div>
        </div>
        <div className="stat-card pending">
          <div className="stat-value">{systemStats.pendingOrders}</div>
          <div className="stat-label">Pending Orders</div>
        </div>
        <div className="stat-card processing">
          <div className="stat-value">{systemStats.processingOrders}</div>
          <div className="stat-label">Processing Orders</div>
        </div>
        <div className="stat-card completed">
          <div className="stat-value">{systemStats.completedOrders}</div>
          <div className="stat-label">Completed Orders</div>
        </div>
      </div>

      <h3>Workstations Overview</h3>
      <table className="overview-table">
        <thead>
          <tr>
            <th>Workstation ID</th>
            <th>Name</th>
            <th>Total Inventory Items</th>
            <th>Recent Orders</th>
            <th>Pending Orders</th>
          </tr>
        </thead>
        <tbody>
          {workstations.map(station => {
            const orders = workstationOrders[station.id] || [];
            const pendingCount = orders.filter(o => o.status === "PENDING").length;
            return (
              <tr key={station.id}>
                <td>WS-{station.id}</td>
                <td>{station.name}</td>
                <td>{getTotalInventoryQuantity(station.id)}</td>
                <td>{orders.length}</td>
                <td className="pending-count">{pendingCount}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );

  const renderWorkstationTab = () => {
    const inventory = workstationInventory[selectedWorkstationId] || [];
    const orders = workstationOrders[selectedWorkstationId] || [];

    return (
      <div className="workstation-tab">
        <div className="workstation-selector">
          <label>Select Workstation: </label>
          <select
            value={selectedWorkstationId || ""}
            onChange={(e) => setSelectedWorkstationId(parseInt(e.target.value))}
          >
            {workstations.map(station => (
              <option key={station.id} value={station.id}>
                WS-{station.id}: {station.name}
              </option>
            ))}
          </select>
        </div>

        <div className="workstation-content">
          <div className="inventory-section">
            <h4>Inventory at {getWorkstationName(selectedWorkstationId)}</h4>
            {inventory.length > 0 ? (
              <table className="inventory-table">
                <thead>
                  <tr>
                    <th>Item Type</th>
                    <th>Item ID</th>
                    <th>Quantity</th>
                    <th>Last Updated</th>
                  </tr>
                </thead>
                <tbody>
                  {inventory.map((item, idx) => (
                    <tr key={idx}>
                      <td>
                        <span className={`item-type-badge ${(item.itemType || "PRODUCT").toLowerCase()}`}>
                          {item.itemType || "PRODUCT"}
                        </span>
                      </td>
                      <td>#{item.itemId}</td>
                      <td className="quantity-value">{item.quantity}</td>
                      <td>{item.updatedAt ? new Date(item.updatedAt).toLocaleDateString() : "N/A"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p className="info-text">No inventory records for this workstation</p>
            )}
          </div>

          <div className="orders-section">
            <h4>Pending Orders at {getWorkstationName(selectedWorkstationId)}</h4>
            {orders.length > 0 ? (
              <div className="orders-list">
                {orders.map(order => (
                  <div key={order.id} className="order-card-admin">
                    <div className="order-header">
                      <div className="order-title">
                        <p><strong>Order #{order.orderNumber}</strong></p>
                        <span className={`status-badge status-${order.status.toLowerCase()}`}>
                          {order.status}
                        </span>
                      </div>
                      <div className="order-meta">
                        <p><strong>Date:</strong> {new Date(order.orderDate).toLocaleDateString()}</p>
                        <p><strong>Priority:</strong> <span className="priority-badge">{order.priority || "NORMAL"}</span></p>
                      </div>
                    </div>
                    
                    {order.orderItems && order.orderItems.length > 0 && (
                      <div className="order-items">
                        <table className="items-table">
                          <thead>
                            <tr>
                              <th>Product Name</th>
                              <th>Quantity</th>
                              <th>Type</th>
                            </tr>
                          </thead>
                          <tbody>
                            {order.orderItems.map((item, idx) => (
                              <tr key={idx}>
                                <td>{item.productName || item.productId || "Unknown"}</td>
                                <td className="quantity-cell">{item.quantity}</td>
                                <td>
                                  <span className={`item-type-badge ${(item.itemType || "PRODUCT").toLowerCase()}`}>
                                    {item.itemType || "PRODUCT"}
                                  </span>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="info-text">No pending orders for this workstation</p>
            )}
          </div>
        </div>
      </div>
    );
  };

  const renderAllInventoryTab = () => (
    <div className="all-inventory-tab">
      <h3>All Workstations Inventory Summary</h3>
      <table className="all-inventory-table">
        <thead>
          <tr>
            <th>Workstation</th>
            <th>Item Type</th>
            <th>Count</th>
            <th>Total Quantity</th>
          </tr>
        </thead>
        <tbody>
          {workstations.map(station => {
            const inventory = workstationInventory[station.id] || [];
            const products = inventory.filter(i => i.itemType === "PRODUCT" || !i.itemType);
            const modules = inventory.filter(i => i.itemType === "MODULE");
            const parts = inventory.filter(i => i.itemType === "PART");

            return [
              <tr key={`${station.id}-all`} className="station-header">
                <td colSpan="4" className="station-name">
                  <strong>WS-{station.id}: {station.name}</strong>
                </td>
              </tr>,
              products.length > 0 && (
                <tr key={`${station.id}-products`}>
                  <td></td>
                  <td>PRODUCT</td>
                  <td>{products.length}</td>
                  <td>{products.reduce((sum, i) => sum + (i.quantity || 0), 0)}</td>
                </tr>
              ),
              modules.length > 0 && (
                <tr key={`${station.id}-modules`}>
                  <td></td>
                  <td>MODULE</td>
                  <td>{modules.length}</td>
                  <td>{modules.reduce((sum, i) => sum + (i.quantity || 0), 0)}</td>
                </tr>
              ),
              parts.length > 0 && (
                <tr key={`${station.id}-parts`}>
                  <td></td>
                  <td>PART</td>
                  <td>{parts.length}</td>
                  <td>{parts.reduce((sum, i) => sum + (i.quantity || 0), 0)}</td>
                </tr>
              ),
            ];
          })}
        </tbody>
      </table>
    </div>
  );

  if (session?.user?.role !== "ADMIN") {
    return (
      <section className="admin-dashboard">
        <div className="error-message">
          ⚠️ Access Denied: Admin role required. Current role: {session?.user?.role || "Unknown"}
        </div>
      </section>
    );
  }

  return (
    <section className="admin-dashboard">
      <h2>Admin Dashboard - System Overview</h2>
      <p>User: <strong>{session?.user?.username || "Unknown"}</strong> (Admin)</p>

      {error && <div className="error-message">{error}</div>}

      <div className="tabs">
        <button
          className={`tab-button ${activeTab === "overview" ? "active" : ""}`}
          onClick={() => setActiveTab("overview")}
        >
          📊 System Overview
        </button>
        <button
          className={`tab-button ${activeTab === "workstation" ? "active" : ""}`}
          onClick={() => setActiveTab("workstation")}
        >
          🏭 Workstation Details
        </button>
        <button
          className={`tab-button ${activeTab === "inventory" ? "active" : ""}`}
          onClick={() => setActiveTab("inventory")}
        >
          📦 All Inventory
        </button>
      </div>

      <div className="tab-content">
        {loading && <div className="loading">Loading system data...</div>}
        {!loading && activeTab === "overview" && renderOverviewTab()}
        {!loading && activeTab === "workstation" && renderWorkstationTab()}
        {!loading && activeTab === "inventory" && renderAllInventoryTab()}
      </div>

      <style>{`
        .admin-dashboard {
          padding: 20px;
        }

        .tabs {
          display: flex;
          gap: 10px;
          margin: 20px 0;
          border-bottom: 2px solid #ddd;
        }

        .tab-button {
          padding: 10px 20px;
          border: none;
          background: #f0f0f0;
          cursor: pointer;
          font-weight: bold;
          border-radius: 4px 4px 0 0;
          transition: all 0.3s;
        }

        .tab-button.active {
          background: #2c5aa0;
          color: white;
          border-bottom: 3px solid #2c5aa0;
        }

        .tab-button:hover {
          background: #e0e0e0;
        }

        .tab-button.active:hover {
          background: #1e3f5a;
        }

        .tab-content {
          margin-top: 20px;
          background: #f9f9f9;
          border: 1px solid #ddd;
          border-radius: 4px;
          padding: 15px;
        }

        .overview-tab {
          padding: 15px;
        }

        .stats-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
          gap: 15px;
          margin-bottom: 30px;
        }

        .stat-card {
          background: white;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 20px;
          text-align: center;
          transition: all 0.3s;
        }

        .stat-card:hover {
          box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .stat-card.pending {
          border-top: 3px solid #ffc107;
        }

        .stat-card.processing {
          border-top: 3px solid #17a2b8;
        }

        .stat-card.completed {
          border-top: 3px solid #28a745;
        }

        .stat-value {
          font-size: 32px;
          font-weight: bold;
          color: #2c5aa0;
          margin-bottom: 5px;
        }

        .stat-label {
          font-size: 13px;
          color: #666;
          font-weight: bold;
        }

        .overview-table,
        .inventory-table,
        .all-inventory-table {
          width: 100%;
          border-collapse: collapse;
          margin-top: 15px;
          background: white;
        }

        .overview-table th,
        .inventory-table th,
        .all-inventory-table th {
          background: #e8f4f8;
          padding: 12px;
          text-align: left;
          font-weight: bold;
          border-bottom: 2px solid #b8d4e0;
        }

        .overview-table td,
        .inventory-table td,
        .all-inventory-table td {
          padding: 10px 12px;
          border-bottom: 1px solid #ddd;
        }

        .overview-table tbody tr:hover,
        .inventory-table tbody tr:hover,
        .all-inventory-table tbody tr:hover {
          background: #f5f5f5;
        }

        .pending-count {
          font-weight: bold;
          color: #dc3545;
        }

        .workstation-selector {
          margin-bottom: 20px;
          padding: 15px;
          background: white;
          border: 1px solid #ddd;
          border-radius: 4px;
        }

        .workstation-selector label {
          font-weight: bold;
          margin-right: 10px;
        }

        .workstation-selector select {
          padding: 8px 12px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 14px;
        }

        .workstation-content {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px;
        }

        .inventory-section,
        .orders-section {
          background: white;
          border: 1px solid #ddd;
          border-radius: 4px;
          padding: 15px;
        }

        .orders-list {
          display: flex;
          flex-direction: column;
          gap: 10px;
        }

        .order-card-admin {
          background: #f9f9f9;
          border: 1px solid #ddd;
          border-radius: 4px;
          padding: 12px;
          border-left: 4px solid #2c5aa0;
        }

        .order-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          margin-bottom: 12px;
          padding-bottom: 10px;
          border-bottom: 1px solid #e0e0e0;
        }

        .order-title {
          display: flex;
          align-items: center;
          gap: 10px;
        }

        .order-title p {
          margin: 0;
          font-size: 14px;
          font-weight: bold;
          color: #333;
        }

        .order-meta {
          display: flex;
          gap: 15px;
          font-size: 12px;
        }

        .order-meta p {
          margin: 0;
          color: #666;
        }

        .priority-badge {
          display: inline-block;
          padding: 2px 8px;
          border-radius: 3px;
          font-size: 11px;
          font-weight: bold;
          background: #fff3cd;
          color: #856404;
        }

        .order-items {
          margin-top: 10px;
          overflow-x: auto;
        }

        .items-table {
          width: 100%;
          border-collapse: collapse;
          font-size: 12px;
          background: white;
          border: 1px solid #e0e0e0;
          border-radius: 3px;
        }

        .items-table thead {
          background: #f0f0f0;
        }

        .items-table th {
          padding: 8px;
          text-align: left;
          font-weight: bold;
          color: #333;
          border-bottom: 1px solid #ddd;
        }

        .items-table td {
          padding: 8px;
          border-bottom: 1px solid #f0f0f0;
          color: #555;
        }

        .items-table tbody tr:hover {
          background: #f9f9f9;
        }

        .quantity-cell {
          font-weight: bold;
          text-align: center;
          color: #2c5aa0;
        }

        .order-info p {
          margin: 5px 0;
          font-size: 13px;
        }

        .status-badge {
          display: inline-block;
          padding: 3px 8px;
          border-radius: 3px;
          font-size: 11px;
          font-weight: bold;
          margin-left: 5px;
        }

        .status-pending {
          background: #fff3cd;
          color: #856404;
        }

        .status-processing {
          background: #cfe2ff;
          color: #084298;
        }

        .status-completed {
          background: #d1e7dd;
          color: #0f5132;
        }

        .item-type-badge {
          display: inline-block;
          padding: 3px 8px;
          border-radius: 3px;
          font-size: 11px;
          font-weight: bold;
        }

        .item-type-badge.product {
          background: #e8f4f8;
          color: #084298;
        }

        .item-type-badge.module {
          background: #fff3cd;
          color: #856404;
        }

        .item-type-badge.part {
          background: #d1e7dd;
          color: #0f5132;
        }

        .quantity-value {
          font-weight: bold;
          color: #2c5aa0;
          text-align: center;
        }

        .station-header {
          background: #f0f0f0;
          font-weight: bold;
        }

        .station-name {
          color: #2c5aa0 !important;
          padding: 12px !important;
        }

        .info-text {
          color: #666;
          font-style: italic;
          padding: 15px;
          text-align: center;
        }

        .error-message {
          background: #f8d7da;
          color: #721c24;
          padding: 12px;
          border-radius: 4px;
          margin-bottom: 15px;
          border: 1px solid #f5c6cb;
        }

        .loading {
          text-align: center;
          padding: 40px;
          color: #666;
          font-size: 16px;
        }
      `}</style>
    </section>
  );
}

export default AdminDashboard;
