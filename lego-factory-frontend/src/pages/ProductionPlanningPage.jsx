import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import axios from "axios";
import "../styles/DashboardStandard.css";

function ProductionPlanningPage() {
  const { session } = useAuth();
  const [productionOrders, setProductionOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProductionOrders();
    // ENHANCED: Refresh every 5 seconds for real-time updates
    const interval = setInterval(fetchProductionOrders, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchProductionOrders = async () => {
    setLoading(true);
    setError(null);

    try {
      // Fetch production control orders from the API
      const response = await axios.get("/api/production-control-orders");
      setProductionOrders(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      setError(
        "Failed to load production orders: " +
          (err.response?.data?.message || err.message)
      );
      console.error("Production orders fetch error:", err);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      PENDING: "#fbbf24",
      SCHEDULED: "#60a5fa",
      IN_PROGRESS: "#10b981",
      COMPLETED: "#6366f1",
      CANCELLED: "#ef4444",
    };
    return colors[status] || "#9ca3af";
  };

  const getPriorityColor = (priority) => {
    const colors = {
      LOW: "#6366f1",
      MEDIUM: "#f59e0b",
      HIGH: "#ef4444",
    };
    return colors[priority] || "#9ca3af";
  };

  return (
    <section className="dashboard-page">
      <div className="page-header">
        <h2>📋 Production Planning</h2>
        <p>Plan and schedule production workflows across manufacturing and assembly stations</p>
      </div>

      {error && (
        <div className="alert alert-error" style={{ marginBottom: "1rem" }}>
          {error}
        </div>
      )}

      <div className="dashboard-box">
        <div className="box-header box-header-primary">
          <h3>Production Control Orders ({productionOrders.length})</h3>
          <button
            onClick={fetchProductionOrders}
            disabled={loading}
            style={{
              padding: "0.5rem 1rem",
              backgroundColor: "#3b82f6",
              color: "white",
              border: "none",
              borderRadius: "0.375rem",
              cursor: loading ? "not-allowed" : "pointer",
              opacity: loading ? 0.6 : 1,
              fontSize: "0.875rem",
              fontWeight: "500",
            }}
          >
            {loading ? "⟳ Refreshing..." : "⟳ Refresh"}
          </button>
        </div>
        <div className="box-content">
          {loading ? (
            <p style={{ textAlign: "center", color: "#666" }}>Loading production orders...</p>
          ) : productionOrders.length === 0 ? (
            <div className="empty-state">
              <p>No production orders available.</p>
              <p style={{ fontSize: "0.9rem", color: "#999" }}>
                Check back soon for incoming production requests.
              </p>
            </div>
          ) : (
            <div className="dashboard-table">
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr style={{ borderBottom: "2px solid #e5e7eb", backgroundColor: "#f9fafb" }}>
                    <th style={{ textAlign: "left", padding: "0.75rem", fontWeight: "600", color: "#374151" }}>Order ID</th>
                    <th style={{ textAlign: "left", padding: "0.75rem", fontWeight: "600", color: "#374151" }}>Items</th>
                    <th style={{ textAlign: "left", padding: "0.75rem", fontWeight: "600", color: "#374151" }}>Workstation</th>
                    <th style={{ textAlign: "left", padding: "0.75rem", fontWeight: "600", color: "#374151" }}>Status</th>
                    <th style={{ textAlign: "left", padding: "0.75rem", fontWeight: "600", color: "#374151" }}>Priority</th>
                    <th style={{ textAlign: "center", padding: "0.75rem", fontWeight: "600", color: "#374151" }}>Qty</th>
                  </tr>
                </thead>
                <tbody>
                  {productionOrders.map((order) => (
                    <tr
                      key={order.id}
                      style={{
                        borderBottom: "1px solid #e5e7eb",
                        backgroundColor: order.status === "IN_PROGRESS" ? "#f0fdf4" : "white",
                      }}
                    >
                      <td style={{ padding: "0.75rem", fontWeight: "500" }}>PO-{order.id}</td>
                      <td style={{ padding: "0.75rem" }}>
                        {order.orderItems && order.orderItems.length > 0 ? (
                          <div style={{ display: "flex", flexDirection: "column", gap: "0.25rem" }}>
                            {order.orderItems.map((item, idx) => (
                              <span
                                key={idx}
                                style={{
                                  fontSize: "0.85rem",
                                  padding: "0.25rem 0.5rem",
                                  backgroundColor: "#f3f4f6",
                                  borderRadius: "0.25rem",
                                  display: "inline-block",
                                  width: "fit-content",
                                }}
                              >
                                <span style={{ fontWeight: "600" }}>{item.itemType || item.name || "Item"}</span>
                                <span style={{ color: "#999", margin: "0 0.25rem" }}>×</span>
                                <span style={{ color: "#059669", fontWeight: "600" }}>{item.quantity}</span>
                                {item.status && (
                                  <span style={{ color: "#666", fontSize: "0.75rem", marginLeft: "0.25rem" }}>
                                    ({item.status})
                                  </span>
                                )}
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span>{order.productName || "Unknown"}</span>
                        )}
                      </td>
                      <td style={{ padding: "0.75rem" }}>WS-{order.assignedWorkstationId || "—"}</td>
                      <td style={{ padding: "0.75rem" }}>
                        <span
                          style={{
                            display: "inline-block",
                            padding: "0.25rem 0.75rem",
                            backgroundColor: getStatusColor(order.status),
                            color: "white",
                            borderRadius: "0.375rem",
                            fontSize: "0.875rem",
                            fontWeight: "500",
                          }}
                        >
                          {order.status}
                        </span>
                      </td>
                      <td style={{ padding: "0.75rem" }}>
                        <span
                          style={{
                            display: "inline-block",
                            padding: "0.25rem 0.75rem",
                            backgroundColor: getPriorityColor(order.priority),
                            color: "white",
                            borderRadius: "0.375rem",
                            fontSize: "0.875rem",
                            fontWeight: "500",
                          }}
                        >
                          {order.priority}
                        </span>
                      </td>
                      <td style={{ padding: "0.75rem", textAlign: "center" }}>{order.quantity}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </section>
  );
}

export default ProductionPlanningPage;
