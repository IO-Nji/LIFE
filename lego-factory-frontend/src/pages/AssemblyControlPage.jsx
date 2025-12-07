import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import axios from "axios";
import "../styles/DashboardStandard.css";
import "../styles/ControlPages.css";

function AssemblyControlPage() {
  const { session } = useAuth();
  const [controlOrders, setControlOrders] = useState([]);
  const [activeOrders, setActiveOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [operatorNotes, setOperatorNotes] = useState("");
  const [defects, setDefects] = useState("");
  const [shippingNotes, setShippingNotes] = useState("");
  const [filter, setFilter] = useState("all"); // all, active, unassigned

  const workstationId = session?.user?.workstationId;

  useEffect(() => {
    if (workstationId) {
      fetchControlOrders();
      // Refresh every 10 seconds
      const interval = setInterval(fetchControlOrders, 10000);
      return () => clearInterval(interval);
    }
  }, [workstationId]);

  const fetchControlOrders = async () => {
    if (!workstationId) return;
    setLoading(true);
    setError(null);

    try {
      const response = await axios.get(
        `/api/assembly-control-orders/workstation/${workstationId}`
      );
      const orders = Array.isArray(response.data) ? response.data : [];
      setControlOrders(orders);

      // Get active orders
      const active = orders.filter((o) => o.status === "IN_PROGRESS");
      setActiveOrders(active);

      if (orders.length === 0) {
        setSuccessMessage("No assembly orders assigned yet");
      }
    } catch (err) {
      setError(
        "Failed to load assembly orders: " +
          (err.response?.data?.message || err.message)
      );
    } finally {
      setLoading(false);
    }
  };

  const startAssembly = async (orderId) => {
    try {
      setLoading(true);
      const response = await axios.put(
        `/api/assembly-control-orders/${orderId}/start`,
        { operatorId: session?.user?.id || "UNKNOWN" }
      );
      setSelectedOrder(response.data);
      await fetchControlOrders();
      setSuccessMessage("Assembly started successfully!");
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError("Failed to start assembly: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const updateNotes = async (orderId) => {
    if (!operatorNotes.trim()) {
      setError("Please enter notes before updating");
      return;
    }

    try {
      setLoading(true);
      const response = await axios.patch(
        `/api/assembly-control-orders/${orderId}/status`,
        { notes: operatorNotes }
      );
      setSelectedOrder(response.data);
      setOperatorNotes("");
      setSuccessMessage("Notes updated successfully!");
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError("Failed to update notes: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const recordDefects = async (orderId) => {
    if (!defects.trim()) {
      setError("Please describe the defects found");
      return;
    }

    try {
      setLoading(true);
      const response = await axios.patch(
        `/api/assembly-control-orders/${orderId}/defects`,
        { defects: [{ description: defects, severity: "MEDIUM", requiresRework: true }] }
      );
      setSelectedOrder(response.data);
      setDefects("");
      setSuccessMessage("Defects recorded. Item marked for rework.");
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError("Failed to record defects: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const completeAssembly = async (orderId) => {
    if (
      !window.confirm(
        "Are you sure assembly is complete? This action cannot be undone."
      )
    ) {
      return;
    }

    try {
      setLoading(true);
      const response = await axios.put(
        `/api/assembly-control-orders/${orderId}/complete`,
        { completionTime: new Date().toISOString() }
      );
      setSelectedOrder(response.data);
      await fetchControlOrders();
      setSuccessMessage("Assembly completed! Ready for shipping.");
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError("Failed to complete assembly: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const addShippingNotes = async (orderId) => {
    if (!shippingNotes.trim()) {
      setError("Please enter shipping notes");
      return;
    }

    try {
      setLoading(true);
      const response = await axios.put(
        `/api/assembly-control-orders/${orderId}/shipping-notes`,
        { shippingNotes }
      );
      setSelectedOrder(response.data);
      setShippingNotes("");
      setSuccessMessage("Shipping notes added successfully!");
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError(
        "Failed to add shipping notes: " + (err.response?.data?.message || err.message)
      );
    } finally {
      setLoading(false);
    }
  };

  const getFilteredOrders = () => {
    switch (filter) {
      case "active":
        return controlOrders.filter((o) => o.status === "IN_PROGRESS");
      case "unassigned":
        return controlOrders.filter((o) => o.status === "ASSIGNED");
      default:
        return controlOrders;
    }
  };

  const formatDateTime = (dateTime) => {
    if (!dateTime) return "N/A";
    const date = new Date(dateTime);
    return date.toLocaleString();
  };

  const getStatusColor = (status) => {
    switch (status) {
      case "ASSIGNED":
        return "#ffc107"; // yellow
      case "IN_PROGRESS":
        return "#17a2b8"; // cyan
      case "COMPLETED":
        return "#28a745"; // green
      case "REWORK":
        return "#fd7e14"; // orange
      case "CANCELLED":
        return "#dc3545"; // red
      default:
        return "#6c757d"; // gray
    }
  };

  const filteredOrders = getFilteredOrders();

  return (
    <div className="control-page-container">
      <div className="control-page-header">
        <h1>⚙️ Assembly Control Station</h1>
        <p className="station-info">
          Workstation ID: <strong>{workstationId || "Not assigned"}</strong> |
          Operator: <strong>{session?.user?.username || "Unknown"}</strong>
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {successMessage && <div className="alert alert-success">{successMessage}</div>}

      <div className="control-page-content">
        <div className="orders-section">
          <div className="section-header">
            <h2>📦 Assembly Control Orders</h2>
            <div className="filter-controls">
              <button
                className={`filter-btn ${filter === "all" ? "active" : ""}`}
                onClick={() => setFilter("all")}
              >
                All ({controlOrders.length})
              </button>
              <button
                className={`filter-btn ${filter === "unassigned" ? "active" : ""}`}
                onClick={() => setFilter("unassigned")}
              >
                Ready ({controlOrders.filter((o) => o.status === "ASSIGNED").length})
              </button>
              <button
                className={`filter-btn ${filter === "active" ? "active" : ""}`}
                onClick={() => setFilter("active")}
              >
                In Progress ({activeOrders.length})
              </button>
              <button
                className="refresh-btn"
                onClick={fetchControlOrders}
                disabled={loading}
              >
                {loading ? "⟳ Refreshing..." : "⟳ Refresh"}
              </button>
            </div>
          </div>

          {filteredOrders.length === 0 ? (
            <div className="empty-state">
              <p>
                {filter === "all"
                  ? "No assembly orders assigned to this workstation"
                  : `No ${filter} orders available`}
              </p>
            </div>
          ) : (
            <div className="orders-grid">
              {filteredOrders.map((order) => (
                <div
                  key={order.id}
                  className={`order-card ${
                    selectedOrder?.id === order.id ? "selected" : ""
                  }`}
                  onClick={() => setSelectedOrder(order)}
                >
                  <div className="order-header">
                    <h3>{order.controlOrderNumber}</h3>
                    <span
                      className="status-badge"
                      style={{ backgroundColor: getStatusColor(order.status) }}
                    >
                      {order.status}
                    </span>
                  </div>

                  <div className="order-details">
                    <div className="detail-row">
                      <span className="label">Source Order:</span>
                      <span className="value">PO-{order.sourceProductionOrderId}</span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Priority:</span>
                      <span className="priority-badge" style={{
                        backgroundColor:
                          order.priority === "HIGH"
                            ? "#dc3545"
                            : order.priority === "MEDIUM"
                            ? "#ffc107"
                            : "#28a745",
                      }}>
                        {order.priority}
                      </span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Target Start:</span>
                      <span className="value">
                        {formatDateTime(order.targetStartTime)}
                      </span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Target End:</span>
                      <span className="value">
                        {formatDateTime(order.targetCompletionTime)}
                      </span>
                    </div>
                  </div>

                  {order.status === "ASSIGNED" && (
                    <button
                      className="action-btn start-btn"
                      onClick={(e) => {
                        e.stopPropagation();
                        startAssembly(order.id);
                      }}
                      disabled={loading}
                    >
                      ▶ Start Assembly
                    </button>
                  )}

                  {order.status === "IN_PROGRESS" && (
                    <div className="action-buttons-group">
                      <button
                        className="action-btn complete-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          completeAssembly(order.id);
                        }}
                        disabled={loading}
                      >
                        ✓ Complete
                      </button>
                      <button
                        className="action-btn defect-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          // Scroll to defects section
                          document
                            .querySelector(".details-section")
                            ?.scrollIntoView({ behavior: "smooth" });
                        }}
                      >
                        ⚠ Report Defect
                      </button>
                    </div>
                  )}

                  {(order.status === "REWORK" || order.status === "COMPLETED") && (
                    <div className="action-buttons-group">
                      <button
                        className="action-btn shipping-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          document
                            .querySelector(".details-section")
                            ?.scrollIntoView({ behavior: "smooth" });
                        }}
                      >
                        📮 Add Shipping Notes
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {selectedOrder && (
          <div className="details-section">
            <h2>📝 Order Details</h2>
            <div className="details-panel">
              <div className="detail-group">
                <h3>Assembly Order Information</h3>
                <div className="detail-row">
                  <span className="label">Order Number:</span>
                  <span className="value">{selectedOrder.controlOrderNumber}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Status:</span>
                  <span
                    className="status-badge"
                    style={{
                      backgroundColor: getStatusColor(selectedOrder.status),
                    }}
                  >
                    {selectedOrder.status}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="label">Priority:</span>
                  <span
                    className="priority-badge"
                    style={{
                      backgroundColor:
                        selectedOrder.priority === "HIGH"
                          ? "#dc3545"
                          : selectedOrder.priority === "MEDIUM"
                          ? "#ffc107"
                          : "#28a745",
                    }}
                  >
                    {selectedOrder.priority}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="label">Source Production Order:</span>
                  <span className="value">PO-{selectedOrder.sourceProductionOrderId}</span>
                </div>
              </div>

              <div className="detail-group">
                <h3>Timeline</h3>
                <div className="detail-row">
                  <span className="label">Target Start:</span>
                  <span className="value">
                    {formatDateTime(selectedOrder.targetStartTime)}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="label">Target Completion:</span>
                  <span className="value">
                    {formatDateTime(selectedOrder.targetCompletionTime)}
                  </span>
                </div>
                {selectedOrder.actualStartTime && (
                  <div className="detail-row">
                    <span className="label">Actual Start:</span>
                    <span className="value">
                      {formatDateTime(selectedOrder.actualStartTime)}
                    </span>
                  </div>
                )}
                {selectedOrder.actualCompletionTime && (
                  <div className="detail-row">
                    <span className="label">Actual Completion:</span>
                    <span className="value">
                      {formatDateTime(selectedOrder.actualCompletionTime)}
                    </span>
                  </div>
                )}
              </div>

              {selectedOrder.assemblyInstructions && (
                <div className="detail-group">
                  <h3>📋 Assembly Instructions</h3>
                  <div className="instructions-box">
                    {selectedOrder.assemblyInstructions}
                  </div>
                </div>
              )}

              {selectedOrder.qualityCheckpoints && (
                <div className="detail-group">
                  <h3>✓ Quality Standards</h3>
                  <div className="checkpoints-box">
                    {selectedOrder.qualityCheckpoints}
                  </div>
                </div>
              )}

              {selectedOrder.status === "IN_PROGRESS" && (
                <>
                  <div className="detail-group">
                    <h3>📌 Operator Notes</h3>
                    <textarea
                      className="notes-input"
                      placeholder="Enter progress notes, issues, or observations..."
                      value={operatorNotes}
                      onChange={(e) => setOperatorNotes(e.target.value)}
                      rows="3"
                    />
                    <button
                      className="action-btn update-btn"
                      onClick={() => updateNotes(selectedOrder.id)}
                      disabled={loading || !operatorNotes.trim()}
                    >
                      💾 Update Notes
                    </button>
                  </div>

                  <div className="detail-group">
                    <h3>⚠️ Report Defects</h3>
                    <textarea
                      className="notes-input"
                      placeholder="Describe any defects found during assembly..."
                      value={defects}
                      onChange={(e) => setDefects(e.target.value)}
                      rows="3"
                    />
                    <button
                      className="action-btn defect-btn"
                      onClick={() => recordDefects(selectedOrder.id)}
                      disabled={loading || !defects.trim()}
                    >
                      📋 Record Defects
                    </button>
                  </div>
                </>
              )}

              {(selectedOrder.status === "REWORK" || selectedOrder.status === "COMPLETED") && (
                <div className="detail-group">
                  <h3>📮 Shipping Notes</h3>
                  <textarea
                    className="notes-input"
                    placeholder="Enter shipping instructions, packaging requirements, etc..."
                    value={shippingNotes}
                    onChange={(e) => setShippingNotes(e.target.value)}
                    rows="3"
                  />
                  <button
                    className="action-btn shipping-btn"
                    onClick={() => addShippingNotes(selectedOrder.id)}
                    disabled={loading || !shippingNotes.trim()}
                  >
                    💾 Save Shipping Notes
                  </button>
                </div>
              )}

              {selectedOrder.notes && (
                <div className="detail-group">
                  <h3>📝 Previous Notes</h3>
                  <div className="notes-display">
                    {selectedOrder.notes}
                  </div>
                </div>
              )}

              {selectedOrder.shippingNotes && (
                <div className="detail-group">
                  <h3>📦 Shipping Instructions</h3>
                  <div className="notes-display">
                    {selectedOrder.shippingNotes}
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default AssemblyControlPage;
