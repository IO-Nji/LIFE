import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import axios from "axios";
import "../styles/AdminDashboard.css";

function AdminDashboardPage() {
  const { session } = useAuth();
  const [workstations, setWorkstations] = useState([]);
  const [orders, setOrders] = useState({
    production: [],
    assembly: [],
    supply: [],
  });
  const [workstationStats, setWorkstationStats] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedWorkstation, setSelectedWorkstation] = useState(null);
  const [activeTab, setActiveTab] = useState("overview"); // overview, workstations, orders, supply

  useEffect(() => {
    fetchDashboardData();
    // Auto-refresh every 15 seconds
    const interval = setInterval(fetchDashboardData, 15000);
    return () => clearInterval(interval);
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setError(null);

    try {
      // Fetch workstations
      const wsResponse = await axios.get("/api/masterdata/workstations");
      setWorkstations(Array.isArray(wsResponse.data) ? wsResponse.data : []);

      // Fetch production control orders
      const prodResponse = await axios.get("/api/production-control-orders");
      setOrders((prev) => ({
        ...prev,
        production: Array.isArray(prodResponse.data) ? prodResponse.data : [],
      }));

      // Fetch assembly control orders
      const asmResponse = await axios.get("/api/assembly-control-orders");
      setOrders((prev) => ({
        ...prev,
        assembly: Array.isArray(asmResponse.data) ? asmResponse.data : [],
      }));

      // Fetch supply orders
      const supResponse = await axios.get("/api/supply-orders/warehouse");
      setOrders((prev) => ({
        ...prev,
        supply: Array.isArray(supResponse.data) ? supResponse.data : [],
      }));

      // Calculate stats
      calculateStats(wsResponse.data, prodResponse.data, asmResponse.data, supResponse.data);
    } catch (err) {
      setError("Failed to load dashboard data: " + (err.message || "Unknown error"));
      console.error("Dashboard fetch error:", err);
    } finally {
      setLoading(false);
    }
  };

  const calculateStats = (ws, prod, asm, supply) => {
    const stats = {};

    ws.forEach((workstation) => {
      const wsId = workstation.id;
      const prodOrders = Array.isArray(prod) ? prod.filter((o) => o.assignedWorkstationId === wsId) : [];
      const asmOrders = Array.isArray(asm) ? asm.filter((o) => o.assignedWorkstationId === wsId) : [];

      stats[wsId] = {
        workstationId: wsId,
        name: workstation.name || `Workstation ${wsId}`,
        type: workstation.workstationType || "Unknown",
        status: calculateWorkstationStatus(prodOrders, asmOrders),
        activeOrders: prodOrders.filter((o) => o.status === "IN_PROGRESS").length +
                      asmOrders.filter((o) => o.status === "IN_PROGRESS").length,
        completedToday: prodOrders.filter((o) => o.status === "COMPLETED").length +
                        asmOrders.filter((o) => o.status === "COMPLETED").length,
        totalOrders: prodOrders.length + asmOrders.length,
      };
    });

    setWorkstationStats(stats);
  };

  const calculateWorkstationStatus = (prodOrders, asmOrders) => {
    const allOrders = [...prodOrders, ...asmOrders];
    if (allOrders.length === 0) return "idle";
    if (allOrders.some((o) => o.status === "IN_PROGRESS")) return "active";
    if (allOrders.some((o) => o.status === "PENDING" || o.status === "SCHEDULED")) return "waiting";
    return "idle";
  };

  const getStatusColor = (status) => {
    const colors = {
      PENDING: "#FFA500",
      SCHEDULED: "#87CEEB",
      IN_PROGRESS: "#4169E1",
      COMPLETED: "#32CD32",
      REJECTED: "#FF6347",
      CANCELLED: "#808080",
      active: "#4169E1",
      idle: "#90EE90",
      waiting: "#FFD700",
    };
    return colors[status] || "#808080";
  };

  const getPriorityColor = (priority) => {
    const colors = {
      HIGH: "#FF4444",
      MEDIUM: "#FFA500",
      LOW: "#4CAF50",
    };
    return colors[priority] || "#808080";
  };

  if (loading && !workstations.length) {
    return (
      <section className="admin-dashboard">
        <h2>🏭 Factory Admin Dashboard</h2>
        <div className="loading-state">Loading dashboard data...</div>
      </section>
    );
  }

  const totalOrders = orders.production.length + orders.assembly.length;
  const activeOrdersCount = orders.production.filter((o) => o.status === "IN_PROGRESS").length +
                           orders.assembly.filter((o) => o.status === "IN_PROGRESS").length;
  const supplyPendingCount = orders.supply.filter((o) => o.status === "PENDING").length;
  const activeWorkstations = Object.values(workstationStats).filter((ws) => ws.status === "active").length;

  return (
    <section className="admin-dashboard">
      <h2>🏭 Factory Admin Dashboard</h2>
      <p className="admin-subtitle">Real-time monitoring and control of factory operations</p>

      {error && <div className="error-alert">{error}</div>}

      {/* KPI Cards */}
      <div className="kpi-grid">
        <div className="kpi-card">
          <div className="kpi-label">Total Orders</div>
          <div className="kpi-value">{totalOrders}</div>
          <div className="kpi-detail">Across all workstations</div>
        </div>
        <div className="kpi-card active">
          <div className="kpi-label">Active Orders</div>
          <div className="kpi-value">{activeOrdersCount}</div>
          <div className="kpi-detail">In progress now</div>
        </div>
        <div className="kpi-card">
          <div className="kpi-label">Active Workstations</div>
          <div className="kpi-value">{activeWorkstations}</div>
          <div className="kpi-detail">Out of {workstations.length}</div>
        </div>
        <div className="kpi-card">
          <div className="kpi-label">Pending Supply</div>
          <div className="kpi-value">{supplyPendingCount}</div>
          <div className="kpi-detail">Waiting to be fulfilled</div>
        </div>
      </div>

      {/* Tabs */}
      <div className="dashboard-tabs">
        <button
          className={`tab-button ${activeTab === "overview" ? "active" : ""}`}
          onClick={() => setActiveTab("overview")}
        >
          📊 Overview
        </button>
        <button
          className={`tab-button ${activeTab === "workstations" ? "active" : ""}`}
          onClick={() => setActiveTab("workstations")}
        >
          🔧 Workstations ({workstations.length})
        </button>
        <button
          className={`tab-button ${activeTab === "orders" ? "active" : ""}`}
          onClick={() => setActiveTab("orders")}
        >
          📋 Orders ({totalOrders})
        </button>
        <button
          className={`tab-button ${activeTab === "supply" ? "active" : ""}`}
          onClick={() => setActiveTab("supply")}
        >
          📦 Supply ({supplyPendingCount})
        </button>
      </div>

      {/* Overview Tab */}
      {activeTab === "overview" && (
        <div className="tab-content">
          <h3>System Status Overview</h3>
          
          <div className="overview-grid">
            <div className="overview-section">
              <h4>Production Control Orders</h4>
              <div className="status-breakdown">
                {["PENDING", "SCHEDULED", "IN_PROGRESS", "COMPLETED"].map((status) => {
                  const count = orders.production.filter((o) => o.status === status).length;
                  return (
                    <div key={status} className="status-bar">
                      <span className="status-label">{status}</span>
                      <div className="status-bar-fill">
                        <div
                          className="status-bar-value"
                          style={{
                            width: totalOrders > 0 ? `${(count / totalOrders) * 100}%` : "0%",
                            backgroundColor: getStatusColor(status),
                          }}
                        >
                          {count}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="overview-section">
              <h4>Assembly Control Orders</h4>
              <div className="status-breakdown">
                {["PENDING", "SCHEDULED", "IN_PROGRESS", "COMPLETED"].map((status) => {
                  const count = orders.assembly.filter((o) => o.status === status).length;
                  return (
                    <div key={status} className="status-bar">
                      <span className="status-label">{status}</span>
                      <div className="status-bar-fill">
                        <div
                          className="status-bar-value"
                          style={{
                            width: totalOrders > 0 ? `${(count / totalOrders) * 100}%` : "0%",
                            backgroundColor: getStatusColor(status),
                          }}
                        >
                          {count}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>

          <div className="overview-section">
            <h4>Workstation Activity Summary</h4>
            <div className="workstation-mini-grid">
              {workstations.map((ws) => {
                const stat = workstationStats[ws.id];
                if (!stat) return null;
                return (
                  <div
                    key={ws.id}
                    className={`workstation-mini-card ${stat.status}`}
                    onClick={() => {
                      setSelectedWorkstation(ws.id);
                      setActiveTab("workstations");
                    }}
                  >
                    <div className="mini-name">{ws.name}</div>
                    <div className="mini-status" style={{ color: getStatusColor(stat.status) }}>
                      {stat.status.toUpperCase()}
                    </div>
                    <div className="mini-stats">
                      <span>🔄 {stat.activeOrders} active</span>
                      <span>✓ {stat.completedToday} done</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* Workstations Tab */}
      {activeTab === "workstations" && (
        <div className="tab-content">
          <h3>Workstation Status</h3>
          <div className="workstations-grid">
            {workstations.map((ws) => {
              const stat = workstationStats[ws.id];
              if (!stat) return null;
              return (
                <div
                  key={ws.id}
                  className={`workstation-card ${stat.status}`}
                  onClick={() => setSelectedWorkstation(ws.id)}
                >
                  <div className="ws-header">
                    <h4>{ws.name}</h4>
                    <span
                      className="ws-status-badge"
                      style={{ backgroundColor: getStatusColor(stat.status) }}
                    >
                      {stat.status.toUpperCase()}
                    </span>
                  </div>
                  <div className="ws-details">
                    <div className="ws-detail-row">
                      <span className="label">Type:</span>
                      <span className="value">{stat.type}</span>
                    </div>
                    <div className="ws-detail-row">
                      <span className="label">Active Orders:</span>
                      <span className="value highlight">{stat.activeOrders}</span>
                    </div>
                    <div className="ws-detail-row">
                      <span className="label">Completed Today:</span>
                      <span className="value">{stat.completedToday}</span>
                    </div>
                    <div className="ws-detail-row">
                      <span className="label">Total Orders:</span>
                      <span className="value">{stat.totalOrders}</span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {selectedWorkstation && (
            <div className="selected-workstation-detail">
              <button
                className="close-button"
                onClick={() => setSelectedWorkstation(null)}
              >
                ✕
              </button>
              <h4>Workstation {selectedWorkstation} - Assigned Orders</h4>
              <div className="orders-list">
                {orders.production
                  .filter((o) => o.assignedWorkstationId === selectedWorkstation)
                  .map((order) => (
                    <div key={order.id} className="order-item">
                      <div className="order-header">
                        <span className="order-number">PO-{order.id}</span>
                        <span
                          className="order-status"
                          style={{ backgroundColor: getStatusColor(order.status) }}
                        >
                          {order.status}
                        </span>
                        <span
                          className="order-priority"
                          style={{ backgroundColor: getPriorityColor(order.priority) }}
                        >
                          {order.priority}
                        </span>
                      </div>
                      <div className="order-details">
                        <span>{order.productName || "Unknown Product"}</span>
                        <span className="order-quantity">Qty: {order.quantity}</span>
                      </div>
                    </div>
                  ))}
                {orders.assembly
                  .filter((o) => o.assignedWorkstationId === selectedWorkstation)
                  .map((order) => (
                    <div key={order.id} className="order-item">
                      <div className="order-header">
                        <span className="order-number">AO-{order.id}</span>
                        <span
                          className="order-status"
                          style={{ backgroundColor: getStatusColor(order.status) }}
                        >
                          {order.status}
                        </span>
                        <span
                          className="order-priority"
                          style={{ backgroundColor: getPriorityColor(order.priority) }}
                        >
                          {order.priority}
                        </span>
                      </div>
                      <div className="order-details">
                        <span>{order.productName || "Unknown Product"}</span>
                        <span className="order-quantity">Qty: {order.quantity}</span>
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Orders Tab */}
      {activeTab === "orders" && (
        <div className="tab-content">
          <h3>All Production & Assembly Orders</h3>
          
          <div className="orders-section">
            <h4>Production Control Orders ({orders.production.length})</h4>
            <div className="orders-table">
              <div className="table-header">
                <div className="col-id">Order ID</div>
                <div className="col-product">Product</div>
                <div className="col-workstation">Workstation</div>
                <div className="col-status">Status</div>
                <div className="col-priority">Priority</div>
                <div className="col-quantity">Qty</div>
              </div>
              <div className="table-body">
                {orders.production.map((order) => (
                  <div key={order.id} className="table-row">
                    <div className="col-id">PO-{order.id}</div>
                    <div className="col-product">{order.productName || "Unknown"}</div>
                    <div className="col-workstation">
                      WS-{order.assignedWorkstationId || "—"}
                    </div>
                    <div className="col-status">
                      <span
                        className="badge"
                        style={{ backgroundColor: getStatusColor(order.status) }}
                      >
                        {order.status}
                      </span>
                    </div>
                    <div className="col-priority">
                      <span
                        className="badge"
                        style={{ backgroundColor: getPriorityColor(order.priority) }}
                      >
                        {order.priority}
                      </span>
                    </div>
                    <div className="col-quantity">{order.quantity}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="orders-section">
            <h4>Assembly Control Orders ({orders.assembly.length})</h4>
            <div className="orders-table">
              <div className="table-header">
                <div className="col-id">Order ID</div>
                <div className="col-product">Product</div>
                <div className="col-workstation">Workstation</div>
                <div className="col-status">Status</div>
                <div className="col-priority">Priority</div>
                <div className="col-quantity">Qty</div>
              </div>
              <div className="table-body">
                {orders.assembly.map((order) => (
                  <div key={order.id} className="table-row">
                    <div className="col-id">AO-{order.id}</div>
                    <div className="col-product">{order.productName || "Unknown"}</div>
                    <div className="col-workstation">
                      WS-{order.assignedWorkstationId || "—"}
                    </div>
                    <div className="col-status">
                      <span
                        className="badge"
                        style={{ backgroundColor: getStatusColor(order.status) }}
                      >
                        {order.status}
                      </span>
                    </div>
                    <div className="col-priority">
                      <span
                        className="badge"
                        style={{ backgroundColor: getPriorityColor(order.priority) }}
                      >
                        {order.priority}
                      </span>
                    </div>
                    <div className="col-quantity">{order.quantity}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Supply Tab */}
      {activeTab === "supply" && (
        <div className="tab-content">
          <h3>Supply Orders</h3>
          <div className="supply-grid">
            {orders.supply.length === 0 ? (
              <div className="empty-state">No supply orders</div>
            ) : (
              orders.supply.map((order) => (
                <div key={order.id} className="supply-card">
                  <div className="supply-header">
                    <span className="supply-number">SO-{order.supplyOrderNumber}</span>
                    <span
                      className="supply-status"
                      style={{ backgroundColor: getStatusColor(order.status) }}
                    >
                      {order.status}
                    </span>
                  </div>
                  <div className="supply-details">
                    <div className="detail-row">
                      <span className="label">From:</span>
                      <span className="value">
                        WS-{order.requestingWorkstationId}
                      </span>
                    </div>
                    <div className="detail-row">
                      <span className="label">To:</span>
                      <span className="value">
                        WS-{order.supplyWarehouseWorkstationId}
                      </span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Priority:</span>
                      <span
                        className="badge"
                        style={{
                          backgroundColor: getPriorityColor(order.priority),
                        }}
                      >
                        {order.priority}
                      </span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Parts:</span>
                      <span className="value">{order.supplyOrderItems?.length || 0}</span>
                    </div>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      <div className="dashboard-footer">
        Last updated: {new Date().toLocaleTimeString()} | Auto-refresh: 15s
      </div>
    </section>
  );
}

export default AdminDashboardPage;
