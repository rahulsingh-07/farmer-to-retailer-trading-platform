import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { retailerLinks } from "../Sidebar";
import "../../css/RetailerDashboard.css";

const RetailerDashboard = () => {
  const { user } = useAuth();

  const linkDescriptions = {
    home: "Today’s overview and alerts.",
    inventory: "Manage SKUs, batches, and stock.",
    orders: "Track fulfillment and deliveries.",
    pricing: "Adjust margins and purchase bids.",
    analytics: "Sales, demand, and supplier mix.",
    suppliers: "Browse and engage farmers.",
    settings: "Store profile, payouts, and alerts.",
  };

  const highlightCards = [
    { id: "activeSkus", label: "Active SKUs", value: "148", delta: "+6 this week", icon: "📦" },
    { id: "pendingOrders", label: "Pending orders", value: "23", delta: "5 due today", icon: "🚚" },
    { id: "avgMargin", label: "Avg. margin", value: "14.8%", delta: "+0.6% vs last week", icon: "💰" },
    { id: "supplierFill", label: "Fill rate", value: "96%", delta: "Past 7 days", icon: "📈" },
  ];

  const tasks = [
    { id: "restock", title: "Restock Wheat (Batch #44)", tag: "Inventory" },
    { id: "confirm", title: "Confirm delivery window for PO-782", tag: "Orders" },
    { id: "bid", title: "Update purchase bid for Maize", tag: "Pricing" },
  ];

  const supplierSignals = [
    { id: "wheat", name: "Wheat", change: "+2.4%", note: "Higher quality lots available" },
    { id: "rice", name: "Rice", change: "-0.8%", note: "Stable prices, ample stock" },
    { id: "mustard", name: "Mustard", change: "+1.1%", note: "Rising demand in city stores" },
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
          <div className="rd-hero-metric">23 orders</div>
          <p className="rd-hero-note">5 need confirmation</p>
          <div className="rd-hero-pills">
            <span>Wheat</span>
            <span>Rice</span>
            <span>Maize</span>
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

      <section className="rd-section">
        <div className="rd-section-header">
          <div>
            <p className="rd-kicker">Navigation</p>
            <h2>Quick links</h2>
          </div>
          <span className="rd-section-hint">Same as the retailer sidebar</span>
        </div>

        <div className="rd-links-grid">
          {retailerLinks.map((link) => (
            <Link key={link.id} to={link.path} className="rd-link-card">
              <div className="rd-link-icon">{link.icon}</div>
              <div className="rd-link-copy">
                <h3>{link.label}</h3>
                <p>{linkDescriptions[link.id]}</p>
              </div>
              <span className="rd-link-arrow">→</span>
            </Link>
          ))}
        </div>
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

          <ul className="rd-list">
            {tasks.map((task) => (
              <li key={task.id} className="rd-list-item">
                <div>
                  <p className="rd-list-title">{task.title}</p>
                  <span className="rd-tag">{task.tag}</span>
                </div>
                <span className="rd-list-arrow">→</span>
              </li>
            ))}
          </ul>
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
            {supplierSignals.map((signal) => (
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
