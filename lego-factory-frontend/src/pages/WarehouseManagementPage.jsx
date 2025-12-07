import { useState, useEffect } from "react";
import axios from "axios";
import { WORKSTATIONS_ENDPOINT } from "../api/apiConfig";
import { useAuth } from "../context/AuthContext.jsx";

function WarehouseManagementPage() {
  const { session, isAdmin, logout } = useAuth();
  const [warehouses, setWarehouses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState({ type: "", message: "" });
  const [selectedWarehouse, setSelectedWarehouse] = useState(null);

  const authToken = session?.token ?? null;

  // Load warehouses on mount
  useEffect(() => {
    if (isAdmin && authToken) {
      loadWarehouses();
    } else {
      setLoading(false);
    }
  }, [isAdmin, authToken]);

  const loadWarehouses = async () => {
    try {
      const response = await axios.get(WORKSTATIONS_ENDPOINT, {
        headers: { Authorization: `Bearer ${authToken}` },
      });
      
      // Filter to only warehouse type workstations
      const warehouseList = response.data.filter(
        (ws) => ws.workstationType === "WAREHOUSE"
      );
      setWarehouses(warehouseList);
    } catch (error) {
      console.error("Failed to load warehouses:", error);
      if (error.response?.status === 401) {
        logout();
      }
      setFeedback({
        type: "error",
        message: "Failed to load warehouses. Please try again.",
      });
    } finally {
      setLoading(false);
    }
  };

  const handleWarehouseClick = (warehouse) => {
    setSelectedWarehouse(warehouse);
  };

  const closeModal = () => {
    setSelectedWarehouse(null);
  };

  if (!session) {
    return (
      <section className="form-section">
        <h2>Administrator sign-in required</h2>
        <p className="form-helper">Log in with an administrator account to manage warehouses.</p>
      </section>
    );
  }

  if (!isAdmin) {
    return (
      <section className="form-section">
        <h2>Insufficient permissions</h2>
        <p className="form-helper">
          You are signed in as <strong>{session.user?.username}</strong>, but only administrators can view
          warehouse management.
        </p>
      </section>
    );
  }

  return (
    <div className="user-management-landscape">
      <section className="form-section">
        <h2>🏭 Warehouse Management</h2>
        <p className="form-helper">
          View and manage all warehouses and distribution centers in the LEGO factory system.
        </p>
        
        {feedback.message && (
          <p
            className={feedback.type === "error" ? "form-error" : "form-success"}
            role={feedback.type === "error" ? "alert" : "status"}
          >
            {feedback.message}
          </p>
        )}

        {loading ? (
          <p>Loading warehouses...</p>
        ) : warehouses.length === 0 ? (
          <p className="form-helper">No warehouses found in the system.</p>
        ) : (
          <>
            <div className="warehouses-grid">
              {warehouses.map((warehouse) => (
                <div 
                  key={warehouse.id} 
                  className="warehouse-card"
                  onClick={() => handleWarehouseClick(warehouse)}
                  style={{ cursor: "pointer" }}
                >
                  <div className="warehouse-header">
                    <h3>{warehouse.name}</h3>
                    <span className="warehouse-id">ID: {warehouse.id}</span>
                  </div>
                  
                  <div className="warehouse-details">
                    <div className="detail-row">
                      <span className="detail-label">Type:</span>
                      <span className="detail-value">{warehouse.workstationType}</span>
                    </div>
                    
                    <div className="detail-row">
                      <span className="detail-label">Description:</span>
                      <span className="detail-value">{warehouse.description || "N/A"}</span>
                    </div>
                    
                    <div className="detail-row">
                      <span className="detail-label">Status:</span>
                      <span className={`status-badge ${warehouse.active ? "active" : "inactive"}`}>
                        {warehouse.active ? "✓ Active" : "✗ Inactive"}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {selectedWarehouse && (
              <div className="modal-overlay" onClick={closeModal}>
                <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                  <div className="modal-header">
                    <h2>{selectedWarehouse.name}</h2>
                    <button className="modal-close" onClick={closeModal}>✕</button>
                  </div>
                  
                  <div className="modal-body">
                    <div className="detail-section">
                      <h3>Warehouse Information</h3>
                      
                      <div className="detail-group">
                        <label>ID:</label>
                        <span>{selectedWarehouse.id}</span>
                      </div>
                      
                      <div className="detail-group">
                        <label>Type:</label>
                        <span>{selectedWarehouse.workstationType}</span>
                      </div>
                      
                      <div className="detail-group">
                        <label>Name:</label>
                        <span>{selectedWarehouse.name}</span>
                      </div>
                      
                      <div className="detail-group">
                        <label>Description:</label>
                        <span>{selectedWarehouse.description || "N/A"}</span>
                      </div>
                      
                      <div className="detail-group">
                        <label>Status:</label>
                        <span className={`status-badge ${selectedWarehouse.active ? "active" : "inactive"}`}>
                          {selectedWarehouse.active ? "✓ Active" : "✗ Inactive"}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="modal-footer">
                    <button className="primary-link" onClick={closeModal}>Close</button>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </section>
    </div>
  );
}

export default WarehouseManagementPage;
