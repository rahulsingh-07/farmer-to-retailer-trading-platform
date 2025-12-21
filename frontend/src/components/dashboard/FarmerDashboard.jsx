import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FiLayers, FiTruck, FiTrendingUp, FiActivity } from "react-icons/fi";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/FarmerDashboard.css";

const FarmerDashboard = () => {
  const { user, token } = useAuth();

  const [cropStats, setCropStats] = useState({
    totalCrops: 0,
    totalActiveAuction: 0,
    totalPendingOrders: 0,
    totalWaitingPayment: 0,
    totalShippedOrders: 0,
    totalCompletedDelivery: 0,
  });
  const [marketSignals, setMarketSignals] = useState([]);

  useEffect(() => {
    const fetchStats = async () => {
      if (!token) return;
      try {
        const data = await api.get("/farmer/totalCrops", token);
        setCropStats({
          totalCrops: data?.totalCrops ?? 0,
          totalActiveOrders: data?.totalActiveOrders ?? 0,
          totalActiveAuction: data?.totalActiveAuction ?? 0,
          totalPendingOrders: data?.totalPendingOrders ?? 0,
          totalWaitingPayment: data?.totalWaitingPayment ?? 0,
          totalShippedOrders: data?.totalShippedOrders ?? 0,
          totalCompletedDelivery: data?.totalCompletedDelivery ?? 0,
        });
      } catch (err) {
        console.error("Failed to load crop stats", err);
      }
    };

    fetchStats();
  }, [token]);

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
              crop: cat,
              change: `+${stats.count} lots`,
              note: avg > 0 ? `Avg ₹${avg} per unit` : "No price data",
            };
          })
          .sort((a, b) => Number.parseInt(b.change, 10) - Number.parseInt(a.change, 10))
          .slice(0, 3);

        setMarketSignals(signals.length ? signals : []);
      } catch (err) {
        console.error("Failed to load market signals", err);
        setMarketSignals([]);
      }
    };

    fetchSignals();
  }, []);

  const highlightCards = [
    { id: "totalCrops", label: "Total crops", value: cropStats.totalCrops, delta: "Synced from server", icon: <FiLayers /> },
    { id: "activeAuctions", label: "Active auctions", value: cropStats.totalActiveAuction, delta: "Live right now", icon: <FiTrendingUp /> },
    { id: "totalOrders", label: "Total Orders", value: cropStats.totalActiveOrders, delta: "last week", icon: <FiTruck /> },
    { id: "pendingOrders", label: "Pending Orders", value: cropStats.totalPendingOrders, delta: "Today", icon: <FiActivity /> },
  ];

  const workflowStats = [
    { id: "waitingPayment", label: "Waiting for payment", value: cropStats.totalWaitingPayment ?? 0 },
    { id: "shipped", label: "Shipped", value: cropStats.totalShippedOrders ?? 0 },
    { id: "delivered", label: "Completed delivery", value: cropStats.totalCompletedDelivery ?? 0 },
  ];

  let renderedSignals = marketSignals;
  if (!marketSignals.length) {
    renderedSignals = [{ id: "fallback-1", crop: "Loading", change: "+0", note: "Fetching live data" }];
  }

  const heroDeliveries = (cropStats.totalShippedOrders ?? 0) + (cropStats.totalCompletedDelivery ?? 0);
  const heroSlotConfirm = cropStats.totalPendingOrders ?? 0;
  const heroPills = renderedSignals.slice(0, 3).map((s) => s.crop || "--");

  return (
    <div className="farmer-dashboard">
      <header className="fd-hero">
        <div className="fd-hero-copy">
          <p className="fd-kicker">Farmer workspace</p>
          <h1>Hey, {user?.username || "Farmer"} 👋</h1>
          <p className="fd-subtitle">
            Track your crops, orders, pricing, and analytics in one place. Jump to any
            section with the quick links below.
          </p>

          <div className="fd-cta-row">
            <Link to="/farmer/myCrops" className="fd-btn primary">
              My crops
            </Link>
            <Link to="/farmer/addCrops" className="fd-btn ghost">
              Add crop
            </Link>
          </div>
        </div>

        <div className="fd-hero-card">
          <div className="fd-hero-label">Today</div>
          <div className="fd-hero-metric">{heroDeliveries} deliveries</div>
          <p className="fd-hero-note">{heroSlotConfirm} need slot confirmation</p>
          <div className="fd-hero-pills">
            {heroPills.map((pill, idx) => (
              <span key={`${pill}-${idx}`}>{pill}</span>
            ))}
          </div>
        </div>
      </header>

      <section className="fd-stats-grid">
        {highlightCards.map((card) => (
          <div key={card.id} className="fd-card">
            <div className="fd-card-top">
              <span className="fd-card-icon">{card.icon}</span>
              <span className="fd-card-delta">{card.delta}</span>
            </div>
            <div className="fd-card-value">{card.value}</div>
            <div className="fd-card-label">{card.label}</div>
          </div>
        ))}
      </section>

      <section className="fd-section fd-two-col">
        <div className="fd-panel">
          <div className="fd-panel-header">
            <div>
              <p className="fd-kicker">Workflow</p>
              <h3>Upcoming tasks</h3>
            </div>
            <Link to="/farmer/orders" className="fd-link-inline">
              View orders
            </Link>
          </div>

          <div
            className="fd-mini-stats"
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))",
              gap: "12px",
              marginTop: "12px",
            }}
          >
            {workflowStats.map((stat) => (
              <div
                key={stat.id}
                className="fd-mini-stat"
                style={{
                  border: "1px solid #e5e7eb",
                  background: "#ffffff",
                  borderRadius: "10px",
                  padding: "12px",
                  display: "flex",
                  flexDirection: "column",
                  gap: "4px",
                  boxShadow: "0 4px 10px rgba(15, 23, 42, 0.04)",
                }}
              >
                <span style={{ fontSize: "12px", color: "#6b7280" }}>{stat.label}</span>
                <span style={{ fontSize: "22px", fontWeight: 700 }}>{stat.value}</span>
              </div>
            ))}
          </div>

          <div
            className="fd-list"
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
              gap: "10px",
              marginTop: "12px",
            }}
          >
            <div
              className="fd-list-item"
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

        <div className="fd-panel">
          <div className="fd-panel-header">
            <div>
              <p className="fd-kicker">Market pulse</p>
              <h3>Signals</h3>
            </div>
            <Link to="/farmer/analytics" className="fd-link-inline">
              Open analytics
            </Link>
          </div>

          <div className="fd-market-grid">
            {renderedSignals.map((signal) => (
              <div key={signal.id} className="fd-market-card">
                <div className="fd-market-top">
                  <span className="fd-market-crop">{signal.crop}</span>
                  <span className={`fd-market-change ${signal.change.startsWith("-") ? "down" : "up"}`}>
                    {signal.change}
                  </span>
                </div>
                <p className="fd-market-note">{signal.note}</p>
              </div>
            ))}
          </div>
        </div>
      </section>
    </div>
  );
};

export default FarmerDashboard;
