import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import axios from "axios";
import "../styles/DashboardStandard.css";
import "../styles/ControlPages.css";

/**
 * Generic Workstation Page - Used for both Manufacturing and Assembly
 * Manufacturing: Injection Molding, Parts Pre-Production, Part Finishing
 * Assembly: Gear Assembly, Motor Assembly, Final Assembly
 * Displays tasks and allows operators to complete them.
 */
function ManufacturingWorkstationPage() {
  const { workstationType } = useParams(); // injection-molding, parts-pre-production, part-finishing, gear-assembly, motor-assembly, final-assembly
  const { session } = useAuth();
  const [controlOrders, setControlOrders] = useState([]);
  const [activeOrders, setActiveOrders] = useState([]);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [operatorNotes, setOperatorNotes] = useState("");
  const [filter, setFilter] = useState("all"); // all, active, scheduled

  const workstationId = session?.user?.workstationId;

  // Map manufacturing workstation types to API endpoints
  const manufacturingEndpoints = {
    "injection-molding": "/api/manufacturing/injection-molding",
    "parts-pre-production": "/api/manufacturing/parts-pre-production",
    "part-finishing": "/api/manufacturing/part-finishing",
  };

  // Map assembly workstation types to API endpoints
  const assemblyEndpoints = {
    "gear-assembly": "/api/assembly/gear-assembly",
    "motor-assembly": "/api/assembly/motor-assembly",
    "final-assembly": "/api/assembly/final-assembly",
  };

  // Combine all endpoints
  const apiEndpointMap = { ...manufacturingEndpoints, ...assemblyEndpoints };

  // Manufacturing station titles
  const manufacturingTitles = {
    "injection-molding": "🏭 Injection Molding Station",
    "parts-pre-production": "⚙️ Parts Pre-Production Station",
    "part-finishing": "✨ Part Finishing Station",
  };

  // Assembly station titles
  const assemblyTitles = {
    "gear-assembly": "⚙️ Gear Assembly Station",
    "motor-assembly": "🔌 Motor Assembly Station",
    "final-assembly": "📦 Final Assembly Station",
  };

  // Combine all titles
  const stationTitles = { ...manufacturingTitles, ...assemblyTitles };

  const apiEndpoint = apiEndpointMap[workstationType] || "/api/manufacturing/injection-molding";
  const stationTitle = stationTitles[workstationType] || "Workstation";
  const isAssembly = Object.keys(assemblyTitles).includes(workstationType);
  const taskType = isAssembly ? "assembly" : "manufacturing";

  useEffect(() => {
    if (workstationId) {
      fetchControlOrders();
      // Refresh every 10 seconds
      const interval = setInterval(fetchControlOrders, 10000);
      return () => clearInterval(interval);
    }
  }, [workstationId, workstationType]);

  const fetchControlOrders = async () => {
    if (!workstationId) return;
    setLoading(true);
    setError(null);

    try {
      const response = await axios.get(
        `${apiEndpoint}/workstation/${workstationId}`
      );
      const orders = Array.isArray(response.data) ? response.data : [];
      setControlOrders(orders);

      // Get active orders
      const active = orders.filter((o) => o.status === "IN_PROGRESS");
      setActiveOrders(active);

      if (orders.length === 0) {
        setSuccessMessage(`No ${taskType} tasks assigned yet`);
      }
    } catch (err) {
      setError(
        `Failed to load ${taskType} tasks: ` +
          (err.response?.data?.message || err.message)
      );
    } finally {
      setLoading(false);
    }
  };

  const startProduction = async (orderId) => {
    try {
      setLoading(true);
      const response = await axios.post(
        `${apiEndpoint}/${orderId}/start`,
        { operatorId: session?.user?.id || "UNKNOWN" }
      );
      setSelectedOrder(response.data);
      await fetchControlOrders();
      setSuccessMessage(`${taskType.charAt(0).toUpperCase() + taskType.slice(1)} task started successfully!`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError(`Failed to start ${taskType}: ` + (err.response?.data?.message || err.message));
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
        `${apiEndpoint}/${orderId}/notes`,
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

  const completeProduction = async (orderId) => {
    const message = isAssembly 
      ? "Are you sure the assembly task is complete and quality checks have passed?"
      : "Are you sure the manufacturing task is complete and quality checks have passed?";
    
    if (!window.confirm(message)) {
      return;
    }

    try {
      setLoading(true);
      const response = await axios.put(
        `${apiEndpoint}/${orderId}/complete`,
        {}
      );
      setSelectedOrder(null);
      setOperatorNotes("");
      await fetchControlOrders();
      const creditMsg = isAssembly && workstationType === "final-assembly"
        ? "Plant Warehouse has been credited with a finished product."
        : "Modules Supermarket has been credited with a module unit.";
      setSuccessMessage(`${taskType.charAt(0).toUpperCase() + taskType.slice(1)} task completed successfully! ${creditMsg}`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError(
        `Failed to complete ${taskType} task: ` +
          (err.response?.data?.message || err.message)
      );
    } finally {
      setLoading(false);
    }
  };

  const haltProduction = async (orderId) => {
    const reason = prompt(`Enter reason for halting ${taskType}:`);
    if (!reason) return;

    try {
      setLoading(true);
      const response = await axios.post(
        `${apiEndpoint}/${orderId}/halt`,
        { reason: reason }
      );
      setSelectedOrder(null);
      setOperatorNotes("");
      await fetchControlOrders();
      setError(null);
      setSuccessMessage(`${taskType.charAt(0).toUpperCase() + taskType.slice(1)} task halted and logged`);
      setTimeout(() => setSuccessMessage(null), 3000);
    } catch (err) {
      setError(
        `Failed to halt ${taskType}: ` +
          (err.response?.data?.message || err.message)
      );
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    const colors = {
      ASSIGNED: "#FFA500", // Orange
      IN_PROGRESS: "#4CAF50", // Green
      COMPLETED: "#2196F3", // Blue
      HALTED: "#F44336", // Red
      ABANDONED: "#9C27B0", // Purple
    };
    return colors[status] || "#757575";
  };

  const getPriorityColor = (priority) => {
    const colors = {
      LOW: "#81C784",
      MEDIUM: "#FFB74D",
      HIGH: "#EF5350",
      URGENT: "#C62828",
    };
    return colors[priority] || "#757575";
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return "Not started";
    const date = new Date(dateString);
    return date.toLocaleString();
  };

  const getFilteredOrders = () => {
    switch (filter) {
      case "active":
        return activeOrders;
      case "scheduled":
        return controlOrders.filter((o) => o.status === "ASSIGNED");
      default:
        return controlOrders;
    }
  };

  const filteredOrders = getFilteredOrders();

  return (
    <div className="control-page">
      <h1>{stationTitle}</h1>

      {error && (
        <div className="error-message">
          <span>{error}</span>
          <button onClick={() => setError(null)}>✕</button>
        </div>
      )}

      {successMessage && (
        <div className="success-message">
          <span>{successMessage}</span>
          <button onClick={() => setSuccessMessage(null)}>✕</button>
        </div>
      )}

      <div className="controls-section">
        <div className="filters">
          <label>
            <input
              type="radio"
              value="all"
              checked={filter === "all"}
              onChange={(e) => setFilter(e.target.value)}
            />
            All Tasks ({controlOrders.length})
          </label>
          <label>
            <input
              type="radio"
              value="active"
              checked={filter === "active"}
              onChange={(e) => setFilter(e.target.value)}
            />
            Active ({activeOrders.length})
          </label>
          <label>
            <input
              type="radio"
              value="scheduled"
              checked={filter === "scheduled"}
              onChange={(e) => setFilter(e.target.value)}
            />
            Scheduled ({controlOrders.filter((o) => o.status === "ASSIGNED").length})
          </label>
        </div>

        <button onClick={fetchControlOrders} disabled={loading} className="refresh-btn">
          🔄 Refresh
        </button>
      </div>

      <div className="main-content">
        <div className="orders-list">
          <h2>📋 Manufacturing Tasks</h2>
          {loading && !selectedOrder ? (
            <div className="loading">Loading manufacturing tasks...</div>
          ) : filteredOrders.length === 0 ? (
            <div className="no-orders">No manufacturing tasks in this filter</div>
          ) : (
            filteredOrders.map((order) => (
              <div
                key={order.id}
                className={`order-card ${selectedOrder?.id === order.id ? "selected" : ""}`}
                onClick={() => {
                  setSelectedOrder(order);
                  setOperatorNotes(order.operatorNotes || "");
                }}
              >
                <div className="order-header">
                  <span className="order-number">{order.controlOrderNumber}</span>
                  <span
                    className="status-badge"
                    style={{ backgroundColor: getStatusColor(order.status) }}
                  >
                    {order.status}
                  </span>
                  <span
                    className="priority-badge"
                    style={{ color: getPriorityColor(order.priority) }}
                  >
                    {order.priority}
                  </span>
                </div>
                <div className="order-details">
                  <span>From Production Order: {order.sourceProductionOrderId}</span>
                  <span>Target: {formatDateTime(order.targetCompletionTime)}</span>
                </div>
              </div>
            ))
          )}
        </div>

        <div className="order-details-panel">
          {selectedOrder ? (
            <div className="details-content">
              <h2>📌 Manufacturing Task Details</h2>

              <div className="detail-group">
                <h3>Task Information</h3>
                <div className="detail-row">
                  <span className="label">Task Number:</span>
                  <span className="value">{selectedOrder.controlOrderNumber}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Production Order ID:</span>
                  <span className="value">{selectedOrder.sourceProductionOrderId}</span>
                </div>
                <div className="detail-row">
                  <span className="label">Status:</span>
                  <span
                    className="status-badge"
                    style={{ backgroundColor: getStatusColor(selectedOrder.status) }}
                  >
                    {selectedOrder.status}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="label">Priority:</span>
                  <span
                    className="priority-badge"
                    style={{ color: getPriorityColor(selectedOrder.priority) }}
                  >
                    {selectedOrder.priority}
                  </span>
                </div>
              </div>

              <div className="detail-group">
                <h3>⏱️ Timeline</h3>
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
                {selectedOrder.actualDurationMinutes && (
                  <div className="detail-row">
                    <span className="label">Duration:</span>
                    <span className="value">{selectedOrder.actualDurationMinutes} minutes</span>
                  </div>
                )}
              </div>

              {selectedOrder.productionInstructions && (
                <div className="detail-group">
                  <h3>📋 Manufacturing Instructions</h3>
                  <div className="instructions-box">
                    {selectedOrder.productionInstructions}
                  </div>
                </div>
              )}

              {selectedOrder.qualityCheckpoints && (
                <div className="detail-group">
                  <h3>✓ Quality Checkpoints</h3>
                  <div className="checkpoints-box">
                    {selectedOrder.qualityCheckpoints}
                  </div>
                </div>
              )}

              {selectedOrder.safetyProcedures && (
                <div className="detail-group">
                  <h3>⚠️ Safety Procedures</h3>
                  <div className="safety-box">
                    {selectedOrder.safetyProcedures}
                  </div>
                </div>
              )}

              {selectedOrder.status === "IN_PROGRESS" && (
                <>
                  <div className="detail-group">
                    <h3>📝 Operator Notes</h3>
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
                      disabled={loading}
                    >
                      💾 Update Notes
                    </button>
                  </div>

                  <div className="action-buttons">
                    <button
                      className="action-btn complete-btn"
                      onClick={() => completeProduction(selectedOrder.id)}
                      disabled={loading}
                    >
                      ✅ Complete Task & Award Modules
                    </button>
                    <button
                      className="action-btn halt-btn"
                      onClick={() => haltProduction(selectedOrder.id)}
                      disabled={loading}
                    >
                      ⏸️ Halt Task
                    </button>
                  </div>
                </>
              )}

              {selectedOrder.status === "ASSIGNED" && (
                <div className="action-buttons">
                  <button
                    className="action-btn start-btn"
                    onClick={() => startProduction(selectedOrder.id)}
                    disabled={loading}
                  >
                    ▶️ Start Task
                  </button>
                </div>
              )}

              {selectedOrder.status === "COMPLETED" && (
                <div className="completion-info">
                  <h3>✅ Task Completed</h3>
                  <p>This manufacturing task has been completed successfully.</p>
                  <p>Modules have been awarded to the Modules Supermarket.</p>
                </div>
              )}

              {selectedOrder.status === "HALTED" && (
                <div className="halt-info">
                  <h3>⏸️ Task Halted</h3>
                  <p>This manufacturing task has been halted.</p>
                  {selectedOrder.operatorNotes && (
                    <p>Reason: {selectedOrder.operatorNotes}</p>
                  )}
                </div>
              )}
            </div>
          ) : (
            <div className="no-selection">Select a manufacturing task to view details</div>
          )}
        </div>
      </div>
    </div>
  );
}

export default ManufacturingWorkstationPage;
