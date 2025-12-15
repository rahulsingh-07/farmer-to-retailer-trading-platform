import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { farmerLinks } from "../Sidebar";
import "../../css/FarmerDashboard.css";

const FarmerDashboard = () => {
  const { user } = useAuth();

  const linkDescriptions = {
    home: "Recent alerts, payouts, and messages.",
    crops: "Add, edit, and track your listed crops.",
    orders: "Fulfill deliveries and update statuses.",
    pricing: "Adjust price floors and bids in one place.",
    analytics: "Yield, demand, and sales performance.",
    marketplace: "Browse buyer demand and bids.",
    settings: "Profile, payouts, and notifications.",
  };

  const highlightCards = [
    { id: "activeCrops", label: "Active crops", value: "12", delta: "+3 this week", icon: "🌱" },
    { id: "openOrders", label: "Open orders", value: "8", delta: "2 need action", icon: "🚚" },
    { id: "avgPrice", label: "Avg. selling price", value: "$212/qtl", delta: "+4.1% vs last week", icon: "💹" },
    { id: "marketVisits", label: "Marketplace visits", value: "142", delta: "Today", icon: "🛰" },
  ];

  const tasks = [
    { id: "verify", title: "Confirm delivery slot for Order #1042", tag: "Today" },
    { id: "price", title: "Update minimum price for Wheat Lot #22", tag: "Pricing" },
    { id: "analytics", title: "Review demand trends for Maize", tag: "Analytics" },
  ];

  const marketSignals = [
    { id: "wheat", crop: "Wheat", change: "+3.2%", note: "Higher bids in Jaipur mandi" },
    { id: "rice", crop: "Rice", change: "-1.1%", note: "Stable demand, watch moisture" },
    { id: "maize", crop: "Maize", change: "+0.8%", note: "Retailers adding new lots" },
  ];

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
          <div className="fd-hero-metric">8 deliveries</div>
          <p className="fd-hero-note">2 need slot confirmation</p>
          <div className="fd-hero-pills">
            <span>Wheat</span>
            <span>Maize</span>
            <span>Mustard</span>
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

          <ul className="fd-list">
            {tasks.map((task) => (
              <li key={task.id} className="fd-list-item">
                <div>
                  <p className="fd-list-title">{task.title}</p>
                  <span className="fd-tag">{task.tag}</span>
                </div>
                <span className="fd-list-arrow">→</span>
              </li>
            ))}
          </ul>
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
            {marketSignals.map((signal) => (
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
