import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import axios from "axios";
import "../styles/ControlPages.css";

/**
 * PartsSupplyWarehousePage - Dashboard for Parts Supply Warehouse (Workstation 9)
 * Displays pending supply orders and allows fulfillment of parts requests
 */
function PartsSupplyWarehousePage() {
  const { session } = useAuth();
  const [supplyOrders, setSupplyOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [filter, setFilter] = useState("PENDING");
  const [notes, setNotes] = useState("");

  // Fetch supply orders for the warehouse
  const fetchSupplyOrders = async () => {
    setLoading(true);
    setError("");
    try {
      const response = await axios.get("/api/supply-orders/warehouse", {
        params: { status: filter || undefined },
      });
      setSupplyOrders(response.data);
      if (selectedOrder) {
        const updated = response.data.find((o) => o.id === selectedOrder.id);
        if (updated) {
          setSelectedOrder(updated);
        }
      }
    } catch (err) {
      setError("Failed to fetch supply orders: " + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Auto-refresh every 10 seconds
  useEffect(() => {
    fetchSupplyOrders();
    const interval = setInterval(fetchSupplyOrders, 10000);
    return () => clearInterval(interval);
  }, [filter]);

  // Fulfill a supply order
  const fulfillSupplyOrder = async (orderId) => {
    try {
      const response = await axios.put(`/api/supply-orders/${orderId}/fulfill`);
      setSuccessMessage(`✓ Supply Order ${response.data.supplyOrderNumber} fulfilled successfully`);
      setTimeout(() => setSuccessMessage(""), 3000);
      fetchSupplyOrders();
      setSelectedOrder(null);
    } catch (err) {
      setError("Failed to fulfill supply order: " + err.message);
    }
  };

  // Reject a supply order
  const rejectSupplyOrder = async (orderId, reason) => {
    if (!reason || reason.trim() === "") {
      setError("Please provide a reason for rejection");
      return;
    }
    try {
      const response = await axios.put(`/api/supply-orders/${orderId}/reject`, { reason });
      setSuccessMessage(`✗ Supply Order ${response.data.supplyOrderNumber} rejected`);
      setTimeout(() => setSuccessMessage(""), 3000);
      setNotes("");
      fetchSupplyOrders();
      setSelectedOrder(null);
    } catch (err) {
      setError("Failed to reject supply order: " + err.message);
    }
  };

  // Update order status to IN_PROGRESS
  const startFulfillment = async (orderId) => {
    try {
      const response = await axios.patch(`/api/supply-orders/${orderId}/status`, {
        status: "IN_PROGRESS",
      });
      setSuccessMessage(`Supply Order ${response.data.supplyOrderNumber} marked as in progress`);
      setTimeout(() => setSuccessMessage(""), 3000);
      fetchSupplyOrders();
    } catch (err) {
      setError("Failed to update order status: " + err.message);
    }
  };

  const getFilteredOrders = () => {
    if (!filter) return supplyOrders;
    return supplyOrders.filter((o) => o.status === filter);
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString() + " " + date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "PENDING":
        return "#FFC107"; // Yellow
      case "IN_PROGRESS":
        return "#00BCD4"; // Cyan
      case "FULFILLED":
        return "#4CAF50"; // Green
      case "REJECTED":
        return "#F44336"; // Red
      case "CANCELLED":
        return "#9E9E9E"; // Gray
      default:
        return "#757575";
    }
  };

  const filteredOrders = getFilteredOrders();

  return (
    <div className="control-page-container">
      {/* Header */}
      <div className="control-page-header">
        <div className="station-info">
          <h1>🏭 Parts Supply Warehouse</h1>
          <p>Workstation 9 - Fulfilling parts requests for production/assembly</p>
          <p>Operator: <strong>{session?.user?.username || "Unknown"}</strong></p>
        </div>
      </div>

      {/* Alert Messages */}
      {error && <div className="alert alert-error">{error}</div>}
      {successMessage && <div className="alert alert-success">{successMessage}</div>}

      {/* Filter Controls */}
      <div className="control-page-content">
        <div className="orders-section">
          <h2>Supply Orders</h2>
          <div className="filter-controls">
            {["PENDING", "IN_PROGRESS", "FULFILLED", "REJECTED"].map((status) => (
              <button
                key={status}
                className={`filter-btn ${filter === status ? "active" : ""}`}
                onClick={() => setFilter(status)}
              >
                {status === "PENDING" && "⏳ Pending"}
                {status === "IN_PROGRESS" && "🔄 In Progress"}
                {status === "FULFILLED" && "✓ Fulfilled"}
                {status === "REJECTED" && "✗ Rejected"}
                <span className="count">
                  ({supplyOrders.filter((o) => o.status === status).length})
                </span>
              </button>
            ))}
          </div>

          {/* Supply Orders Grid */}
          {loading && <p className="loading">Loading supply orders...</p>}
          {!loading && filteredOrders.length === 0 && (
            <p className="no-orders">No {filter.toLowerCase()} supply orders</p>
          )}

          <div className="orders-grid">
            {filteredOrders.map((order) => (
              <div
                key={order.id}
                className={`order-card ${selectedOrder?.id === order.id ? "selected" : ""}`}
                onClick={() => setSelectedOrder(order)}
              >
                <div className="order-header">
                  <span className="order-number">{order.supplyOrderNumber}</span>
                  <span className="status-badge" style={{ backgroundColor: getStatusColor(order.status) }}>
                    {order.status}
                  </span>
                </div>

                <div className="order-details">
                  <div className="detail-row">
                    <span className="label">Source Order:</span>
                    <span className="value">{order.sourceControlOrderType === "PRODUCTION" ? "🏭" : "⚙️"} {order.sourceControlOrderId}</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Requesting Workstation:</span>
                    <span className="value">Workstation {order.requestingWorkstationId}</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Priority:</span>
                    <span className="priority-badge" style={{ color: getPriorityColor(order.priority) }}>
                      {order.priority}
                    </span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Items:</span>
                    <span className="value">{order.supplyOrderItems?.length || 0} parts</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Needed By:</span>
                    <span className="value">{formatDateTime(order.requestedByTime)}</span>
                  </div>
                  <div className="detail-row">
                    <span className="label">Created:</span>
                    <span className="value">{formatDateTime(order.createdAt)}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Details Panel */}
        {selectedOrder && (
          <div className="details-panel">
            <h2>Supply Order Details</h2>

            {/* Order Information */}
            <div className="details-section">
              <div className="detail-group">
                <h3>Order Information</h3>
                <div className="detail-row">
                  <span className="label">Order Number:</span>
                  <span className="value">{selectedOrder.supplyOrderNumber}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Status:</span>
                  <span className="status-badge" style={{ backgroundColor: getStatusColor(selectedOrder.status) }}>
                    {selectedOrder.status}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="label">Priority:</span>
                  <span className="priority-badge" style={{ color: getPriorityColor(selectedOrder.priority) }}>
                    {selectedOrder.priority}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="label">Source Control Order:</span>
                  <span className="value">
                    {selectedOrder.sourceControlOrderType === "PRODUCTION" ? "🏭 Production" : "⚙️ Assembly"} #{selectedOrder.sourceControlOrderId}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="label">Requesting Workstation:</span>
                  <span className="value">Workstation {selectedOrder.requestingWorkstationId}</span>
                </div>
              </div>
            </div>

            {/* Parts List */}
            <div className="details-section">
              <div className="detail-group">
                <h3>Parts Needed</h3>
                <div className="items-box">
                  {selectedOrder.supplyOrderItems && selectedOrder.supplyOrderItems.length > 0 ? (
                    selectedOrder.supplyOrderItems.map((item, idx) => (
                      <div key={idx} className="item-entry">
                        <span className="item-label">Part ID: {item.partId}</span>
                        <span className="item-quantity">
                          Requested: <strong>{item.quantityRequested}</strong> {item.unit}
                          {item.quantitySupplied !== null && (
                            <>
                              <br />
                              Supplied: <strong>{item.quantitySupplied}</strong> {item.unit}
                            </>
                          )}
                        </span>
                        {item.notes && <span className="item-notes">Note: {item.notes}</span>}
                      </div>
                    ))
                  ) : (
                    <p className="no-items">No items in this supply order</p>
                  )}
                </div>
              </div>
            </div>

            {/* Notes */}
            {selectedOrder.notes && (
              <div className="details-section">
                <div className="detail-group">
                  <h3>Notes</h3>
                  <div className="notes-display">{selectedOrder.notes}</div>
                </div>
              </div>
            )}

            {/* Timestamps */}
            <div className="details-section">
              <div className="detail-group">
                <h3>Timestamps</h3>
                <div className="detail-row">
                  <span className="label">Created:</span>
                  <span className="value">{formatDateTime(selectedOrder.createdAt)}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Needed By:</span>
                  <span className="value">{formatDateTime(selectedOrder.requestedByTime)}</span>
                </div>
                {selectedOrder.fulfilledAt && (
                  <div className="detail-row">
                    <span className="label">Fulfilled:</span>
                    <span className="value">{formatDateTime(selectedOrder.fulfilledAt)}</span>
                  </div>
                )}
                {selectedOrder.rejectedAt && (
                  <div className="detail-row">
                    <span className="label">Rejected:</span>
                    <span className="value">{formatDateTime(selectedOrder.rejectedAt)}</span>
                  </div>
                )}
              </div>
            </div>

            {/* Action Buttons */}
            {selectedOrder.status === "PENDING" && (
              <div className="action-buttons-group">
                <button className="action-btn start-btn" onClick={() => startFulfillment(selectedOrder.id)}>
                  ▶️ Start Fulfillment
                </button>
              </div>
            )}

            {selectedOrder.status === "IN_PROGRESS" && (
              <div className="action-buttons-group">
                <button className="action-btn complete-btn" onClick={() => fulfillSupplyOrder(selectedOrder.id)}>
                  ✓ Mark Fulfilled
                </button>
                <button
                  className="action-btn defect-btn"
                  onClick={() => {
                    const reason = prompt("Enter reason for rejection:");
                    if (reason) rejectSupplyOrder(selectedOrder.id, reason);
                  }}
                >
                  ✗ Reject Order
                </button>
              </div>
            )}

            {["FULFILLED", "REJECTED", "CANCELLED"].includes(selectedOrder.status) && (
              <div className="action-buttons-group">
                <p style={{ textAlign: "center", color: "#999", margin: "0" }}>
                  This order is {selectedOrder.status.toLowerCase()}
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

function getPriorityColor(priority) {
  switch (priority) {
    case "HIGH":
      return "#F44336";
    case "URGENT":
      return "#D32F2F";
    case "MEDIUM":
      return "#FFC107";
    case "LOW":
      return "#4CAF50";
    default:
      return "#999";
  }
}

export default PartsSupplyWarehousePage;
