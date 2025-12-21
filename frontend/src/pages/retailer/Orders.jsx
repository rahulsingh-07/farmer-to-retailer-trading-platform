import React, { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import OrderCard from "../../components/OrderCard";
import "../../css/Orders.css";

const statusFilters = [
  { value: "ALL", label: "All" },
  { value: "CONFIRMED", label: "Confirmed" },
  { value: "SHIPPED", label: "Shipped" },
];

const RetailerOrders = () => {
  const { token } = useAuth();
  const [orders, setOrders] = useState([]);
  const [status, setStatus] = useState("ALL");
  const [loading, setLoading] = useState(true);

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (status !== "ALL") params.append("status", status);
      const query = params.toString();
      let path = "/user/retailer/orders";
      if (query) {
        path = path + "?" + query;
      }
      const data = await api.get(path, token);
      setOrders(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      toast.error(err.message || "Unable to load orders");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [status]);

  return (
    <div className="orders-page">
      <div className="orders-header">
        <div>
          <p className="kicker">Purchases</p>
          <h1 className="orders-title">Retailer Orders</h1>
          <p className="orders-subtitle">Review every purchase you've placed. Filter to see confirmed or shipped orders quickly.</p>
        </div>
        <div className="filters">
          {statusFilters.map((item) => (
            <button
              key={item.value}
              type="button"
              className={`filter-btn ${status === item.value ? "active" : ""}`}
              onClick={() => setStatus(item.value)}
            >
              {item.label}
            </button>
          ))}
          <button type="button" className="filter-btn" onClick={fetchOrders} disabled={loading}>
            {loading ? "Refreshing..." : "Refresh"}
          </button>
        </div>
      </div>

      {loading && <div className="loading-block">Loading orders...</div>}

      {!loading && orders.length === 0 && <div className="empty-state">No orders found for this filter.</div>}

      {!loading && orders.length > 0 && (
        <div className="orders-grid">
          {orders.map((order) => (
            <OrderCard key={order.orderId} order={order} kind="retailer" />
          ))}
        </div>
      )}
    </div>
  );
};

export default RetailerOrders;
