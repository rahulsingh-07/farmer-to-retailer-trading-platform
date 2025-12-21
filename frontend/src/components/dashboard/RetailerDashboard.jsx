import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/RetailerDashboard.css";

const RetailerDashboard = () => {
  const { user, token } = useAuth();
  const [supplierSignals, setSupplierSignals] = useState([]);
  const [orderStats, setOrderStats] = useState({
    total: 0,
    needConfirmation: 0,
    confirmed: 0,
    shipped: 0,
  });
  const [unreadNotifications, setUnreadNotifications] = useState(0);

  useEffect(() => {
    const fetchSignals = async () => {
      try {
        const data = await api.get("/public/crops");
        const raw = Array.isArray(data?.content) ? data.content : Array.isArray(data) ? data : [];
        const byCategory = raw.reduce((acc, item) => {
          const cat = item.category || "Other";
          const price = Number(item.pricePerUnit || 0);
          if (!acc[cat]) acc[cat] = { count: 0, totalPrice: 0 };
          acc[cat].count += 1;
          acc[cat].totalPrice += Number.isFinite(price) ? price : 0;
          return acc;
        }, {});

        const signals = Object.entries(byCategory)
          .map(([cat, stats]) => {
            const avg = stats.count > 0 ? Math.round(stats.totalPrice / stats.count) : 0;
            return {
              id: cat.toLowerCase().replaceAll(/\s+/g, "-"),
              name: cat,
              change: `+${stats.count} lots`,
              note: avg > 0 ? `Avg ₹${avg} per unit` : "No price data",
            };
          })
          .sort((a, b) => Number.parseInt(b.change, 10) - Number.parseInt(a.change, 10))
          .slice(0, 3);

        setSupplierSignals(signals.length ? signals : []);
      } catch (err) {
        console.error("Failed to load supplier signals", err);
        setSupplierSignals([]);
      }
    };

    fetchSignals();
  }, []);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const data = await api.get("/user/retailer/dashboard", token);
        const confirmed = Number(data?.confirmed) || 0;
        const shipped = Number(data?.shipped) || 0;
        const needConfirmation = Number(data?.needConfirmation) || 0;
        const notifications = Number(data?.notifications) || 0;

        setOrderStats({
          total: confirmed + shipped + needConfirmation,
          needConfirmation,
          confirmed,
          shipped,
        });

        setUnreadNotifications(notifications);
      } catch (err) {
        console.error("Failed to load dashboard summary", err);
      }
    };

    if (token) {
      fetchDashboard();
    }
  }, [token]);

  let renderedSignals = supplierSignals;
  if (!supplierSignals.length) {
    renderedSignals = [{ id: "fallback-1", name: "Loading", change: "+0", note: "Fetching live data" }];
  }

  const heroPills = renderedSignals.slice(0, 3).map((s) => s.name || "--");
  const highlightCards = [
    { id: "orders", label: "Total orders", value: orderStats.total, delta: `${orderStats.confirmed} confirmed`, icon: "📦" },
    { id: "confirm", label: "Need confirmation", value: orderStats.needConfirmation, delta: `${orderStats.shipped} shipped`, icon: "⏳" },
    { id: "notifications", label: "Unread alerts", value: unreadNotifications, delta: "Notifications", icon: "🔔" },
    { id: "signals", label: "Top signals", value: renderedSignals.length, delta: "Supply watch", icon: "📈" },
  ];

  return (
    <div className="retailer-dashboard">
      <header className="rd-hero">
        <div className="rd-hero-copy">
          <p className="rd-kicker">Retailer workspace</p>
          <h1>Welcome back, {user?.username || "Retailer"} 👋</h1>
          <p className="rd-subtitle">
            Monitor inventory, orders, pricing, and supplier signals in one view. Use the quick
            links to jump to detailed workflows.
          </p>

          <div className="rd-cta-row">
            <Link to="/retailer/inventory" className="rd-btn primary">
              Update inventory
            </Link>
            <Link to="/retailer/orders" className="rd-btn ghost">
              View orders
            </Link>
          </div>
        </div>

        <div className="rd-hero-card">
          <div className="rd-hero-label">Today</div>
          <div className="rd-hero-metric">{orderStats.total} orders</div>
          <p className="rd-hero-note">{orderStats.needConfirmation} need confirmation</p>
          <div className="rd-hero-pills">
            {heroPills.map((pill, idx) => (
              <span key={`${pill}-${idx}`}>{pill}</span>
            ))}
          </div>
        </div>
      </header>

      <section className="rd-stats-grid">
        {highlightCards.map((card) => (
          <div key={card.id} className="rd-card">
            <div className="rd-card-top">
              <span className="rd-card-icon">{card.icon}</span>
              <span className="rd-card-delta">{card.delta}</span>
            </div>
            <div className="rd-card-value">{card.value}</div>
            <div className="rd-card-label">{card.label}</div>
          </div>
        ))}
      </section>

      

      <section className="rd-section rd-two-col">
        <div className="rd-panel">
          <div className="rd-panel-header">
            <div>
              <p className="rd-kicker">Workflow</p>
              <h3>Upcoming tasks</h3>
            </div>
            <Link to="/retailer/orders" className="rd-link-inline">
              Open orders
            </Link>
          </div>

          <div
            className="rd-list"
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
              gap: "10px",
              marginTop: "12px",
            }}
          >
            <div
              className="rd-list-item"
              style={{
                border: "1px dashed #e5e7eb",
                borderRadius: "10px",
                padding: "12px",
                textAlign: "center",
                color: "#6b7280",
                background: "#f8fafc",
              }}
            >
              No tasks right now
            </div>
          </div>
        </div>

        <div className="rd-panel">
          <div className="rd-panel-header">
            <div>
              <p className="rd-kicker">Supplier pulse</p>
              <h3>Signals</h3>
            </div>
            <Link to="/retailer/analytics" className="rd-link-inline">
              View analytics
            </Link>
          </div>

          <div className="rd-market-grid">
            {renderedSignals.map((signal) => (
              <div key={signal.id} className="rd-market-card">
                <div className="rd-market-top">
                  <span className="rd-market-name">{signal.name}</span>
                  <span className={`rd-market-change ${signal.change.startsWith("-") ? "down" : "up"}`}>
                    {signal.change}
                  </span>
                </div>
                <p className="rd-market-note">{signal.note}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
};

export default RetailerDashboard;
