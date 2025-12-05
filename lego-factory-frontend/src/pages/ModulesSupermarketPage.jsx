import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import axios from "axios";

function ModulesSupermarketPage() {
  const { session } = useAuth();
  const [warehouseOrders, setWarehouseOrders] = useState([]);
  const [filteredOrders, setFilteredOrders] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);
  const [statusFilter, setStatusFilter] = useState("PENDING");
  const [fulfillmentInProgress, setFulfillmentInProgress] = useState({});

  useEffect(() => {
    fetchWarehouseOrders();
    // Refresh orders every 30 seconds
    const interval = setInterval(fetchWarehouseOrders, 30000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    // Filter orders based on selected status
    if (statusFilter === "ALL") {
      setFilteredOrders(warehouseOrders);
    } else {
      setFilteredOrders(warehouseOrders.filter(order => order.status === statusFilter));
    }
  }, [warehouseOrders, statusFilter]);

  const fetchWarehouseOrders = async () => {
    try {
      // Fetch warehouse orders for Modules Supermarket (workstation 8)
      const workstationId = session?.user?.workstationId || 8;
      console.log("Fetching warehouse orders for workstation:", workstationId);
      
      const response = await axios.get(`/api/warehouse-orders/workstation/${workstationId}`);
      console.log("Warehouse orders response:", response.data);
      
      const data = response.data;
      if (Array.isArray(data)) {
        setWarehouseOrders(data);
        setError(null);
      } else {
        console.warn("Expected array of warehouse orders, got:", data);
        setWarehouseOrders([]);
        setError("Unexpected response format from server.");
      }
    } catch (err) {
      console.error("Failed to fetch warehouse orders:", err);
      if (err.response?.status === 403) {
        setError("Authorization denied. You need MODULES_SUPERMARKET role.");
      } else if (err.response?.status === 404) {
        // No orders exist yet
        setWarehouseOrders([]);
        setError(null);
      } else {
        setError("Failed to load warehouse orders: " + (err.response?.data?.message || err.message));
      }
    }
  };

  const handleFulfillOrder = async (orderId, orderNumber) => {
    setFulfillmentInProgress(prev => ({ ...prev, [orderId]: true }));
    setError(null);
    setSuccessMessage(null);

    try {
      console.log("Fulfilling warehouse order:", orderId);
      const response = await axios.put(`/api/warehouse-orders/${orderId}/fulfill-modules`);
      
      console.log("Fulfillment response:", response.data);
      setSuccessMessage(`Warehouse order ${orderNumber} fulfilled successfully!`);
      
      // Refresh orders after fulfillment
      await fetchWarehouseOrders();
    } catch (err) {
      console.error("Failed to fulfill warehouse order:", err);
      setError("Failed to fulfill order: " + (err.response?.data?.message || err.message));
    } finally {
      setFulfillmentInProgress(prev => ({ ...prev, [orderId]: false }));
    }
  };

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      console.log("Updating warehouse order status:", orderId, "to", newStatus);
      const response = await axios.patch(
        `/api/warehouse-orders/${orderId}/status?status=${newStatus}`
      );
      
      console.log("Status update response:", response.data);
      setSuccessMessage("Warehouse order status updated successfully!");
      
      // Refresh orders after status change
      await fetchWarehouseOrders();
    } catch (err) {
      console.error("Failed to update warehouse order status:", err);
      setError("Failed to update status: " + (err.response?.data?.message || err.message));
    }
  };

  const getStatusBadgeColor = (status) => {
    switch (status) {
      case "PENDING":
        return "bg-yellow-100 text-yellow-800";
      case "PROCESSING":
        return "bg-blue-100 text-blue-800";
      case "FULFILLED":
        return "bg-green-100 text-green-800";
      case "REJECTED":
        return "bg-red-100 text-red-800";
      case "CANCELLED":
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  const getTriggerScenarioBadgeColor = (scenario) => {
    switch (scenario) {
      case "SCENARIO_2":
        return "bg-purple-100 text-purple-800";
      case "SCENARIO_3":
        return "bg-indigo-100 text-indigo-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  return (
    <section>
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-blue-700 mb-2">Modules Supermarket</h1>
        <p className="text-gray-600">Manage incoming warehouse orders for module fulfillment</p>
      </div>

        {/* Messages */}
        {error && (
          <div className="form-error mb-6 p-4 bg-red-50 border-l-4 border-red-600 rounded">
            <p className="font-semibold text-red-900">Error</p>
            <p className="text-red-800 text-sm mt-1">{error}</p>
            <button
              onClick={() => setError(null)}
              className="text-red-600 hover:text-red-800 font-semibold text-sm mt-2"
            >
              Dismiss
            </button>
          </div>
        )}

        {successMessage && (
          <div className="form-success-details mb-6">
            <p className="font-semibold text-green-900">Success</p>
            <p className="text-green-800 text-sm mt-1">{successMessage}</p>
            <button
              onClick={() => setSuccessMessage(null)}
              className="text-green-700 hover:text-green-900 font-semibold text-sm mt-2"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Filter and Refresh Controls */}
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", gap: "1rem", marginBottom: "1.5rem" }}>
          <button
            onClick={fetchWarehouseOrders}
            disabled={loading}
            className="primary-button"
          >
            {loading ? "Refreshing..." : "Refresh"}
          </button>
          <div style={{ display: "flex", gap: "0.5rem", alignItems: "flex-end" }}>
            <label className="text-sm font-semibold text-gray-700 uppercase whitespace-nowrap">
              Filter by Status
            </label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white font-medium text-gray-700"
            >
              <option value="ALL">All Orders</option>
              <option value="PENDING">Pending</option>
              <option value="PROCESSING">Processing</option>
              <option value="FULFILLED">Fulfilled</option>
              <option value="REJECTED">Rejected</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
        </div>

        {/* Orders Table */}
        <div className="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200 mb-6">
          {filteredOrders.length === 0 ? (
            <div className="p-8 text-center text-gray-500">
              <p className="text-lg font-semibold">No warehouse orders found</p>
              {statusFilter !== "ALL" && (
                <p className="text-sm mt-2 text-gray-400">Try changing the filter to see other orders</p>
              )}
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="products-table w-full">
                <thead className="bg-blue-700 text-white">
                  <tr>
                    <th className="text-left">Order #</th>
                    <th className="text-left">Source Order</th>
                    <th className="text-left">Status</th>
                    <th className="text-left">Scenario</th>
                    <th className="text-left">Items</th>
                    <th className="text-left">Created</th>
                    <th className="text-left">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredOrders.map((order) => (
                    <tr key={order.id}>
                      <td className="font-semibold">
                        <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs">
                          {order.warehouseOrderNumber}
                        </span>
                      </td>
                      <td className="text-gray-700 font-medium">
                        CO-{order.sourceCustomerOrderId}
                      </td>
                      <td>
                        <span className={`inline-block px-3 py-1 text-xs font-bold rounded ${getStatusBadgeColor(order.status)}`}>
                          {order.status}
                        </span>
                      </td>
                      <td>
                        <span className={`inline-block px-3 py-1 text-xs font-bold rounded ${getTriggerScenarioBadgeColor(order.triggerScenario)}`}>
                          {order.triggerScenario || "—"}
                        </span>
                      </td>
                      <td className="text-gray-700">
                        {Array.isArray(order.warehouseOrderItems) && order.warehouseOrderItems.length > 0 ? (
                          <div className="flex flex-wrap gap-2">
                            {order.warehouseOrderItems.map((item) => (
                              <span key={item.id} className="bg-gray-100 px-2 py-1 rounded text-xs whitespace-nowrap">
                                <span className="font-bold text-gray-800">{item.itemName || `Item ${item.itemId}`}</span>
                                <span className="text-gray-600"> — </span>
                                <span className="text-orange-700 font-bold">{item.requestedQuantity}req</span>
                                <span className="text-gray-600">/</span>
                                <span className="text-green-700 font-bold">{item.fulfilledQuantity || 0}ful</span>
                              </span>
                            ))}
                          </div>
                        ) : (
                          <span className="text-gray-400 text-sm">—</span>
                        )}
                      </td>
                      <td className="text-gray-700 text-sm font-medium">
                        {order.createdAt ? new Date(order.createdAt).toLocaleDateString('en-US', {month: 'short', day: 'numeric'}) : "—"}
                      </td>
                      <td className="space-x-1">
                        {(order.status === "PENDING" || order.status === "PROCESSING") && (
                          <button
                            onClick={() => handleFulfillOrder(order.id, order.warehouseOrderNumber)}
                            disabled={fulfillmentInProgress[order.id]}
                            className="primary-button px-3 py-1 text-xs"
                          >
                            {fulfillmentInProgress[order.id] ? "Processing..." : "Fulfill"}
                          </button>
                        )}
                        {order.status !== "FULFILLED" && order.status !== "CANCELLED" && (
                          <button
                            onClick={() => handleStatusChange(order.id, "CANCELLED")}
                            className="px-3 py-1 text-xs bg-red-600 text-white rounded hover:bg-red-700 transition"
                          >
                            Cancel
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Summary Stats and Info Boxes */}
        <div style={{ display: "flex", gap: "2rem", marginTop: "2rem" }}>
          {/* Summary Stats */}
          <div className="bg-white rounded-lg shadow-md p-6 border border-gray-200" style={{ flex: 1 }}>
            <h3 className="text-lg font-bold text-gray-900 mb-6 uppercase tracking-wide">Order Summary</h3>
            <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", paddingBottom: "1rem", borderBottom: "1px solid #e5e7eb" }}>
                <p className="text-gray-600 text-sm font-bold uppercase tracking-wider">Total Orders</p>
                <p className="text-2xl font-bold text-gray-900">{warehouseOrders.length}</p>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", paddingBottom: "1rem", borderBottom: "1px solid #e5e7eb" }}>
                <p className="text-yellow-700 text-sm font-bold uppercase tracking-wider">Pending</p>
                <p className="text-2xl font-bold text-yellow-600">
                  {warehouseOrders.filter(o => o.status === "PENDING").length}
                </p>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", paddingBottom: "1rem", borderBottom: "1px solid #e5e7eb" }}>
                <p className="text-blue-700 text-sm font-bold uppercase tracking-wider">Processing</p>
                <p className="text-2xl font-bold text-blue-600">
                  {warehouseOrders.filter(o => o.status === "PROCESSING").length}
                </p>
              </div>
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <p className="text-green-700 text-sm font-bold uppercase tracking-wider">Fulfilled</p>
                <p className="text-2xl font-bold text-green-600">
                  {warehouseOrders.filter(o => o.status === "FULFILLED").length}
                </p>
              </div>
            </div>
          </div>

          {/* Information Box */}
          <div className="bg-blue-50 border-l-4 border-blue-600 rounded p-6" style={{ flex: 1 }}>
            <h3 className="font-bold text-blue-900 text-base mb-4">About Warehouse Orders</h3>
            <ul className="space-y-2 text-blue-800 text-sm">
              <li><strong>SCENARIO_2:</strong> Plant Warehouse has no stock, requesting all items from Modules Supermarket</li>
              <li><strong>SCENARIO_3:</strong> Plant Warehouse has partial stock, requesting remaining items from Modules Supermarket</li>
              <li>Click <strong>Fulfill</strong> to complete the warehouse order and deduct stock from inventory</li>
              <li>Orders are automatically fetched every 30 seconds for real-time updates</li>
            </ul>
          </div>
        </div>
    </section>
  );
}

export default ModulesSupermarketPage;
