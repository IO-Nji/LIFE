import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import axios from "axios";

function PlantWarehousePage() {
  const { session } = useAuth();
  const [products, setProducts] = useState([]);
  const [selectedProducts, setSelectedProducts] = useState({});
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  useEffect(() => {
    fetchProducts();
  }, []);

  useEffect(() => {
    if (session?.user?.workstationId) {
      fetchOrders();
    }
  }, [session?.user?.workstationId]);

  const fetchProducts = async () => {
    try {
      const response = await axios.get("/api/masterdata/product-variants");
      console.log("Products response:", response.data);
      console.log("First product structure:", response.data?.[0]);
      setProducts(response.data);
    } catch (err) {
      setError("Failed to load products: " + (err.response?.data?.message || err.message));
    }
  };

  const fetchOrders = async () => {
    if (!session?.user?.workstationId) {
      console.log("Workstation ID not available yet");
      setOrders([]);
      return;
    }
    try {
      console.log("Fetching orders from:", `/api/customer-orders/workstation/${session.user.workstationId}`);
      const response = await axios.get("/api/customer-orders/workstation/" + session.user.workstationId);
      console.log("Orders response status:", response.status);
      console.log("Orders response content-type:", response.headers['content-type']);
      console.log("Orders response data type:", typeof response.data, "is array:", Array.isArray(response.data));
      const data = response.data;
      if (Array.isArray(data)) {
        setOrders(data);
      } else {
        console.warn("Expected array of orders, got:", data);
        setOrders([]);
        setError("Unexpected response format from server. Got HTML instead of JSON. Backend service may not be responding correctly.");
      }
    } catch (err) {
      console.error("Failed to fetch orders:", err);
      console.error("Error response:", err.response?.status, err.response?.headers, err.response?.data?.substring?.(0, 200));
      if (err.response?.status === 403) {
        setError("Authorization denied. Check your role permissions.");
      } else if (err.response?.status === 404) {
        setOrders([]);
      } else {
        setError("Failed to load orders: " + (err.response?.data?.message || err.message));
      }
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

      setSuccessMessage(`Order created: ${response.data.orderNumber}`);
      setSelectedProducts({});
      fetchOrders();
    } catch (err) {
      setError("Failed to create order: " + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <section>
      <h2>Plant Warehouse Dashboard</h2>
      
      {session?.user?.workstationId ? (
        <p>Workstation ID: <strong>{session.user.workstationId}</strong></p>
      ) : (
        <div className="error-message">⚠️ No workstation assigned to your account. Contact administrator.</div>
      )}

      {error && <div className="error-message">{error}</div>}
      {successMessage && <div className="success-message">{successMessage}</div>}

      <div className="warehouse-layout">
        <div className="create-order-section">
          <h3>Create Customer Order</h3>
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
          <h3>Recent Orders</h3>
          {Array.isArray(orders) && orders.length > 0 ? (
            <div className="orders-list">
              {orders.map((order) => (
                <div key={order.id} className="order-card">
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
                </div>
              ))}
            </div>
          ) : (
            <p>No orders found for this workstation</p>
          )}
        </div>
      </div>
    </section>
  );
}

export default PlantWarehousePage;
