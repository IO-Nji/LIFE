import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import axios from "axios";

function PlantWarehousePage() {
  const { session } = useAuth();
  const [products, setProducts] = useState([]);
  const [selectedProducts, setSelectedProducts] = useState({});
  const [orders, setOrders] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [loading, setLoading] = useState(false);
  const [fulfillingOrderId, setFulfillingOrderId] = useState(null);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    fetchProducts();
    if (session?.user?.workstationId) {
      fetchOrders();
      fetchInventory();
    }
  }, [session?.user?.workstationId]);

  const fetchProducts = async () => {
    try {
      const response = await axios.get("/api/masterdata/product-variants");
      setProducts(response.data);
    } catch (err) {
      setError("Failed to load products: " + (err.response?.data?.message || err.message));
    }
  };

  const fetchOrders = async () => {
    if (!session?.user?.workstationId) {
      setOrders([]);
      return;
    }
    try {
      const response = await axios.get("/api/customer-orders/workstation/" + session.user.workstationId);
      if (Array.isArray(response.data)) {
        setOrders(response.data);
      } else {
        setOrders([]);
        setError("Unexpected response format from server.");
      }
    } catch (err) {
      if (err.response?.status === 404) {
        setOrders([]);
      } else {
        setError("Failed to load orders: " + (err.response?.data?.message || err.message));
      }
    }
  };

  const fetchInventory = async () => {
    if (!session?.user?.workstationId) return;
    try {
      const response = await axios.get(`/api/inventory/workstation/${session.user.workstationId}`);
      if (Array.isArray(response.data)) {
        setInventory(response.data);
      } else {
        setInventory([]);
      }
    } catch (err) {
      console.error("Failed to fetch inventory:", err);
      setInventory([]);
    }
  };

  const handleQuantityChange = (productId, quantity) => {
    setSelectedProducts({
      ...selectedProducts,
      [productId]: parseInt(quantity) || 0,
    });
  };

  const handleCreateOrder = async () => {
    if (!session?.user?.workstationId) {
      setError("Cannot create order: workstation ID not found in session");
      return;
    }

    const orderItems = Object.entries(selectedProducts)
      .filter(([_, quantity]) => quantity > 0)
      .map(([productId, quantity]) => ({
        itemType: "PRODUCT",
        itemId: parseInt(productId),
        quantity,
        notes: "",
      }));

    if (orderItems.length === 0) {
      setError("Please select at least one product with quantity > 0");
      return;
    }

    setLoading(true);
    setError(null);
    setSuccessMessage(null);

    try {
      const response = await axios.post("/api/customer-orders", {
        orderItems,
        workstationId: session.user.workstationId,
        notes: "Plant warehouse order",
      });

      setSuccessMessage(`Order created: ${response.data.orderNumber} - Click "Fulfill" to process`);
      setSelectedProducts({});
      fetchOrders();
    } catch (err) {
      setError("Failed to create order: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleFulfillOrder = async (orderId) => {
    setFulfillingOrderId(orderId);
    setError(null);
    setSuccessMessage(null);

    try {
      const response = await axios.put(`/api/customer-orders/${orderId}/fulfill`);
      setSuccessMessage(`Order fulfilled successfully! Status: ${response.data.status}`);
      fetchOrders();
      fetchInventory();
    } catch (err) {
      setError("Failed to fulfill order: " + (err.response?.data?.message || err.message));
    } finally {
      setFulfillingOrderId(null);
    }
  };

  return (
    <section className="plant-warehouse-page">
      <h2>Plant Warehouse Dashboard</h2>
      
      {session?.user?.workstationId ? (
        <p>Workstation ID: <strong>{session.user.workstationId}</strong></p>
      ) : (
        <div className="error-message">⚠️ No workstation assigned to your account. Contact administrator.</div>
      )}

      {error && <div className="error-message">{error}</div>}
      {successMessage && <div className="success-message">{successMessage}</div>}

      <div className="warehouse-layout">
        <div className="inventory-section">
          <h3>📦 Current Inventory</h3>
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
                    <td>{item.itemType || "PRODUCT"}</td>
                    <td>#{item.itemId}</td>
                    <td className="quantity-cell">{item.quantity}</td>
                    <td>{item.updatedAt ? new Date(item.updatedAt).toLocaleDateString() : "N/A"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <p className="info-text">No inventory records yet</p>
          )}
        </div>

        <div className="create-order-section">
          <h3>➕ Create Customer Order</h3>
          <table className="products-table">
            <thead>
              <tr>
                <th>Product Variant</th>
                <th>Price</th>
                <th>Estimated Time</th>
                <th>Quantity</th>
              </tr>
            </thead>
            <tbody>
              {products.length > 0 ? (
                products.map((product) => (
                  <tr key={product.id}>
                    <td>{product.name || "Unknown"}</td>
                    <td>${(product.price || 0).toFixed(2)}</td>
                    <td>{product.estimatedTimeMinutes || 0} min</td>
                    <td>
                      <input
                        type="number"
                        min="0"
                        value={selectedProducts[product.id] || 0}
                        onChange={(e) => handleQuantityChange(product.id, e.target.value)}
                        className="quantity-input"
                      />
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4">No products available</td>
                </tr>
              )}
            </tbody>
          </table>
          <button
            onClick={handleCreateOrder}
            disabled={loading}
            className="primary-button"
          >
            {loading ? "Creating Order..." : "Create Order"}
          </button>
        </div>

        <div className="orders-section">
          <h3>📋 Recent Orders</h3>
          {Array.isArray(orders) && orders.length > 0 ? (
            <div className="orders-list">
              {orders.map((order) => (
                <div key={order.id} className="order-card">
                  <div className="order-header">
                    <div>
                      <p>
                        <strong>Order:</strong> {order.orderNumber}
                      </p>
                      <p>
                        <strong>Status:</strong>{" "}
                        <span className={`status-badge status-${order.status.toLowerCase()}`}>
                          {order.status}
                        </span>
                      </p>
                      <p>
                        <strong>Date:</strong> {new Date(order.orderDate).toLocaleDateString()}
                      </p>
                      <p>
                        <strong>Items:</strong> {order.orderItems?.length || 0}
                      </p>
                      {order.notes && (
                        <p>
                          <strong>Notes:</strong> {order.notes}
                        </p>
                      )}
                    </div>
                    {order.status === "PENDING" && (
                      <button
                        onClick={() => handleFulfillOrder(order.id)}
                        disabled={fulfillingOrderId === order.id}
                        className="fulfill-button"
                      >
                        {fulfillingOrderId === order.id ? "Processing..." : "Fulfill Order"}
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="info-text">No orders found for this workstation</p>
          )}
        </div>
      </div>

      <style>{`
        .plant-warehouse-page {
          padding: 20px;
        }

        .warehouse-layout {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px;
          margin-top: 20px;
        }

        .inventory-section,
        .create-order-section,
        .orders-section {
          background: #f9f9f9;
          border: 1px solid #ddd;
          border-radius: 8px;
          padding: 15px;
        }

        .inventory-table,
        .products-table {
          width: 100%;
          border-collapse: collapse;
          margin-bottom: 15px;
          font-size: 13px;
        }

        .inventory-table th,
        .products-table th {
          background: #e8f4f8;
          padding: 10px;
          text-align: left;
          font-weight: bold;
          border-bottom: 2px solid #b8d4e0;
        }

        .inventory-table td,
        .products-table td {
          padding: 8px;
          border-bottom: 1px solid #ddd;
        }

        .quantity-input {
          width: 70px;
          padding: 5px;
          border: 1px solid #ddd;
          border-radius: 4px;
          text-align: center;
        }

        .quantity-cell {
          text-align: center;
          font-weight: bold;
          color: #2c5aa0;
        }

        .primary-button,
        .fulfill-button {
          padding: 10px 20px;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          font-weight: bold;
          transition: background-color 0.3s;
        }

        .primary-button {
          background: #2c5aa0;
          color: white;
          width: 100%;
        }

        .primary-button:hover:not(:disabled) {
          background: #1e3f5a;
        }

        .primary-button:disabled {
          background: #ccc;
          cursor: not-allowed;
        }

        .fulfill-button {
          background: #28a745;
          color: white;
          padding: 8px 15px;
        }

        .fulfill-button:hover:not(:disabled) {
          background: #218838;
        }

        .fulfill-button:disabled {
          background: #ccc;
          cursor: not-allowed;
        }

        .order-card {
          background: white;
          border: 1px solid #ddd;
          border-radius: 4px;
          padding: 15px;
          margin-bottom: 10px;
        }

        .order-header {
          display: flex;
          justify-content: space-between;
          align-items: flex-start;
          gap: 15px;
        }

        .order-header div {
          flex: 1;
        }

        .order-card p {
          margin: 5px 0;
          font-size: 13px;
        }

        .order-card strong {
          color: #333;
        }

        .status-badge {
          display: inline-block;
          padding: 3px 8px;
          border-radius: 3px;
          font-size: 11px;
          font-weight: bold;
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

        .error-message {
          background: #f8d7da;
          color: #721c24;
          padding: 12px;
          border-radius: 4px;
          margin-bottom: 15px;
          border: 1px solid #f5c6cb;
        }

        .success-message {
          background: #d4edda;
          color: #155724;
          padding: 12px;
          border-radius: 4px;
          margin-bottom: 15px;
          border: 1px solid #c3e6cb;
        }

        .info-text {
          color: #666;
          font-style: italic;
        }
      `}</style>
    </section>
  );
}

export default PlantWarehousePage;
