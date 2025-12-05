import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import axios from "axios";

function ProductionPlanningPage() {
  const { session } = useAuth();
  const [productionOrders, setProductionOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProductionOrders();
    // Refresh every 15 seconds
    const interval = setInterval(fetchProductionOrders, 15000);
    return () => clearInterval(interval);
  }, []);

  const fetchProductionOrders = async () => {
    setLoading(true);
    setError(null);

    try {
      // TODO: Implement production planning API endpoint
      // const response = await axios.get("/api/production-orders");
      // setProductionOrders(Array.isArray(response.data) ? response.data : []);
      setProductionOrders([]);
    } catch (err) {
      setError(
        "Failed to load production orders: " +
          (err.response?.data?.message || err.message)
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <section className="page-container">
      <div className="page-header">
        <h2>📋 Production Planning</h2>
        <p>Plan and schedule production workflows across manufacturing and assembly stations</p>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="content-area">
        {loading ? (
          <p>Loading production orders...</p>
        ) : productionOrders.length === 0 ? (
          <p className="info-message">
            No production orders available. Check back soon for incoming production requests.
          </p>
        ) : (
          <div className="orders-list">
            {productionOrders.map((order) => (
              <div key={order.id} className="order-card">
                <h3>Order #{order.id}</h3>
                <p>Status: {order.status}</p>
              </div>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

export default ProductionPlanningPage;
