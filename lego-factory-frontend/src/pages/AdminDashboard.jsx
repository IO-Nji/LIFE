import { useState, useEffect } from "react";
import axios from "axios";
import { useAuth } from "../context/AuthContext";
import ErrorNotification from "../components/ErrorNotification";
import { getErrorMessage } from "../utils/errorHandler";

/**
 * Product name mapping
 * Maps itemId to product variant name
 */
const PRODUCT_NAMES = {
  1: { name: "Technic Truck Yellow" },
  2: { name: "Technic Truck Red" },
  3: { name: "Creator House" },
  4: { name: "Friends Cafe" },
};

function AdminDashboard() {
  const { session } = useAuth();
  const [workstations, setWorkstations] = useState([]);
  const [selectedWorkstationId, setSelectedWorkstationId] = useState(null);
  const [workstationInventory, setWorkstationInventory] = useState({});
  const [workstationOrders, setWorkstationOrders] = useState({});
  const [productVariants, setProductVariants] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState("overview");
  const [error, setError] = useState(null);
  const [notification, setNotification] = useState(null);
  const [editingProductId, setEditingProductId] = useState(null);
  const [showProductForm, setShowProductForm] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [selectedProductView, setSelectedProductView] = useState(null);
  const [expandedModules, setExpandedModules] = useState({});
  const [formData, setFormData] = useState({
    name: "",
    description: "",
    price: "",
    estimatedTimeMinutes: "",
  });
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
      fetchProductVariants();
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
    setNotification(null);
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
      const errorMsg = getErrorMessage(err);
      setError(errorMsg);
      setNotification({
        message: errorMsg,
        type: 'error'
      });
    } finally {
      setLoading(false);
    }
  };

  const fetchAllWorkstationsData = async (stations) => {
    const inventoryMap = {};
    const ordersMap = {};

    for (const station of stations) {
      try {
        const invResponse = await axios.get(`/api/stock/workstation/${station.id}`);
        inventoryMap[station.id] = Array.isArray(invResponse.data) ? invResponse.data : [];
      } catch (err) {
        console.error(`Failed to fetch inventory for WS-${station.id}:`, err);
        inventoryMap[station.id] = [];
      }

      try {
        const ordersResponse = await axios.get(`/api/customer-orders/workstation/${station.id}`);
        ordersMap[station.id] = Array.isArray(ordersResponse.data) ? ordersResponse.data : [];
      } catch (err) {
        // Silently fail - orders endpoint may not be available
        ordersMap[station.id] = [];
      }
    }

    setWorkstationInventory(inventoryMap);
    setWorkstationOrders(ordersMap);
    calculateSystemStats(ordersMap);
  };

  const fetchWorkstationData = async (workstationId) => {
    try {
      const invResponse = await axios.get(`/api/stock/workstation/${workstationId}`);
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
      // Silently fail - orders endpoint may not be available
    }
  };

  // ========== PRODUCT VARIANT MANAGEMENT ==========
  
  const fetchProductVariants = async () => {
    try {
      const response = await axios.get("/api/masterdata/product-variants");
      setProductVariants(Array.isArray(response.data) ? response.data : []);
    } catch (err) {
      console.error("Failed to fetch product variants:", err);
      setProductVariants([]);
    }
  };

  const handleProductFormChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveProduct = async (e) => {
    e.preventDefault();
    
    if (!formData.name || !formData.price || !formData.estimatedTimeMinutes) {
      setError("Name, Price, and Estimated Time are required");
      return;
    }

    setLoading(true);
    setError(null);
    
    try {
      const payload = {
        name: formData.name,
        description: formData.description,
        price: parseFloat(formData.price),
        estimatedTimeMinutes: parseInt(formData.estimatedTimeMinutes)
      };

      if (editingProductId) {
        // Update existing
        await axios.put(`/api/masterdata/product-variants/${editingProductId}`, payload);
        setNotification({ message: "Product variant updated successfully", type: "success" });
      } else {
        // Create new
        await axios.post("/api/masterdata/product-variants", payload);
        setNotification({ message: "Product variant created successfully", type: "success" });
      }

      // Reset form
      setFormData({ name: "", description: "", price: "", estimatedTimeMinutes: "" });
      setEditingProductId(null);
      setShowProductForm(false);
      
      // Refresh list
      await fetchProductVariants();
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      setError(errorMsg);
      setNotification({ message: errorMsg, type: "error" });
    } finally {
      setLoading(false);
    }
  };

  const handleEditProduct = (product) => {
    setFormData({
      name: product.name,
      description: product.description || "",
      price: product.price.toString(),
      estimatedTimeMinutes: product.estimatedTimeMinutes.toString()
    });
    setEditingProductId(product.id);
    setShowProductForm(true);
  };

  const handleDeleteProduct = async (id) => {
    if (!window.confirm("Are you sure you want to delete this product variant?")) {
      return;
    }

    setLoading(true);
    setError(null);

    try {
      await axios.delete(`/api/masterdata/product-variants/${id}`);
      setNotification({ message: "Product variant deleted successfully", type: "success" });
      await fetchProductVariants();
    } catch (err) {
      const errorMsg = getErrorMessage(err);
      setError(errorMsg);
      setNotification({ message: errorMsg, type: "error" });
    } finally {
      setLoading(false);
    }
  };

  const handleCancelForm = () => {
    setFormData({ name: "", description: "", price: "", estimatedTimeMinutes: "" });
    setEditingProductId(null);
    setShowProductForm(false);
    setError(null);
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
                    <th>Product</th>
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
                          {PRODUCT_NAMES[item.itemId]?.name || item.itemType || "PRODUCT"}
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

  // ========== RENDER PRODUCTS TAB ==========
  
  const renderProductsTab = () => {
    const toggleModuleExpand = (moduleId) => {
      setExpandedModules(prev => ({
        ...prev,
        [moduleId]: !prev[moduleId]
      }));
    };

    return (
      <div className="products-tab">
        {/* MANAGEMENT SECTION */}
        <div className="products-management-section">
          <div className="products-header">
            <h3>⚙️ Product Variants Management</h3>
            <button
              className="primary-link"
              onClick={() => {
                setShowProductForm(true);
                setEditingProductId(null);
                setFormData({ name: "", description: "", price: "", estimatedTimeMinutes: "" });
                setError(null);
              }}
            >
              ➕ Add New Product
            </button>
          </div>

          {showProductForm && (
            <div className="product-form-container">
              <h4>{editingProductId ? "Edit Product Variant" : "Create New Product Variant"}</h4>
              <form onSubmit={handleSaveProduct} className="product-form">
                <div className="form-group">
                  <label>Product Name *</label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleProductFormChange}
                    placeholder="e.g., Technic Truck Blue"
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Description</label>
                  <textarea
                    name="description"
                    value={formData.description}
                    onChange={handleProductFormChange}
                    placeholder="Product description"
                    rows="3"
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Price ($) *</label>
                    <input
                      type="number"
                      name="price"
                      value={formData.price}
                      onChange={handleProductFormChange}
                      placeholder="99.99"
                      step="0.01"
                      min="0"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Est. Time (minutes) *</label>
                    <input
                      type="number"
                      name="estimatedTimeMinutes"
                      value={formData.estimatedTimeMinutes}
                      onChange={handleProductFormChange}
                      placeholder="180"
                      min="0"
                      required
                    />
                  </div>
                </div>

                <div className="form-actions">
                  <button type="submit" className="primary-link">
                    {editingProductId ? "✓ Update" : "✓ Create"}
                  </button>
                  <button type="button" onClick={handleCancelForm} className="secondary-link">
                    ✕ Cancel
                  </button>
                </div>
              </form>
            </div>
          )}

          <div className="products-list">
            {productVariants.length > 0 ? (
              <table className="products-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Product Name</th>
                    <th>Description</th>
                    <th>Price</th>
                    <th>Est. Time</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {productVariants.map(product => (
                    <tr key={product.id}>
                      <td>#{product.id}</td>
                      <td className="product-name">{product.name}</td>
                      <td className="product-description">{product.description || "-"}</td>
                      <td>${product.price.toFixed(2)}</td>
                      <td>{product.estimatedTimeMinutes} min</td>
                      <td className="actions">
                        <button
                          className="edit-btn"
                          onClick={() => handleEditProduct(product)}
                          title="Edit"
                        >
                          ✎ Edit
                        </button>
                        <button
                          className="delete-btn"
                          onClick={() => handleDeleteProduct(product.id)}
                          title="Delete"
                        >
                          🗑 Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p className="no-data">No product variants found</p>
            )}
          </div>
        </div>

        {/* CATALOG SECTION */}
        <div className="products-catalog-section">
          <div className="catalog-header">
            <h3>📋 Products Catalog</h3>
            <p className="catalog-subtitle">Total Products: {productVariants.length}</p>
          </div>

          {selectedProduct ? (
            <div className="product-details">
              <button className="btn-back" onClick={() => setSelectedProduct(null)}>
                ← Back to Products
              </button>
              
              <div className="product-info">
                <h2>{selectedProduct.name}</h2>
                <p className="product-desc">{selectedProduct.description}</p>
                <div className="product-specs">
                  <div className="spec-item">
                    <strong>Price:</strong> <span className="price">${selectedProduct.price.toFixed(2)}</span>
                  </div>
                  <div className="spec-item">
                    <strong>Est. Time:</strong> <span>{selectedProduct.estimatedTimeMinutes} minutes</span>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="products-grid">
              {productVariants.length === 0 ? (
                <p className="empty-message">No products available</p>
              ) : (
                productVariants.map(product => (
                  <div 
                    key={product.id} 
                    className="product-card"
                    onClick={() => setSelectedProduct(product)}
                  >
                    <div className="product-card-content">
                      <h3>{product.name}</h3>
                      <p className="card-description">{product.description}</p>
                      <div className="card-specs">
                        <span className="price-badge">${product.price.toFixed(2)}</span>
                        <span className="time-badge">{product.estimatedTimeMinutes}m</span>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>
      </div>
    );
  };

  const renderOrdersTab = () => {
    // Flatten all orders from all workstations
    const allOrders = [];
    Object.entries(workstationOrders).forEach(([wsId, orders]) => {
      if (Array.isArray(orders)) {
        orders.forEach(order => {
          allOrders.push({
            ...order,
            workstationId: parseInt(wsId)
          });
        });
      }
    });

    // Sort by creation time (newest first)
    allOrders.sort((a, b) => {
      const dateA = new Date(a.createdAt || 0);
      const dateB = new Date(b.createdAt || 0);
      return dateB - dateA;
    });

    const getStatusColor = (status) => {
      switch (status) {
        case "PENDING":
          return "#ff9800";
        case "PROCESSING":
          return "#2196f3";
        case "COMPLETED":
          return "#4caf50";
        case "CANCELLED":
          return "#f44336";
        default:
          return "#999";
      }
    };

    return (
      <div className="orders-tab">
        <div className="orders-header">
          <h3>📋 All Created Orders</h3>
          <p className="orders-subtitle">View all customer orders across all workstations</p>
        </div>

        {allOrders.length === 0 ? (
          <div className="no-orders-message">
            <p>No orders found</p>
          </div>
        ) : (
          <div className="orders-table-container">
            <table className="orders-table">
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Workstation</th>
                  <th>Product</th>
                  <th>Status</th>
                  <th>Created At</th>
                  <th>Updated At</th>
                </tr>
              </thead>
              <tbody>
                {allOrders.map((order) => (
                  <tr key={order.id} className="order-row">
                    <td className="order-id">#{order.id}</td>
                    <td>WS-{order.workstationId}</td>
                    <td>{PRODUCT_NAMES[order.itemId]?.name || `Item ${order.itemId}`}</td>
                    <td>
                      <span 
                        className="status-badge" 
                        style={{ backgroundColor: getStatusColor(order.status) }}
                      >
                        {order.status}
                      </span>
                    </td>
                    <td>{new Date(order.createdAt).toLocaleString()}</td>
                    <td>{new Date(order.updatedAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    );
  };

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
      {notification && (
        <ErrorNotification
          message={notification.message}
          type={notification.type}
          onClose={() => setNotification(null)}
        />
      )}
      
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
        <button
          className={`tab-button ${activeTab === "products" ? "active" : ""}`}
          onClick={() => setActiveTab("products")}
        >
          🎯 Product Variants
        </button>
        <button
          className={`tab-button ${activeTab === "orders" ? "active" : ""}`}
          onClick={() => setActiveTab("orders")}
        >
          📋 All Orders
        </button>
      </div>

      <div className="tab-content">
        {loading && <div className="loading">Loading system data...</div>}
        {!loading && activeTab === "overview" && renderOverviewTab()}
        {!loading && activeTab === "workstation" && renderWorkstationTab()}
        {!loading && activeTab === "inventory" && renderAllInventoryTab()}
        {!loading && activeTab === "products" && renderProductsTab()}
        {!loading && activeTab === "orders" && renderOrdersTab()}
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

        /* Product Variants Tab Styles */
        .products-tab {
          padding: 20px;
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 30px;
        }

        .products-management-section {
          grid-column: 1;
        }

        .products-catalog-section {
          grid-column: 2;
          border-left: 2px solid #ddd;
          padding-left: 30px;
        }

        .products-header,
        .catalog-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
          padding-bottom: 15px;
          border-bottom: 2px solid #e0e0e0;
        }

        .products-header h3,
        .catalog-header h3 {
          margin: 0;
          color: #2c5aa0;
          font-size: 18px;
        }

        .catalog-subtitle {
          margin: 0;
          color: #888;
          font-size: 13px;
        }

        .product-form-container {
          background: #f9f9f9;
          border: 1px solid #ddd;
          border-radius: 4px;
          padding: 20px;
          margin-bottom: 20px;
        }

        .product-form-container h4 {
          margin-top: 0;
          color: #333;
        }

        .product-form {
          display: flex;
          flex-direction: column;
          gap: 15px;
        }

        .form-group {
          display: flex;
          flex-direction: column;
        }

        .form-group label {
          font-weight: bold;
          margin-bottom: 5px;
          color: #333;
          font-size: 13px;
        }

        .form-group input,
        .form-group textarea {
          padding: 10px;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 13px;
          font-family: inherit;
        }

        .form-group input:focus,
        .form-group textarea:focus {
          outline: none;
          border-color: #2c5aa0;
          box-shadow: 0 0 5px rgba(44, 90, 160, 0.3);
        }

        .form-row {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 15px;
        }

        .form-actions {
          display: flex;
          gap: 10px;
          justify-content: flex-start;
        }

        .form-actions button {
          padding: 10px 20px;
          border: none;
          border-radius: 4px;
          font-size: 13px;
          font-weight: bold;
          cursor: pointer;
          transition: all 0.3s ease;
        }

        .primary-link {
          background: #2c5aa0;
          color: white;
          padding: 10px 20px;
          border: none;
          border-radius: 4px;
          font-size: 13px;
          font-weight: bold;
          cursor: pointer;
          transition: all 0.3s ease;
        }

        .primary-link:hover {
          background: #1e3d66;
        }

        .secondary-link {
          background: #ccc;
          color: #333;
          padding: 10px 20px;
          border: none;
          border-radius: 4px;
          font-size: 13px;
          font-weight: bold;
          cursor: pointer;
          transition: all 0.3s ease;
        }

        .secondary-link:hover {
          background: #aaa;
        }

        .products-list {
          margin-top: 20px;
        }

        .products-table {
          width: 100%;
          border-collapse: collapse;
          background: white;
          border: 1px solid #ddd;
          border-radius: 4px;
          overflow: hidden;
        }

        .products-table thead {
          background: #e8f4f8;
          border-bottom: 2px solid #b8d4e0;
        }

        .products-table th {
          padding: 12px;
          text-align: left;
          font-weight: bold;
          color: #2c5aa0;
          font-size: 12px;
          text-transform: uppercase;
        }

        .products-table tbody tr:hover {
          background: #f5f5f5;
        }

        .products-table td {
          padding: 12px;
          border-bottom: 1px solid #f0f0f0;
          font-size: 13px;
          color: #555;
        }

        .product-name {
          font-weight: bold;
          color: #2c5aa0;
        }

        .product-description {
          color: #888;
          font-size: 12px;
          max-width: 300px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .actions {
          display: flex;
          gap: 5px;
          justify-content: flex-start;
        }

        .edit-btn,
        .delete-btn {
          padding: 5px 10px;
          border: none;
          border-radius: 3px;
          font-size: 12px;
          cursor: pointer;
          font-weight: bold;
          transition: all 0.3s ease;
          white-space: nowrap;
        }

        .edit-btn {
          background: #17a2b8;
          color: white;
        }

        .edit-btn:hover {
          background: #138496;
        }

        .delete-btn {
          background: #dc3545;
          color: white;
        }

        .delete-btn:hover {
          background: #c82333;
        }

        .no-data {
          text-align: center;
          padding: 40px;
          color: #999;
          font-style: italic;
        }

        /* Products Catalog Grid Styles */
        .products-grid {
          display: grid;
          grid-template-columns: 1fr;
          gap: 15px;
          margin-top: 20px;
        }

        .product-card {
          background: white;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 15px;
          cursor: pointer;
          transition: all 0.3s ease;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .product-card:hover {
          border-color: #2c5aa0;
          box-shadow: 0 4px 12px rgba(44, 90, 160, 0.2);
          transform: translateY(-2px);
        }

        .product-card-content h3 {
          margin: 0 0 8px 0;
          color: #2c5aa0;
          font-size: 16px;
        }

        .card-description {
          margin: 0 0 12px 0;
          color: #666;
          font-size: 13px;
          line-height: 1.4;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }

        .card-specs {
          display: flex;
          gap: 10px;
          flex-wrap: wrap;
        }

        .price-badge,
        .time-badge {
          display: inline-block;
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 500;
        }

        .price-badge {
          background: #e8f4f8;
          color: #2c5aa0;
        }

        .time-badge {
          background: #f0f0f0;
          color: #333;
        }

        .empty-message {
          text-align: center;
          color: #999;
          padding: 20px;
          font-size: 14px;
        }

        .catalog-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 20px;
          padding-bottom: 15px;
          border-bottom: 2px solid #e0e0e0;
        }

        .catalog-header h3 {
          margin: 0;
          color: #2c5aa0;
          font-size: 18px;
        }

        .catalog-subtitle {
          margin: 0;
          color: #888;
          font-size: 13px;
        }

        .products-management-section {
          flex: 1;
        }

        .products-catalog-section {
          flex: 1;
          border-left: 2px solid #ddd;
          padding-left: 30px;
        }

        .products-management-tab {
          display: flex;
          gap: 30px;
          padding: 20px;
        }

        @media (max-width: 1200px) {
          .products-management-tab {
            flex-direction: column;
          }

          .products-catalog-section {
            border-left: none;
            border-top: 2px solid #ddd;
            padding-left: 0;
            padding-top: 30px;
          }
        }

        /* Orders Tab Styles */
        .orders-tab {
          padding: 20px;
        }

        .orders-header {
          margin-bottom: 20px;
          padding-bottom: 15px;
          border-bottom: 2px solid #e0e0e0;
        }

        .orders-header h3 {
          margin: 0 0 5px 0;
          color: #2c5aa0;
          font-size: 18px;
        }

        .orders-subtitle {
          margin: 0;
          color: #888;
          font-size: 13px;
        }

        .orders-table-container {
          background: white;
          border: 1px solid #ddd;
          border-radius: 4px;
          overflow-x: auto;
        }

        .orders-table {
          width: 100%;
          border-collapse: collapse;
          font-size: 13px;
        }

        .orders-table thead {
          background: #f5f5f5;
          border-bottom: 2px solid #ddd;
        }

        .orders-table th {
          padding: 12px 15px;
          text-align: left;
          font-weight: 600;
          color: #2c5aa0;
        }

        .orders-table tbody tr {
          border-bottom: 1px solid #eee;
          transition: background-color 0.2s ease;
        }

        .orders-table tbody tr:hover {
          background-color: #f9f9f9;
        }

        .orders-table td {
          padding: 12px 15px;
          color: #333;
        }

        .order-id {
          font-weight: 500;
          color: #2c5aa0;
        }

        .status-badge {
          display: inline-block;
          padding: 4px 12px;
          border-radius: 12px;
          color: white;
          font-size: 12px;
          font-weight: 600;
          text-transform: uppercase;
        }

        .no-orders-message {
          text-align: center;
          padding: 40px 20px;
          color: #999;
          font-size: 14px;
          background: #f9f9f9;
          border: 1px solid #ddd;
          border-radius: 4px;
          margin: 20px 0;
        }

        .no-orders-message p {
          margin: 0;
        }
      `}</style>
    </section>
  );
}

export default AdminDashboard;
