import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../../context/AuthContext";
import "./RetailerDashboard.css";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";
import {
  Package,
  Clock,
  Bell
} from "lucide-react";

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

  /* =========================
     SUPPLIER SIGNALS
     ========================= */
  useEffect(() => {
    const fetchSignals = async () => {
      try {
        const response = await fetch("http://localhost:8081/public/crops");
        const data = await response.json(); // ✅ FIXED

        const raw = Array.isArray(data?.content)
          ? data.content
          : Array.isArray(data)
            ? data
            : [];

        const byCategory = raw.reduce((acc, item) => {
          const category = item.category || "Other";
          const price = Number(item.pricePerUnit || 0);

          if (!acc[category]) {
            acc[category] = { count: 0, totalPrice: 0 };
          }

          acc[category].count += 1;
          acc[category].totalPrice += Number.isFinite(price) ? price : 0;

          return acc;
        }, {});

        const signals = Object.entries(byCategory)
          .map(([category, stats]) => {
            const avgPrice =
              stats.count > 0
                ? Math.round(stats.totalPrice / stats.count)
                : 0;

            return {
              id: category.toLowerCase().replace(/\s+/g, "-"),
              name: category,
              change: `+${stats.count} lots`,
              note:
                avgPrice > 0
                  ? `Avg ₹${avgPrice} per unit`
                  : "No price data",
            };
          })
          .sort(
            (a, b) =>
              parseInt(b.change.replace("+", "")) -
              parseInt(a.change.replace("+", ""))
          )
          .slice(0, 3);

        setSupplierSignals(signals);
      } catch (err) {
        console.error("Failed to load supplier signals", err);
        setSupplierSignals([]);
      }
    };

    fetchSignals();
  }, []);

  /* =========================
     DASHBOARD STATS
     ========================= */
  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const response = await fetch(
          "http://localhost:8081/user/retailer/dashboard",
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        const result = await response.json();

        // ✅ CORRECT UNWRAP
        const stats = result?.data || {};

        const confirmed = Number(stats.confirmed) || 0;
        const shipped = Number(stats.shipped) || 0;
        const needConfirmation = Number(stats.needConfirmation) || 0;
        const notifications = Number(stats.notifications) || 0;

        setOrderStats({
          total: confirmed + shipped + needConfirmation,
          confirmed,
          shipped,
          needConfirmation,
        });

        setUnreadNotifications(notifications);
      } catch (err) {
        console.error("Failed to load dashboard summary", err);
      }
    };

    if (token) fetchDashboard();
  }, [token]);

  /* =========================
     FALLBACK UI DATA
     ========================= */
    supplierSignals.length > 0
      ? supplierSignals
      : [
        {
          id: "loading",
          name: "Loading",
          change: "+0",
          note: "Fetching live data",
        },
      ];

  const highlightCards = [
  {
    id: "orders",
    label: "Total Orders",
    value: orderStats.total,
    delta: `${orderStats.confirmed} confirmed`,
    icon: Package,
    bg: "#d9e6f7",
    color: "#2563eb"
  },
  {
    id: "confirm",
    label: "Need Confirmation",
    value: orderStats.needConfirmation,
    delta: `${orderStats.shipped} shipped`,
    icon: Clock,
    bg: "#fff0de",
    color: "#f97316"
  },
  {
    id: "notifications",
    label: "Unread Alerts",
    value: unreadNotifications,
    delta: "Notifications",
    icon: Bell,
    bg: "#f9f5d0",
    color: "#ca8a04"
  }
];


  const orderStatusChartData = [
    { status: "Confirmed", count: orderStats.confirmed },
    { status: "Shipped", count: orderStats.shipped },
    { status: "Pending", count: orderStats.needConfirmation },
  ];

  const orderDistributionData = [
    { name: "Confirmed", value: orderStats.confirmed },
    { name: "Shipped", value: orderStats.shipped },
    { name: "Pending", value: orderStats.needConfirmation },
  ];

  const CHART_COLORS = ["#22c55e", "#0ea5e9", "#f59e0b"];



  /* =========================
     RENDER
     ========================= */
  return (
    <div className="retailer-dashboard">
      <header className="rd-hero">
        <div className="rd-hero-copy">
          <p className="rd-kicker">Retailer workspace</p>
          <h1>Welcome back, {user?.username || "Retailer"} 👋</h1>
          <p className="rd-subtitle">
            Monitor inventory, orders, pricing, and supplier signals in one view.
          </p>

          <div className="rd-cta-row">
            <Link to="/retailer/inventory" className="rd-btn primary">
            Inventory
            </Link>
            <Link to="/retailer/orders" className="rd-btn ghost">
              View orders
            </Link>
          </div>
        </div>
      </header>

      <section className="rd-stats-grid">
  {highlightCards.map(card => {
    const Icon = card.icon;

    return (
      <div
        key={card.id}
        className="rd-card"
        style={{ backgroundColor: card.bg }}
      >
        <div className="rd-card-top">
          <Icon size={26} color={card.color} />
          <span className="rd-card-delta">{card.delta}</span>
        </div>

        <div
          className="rd-card-value"
          style={{ color: card.color }}
        >
          {card.value}
        </div>

        <div className="rd-card-label">
          {card.label}
        </div>
      </div>
    );
  })}
</section>

      <section className="rd-section rd-two-col">

        {/* BAR CHART */}
        <div className="rd-panel">
          <div className="rd-panel-header">
            <h3>Order status overview</h3>
            <p>Compare current order states</p>
          </div>

          <div className="rd-chart">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={orderStatusChartData}>
                <XAxis dataKey="status" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar
                  dataKey="count"
                  radius={[8, 8, 0, 0]}
                  fill="#0ea5e9"
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* PIE CHART */}
        <div className="rd-panel">
          <div className="rd-panel-header">
            <h3>Order distribution</h3>
            <p>How your orders are split</p>
          </div>

          <div className="rd-chart">
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie
                  data={orderDistributionData}
                  dataKey="value"
                  nameKey="name"
                  innerRadius={55}
                  outerRadius={90}
                  paddingAngle={4}
                  label
                >
                  {orderDistributionData.map((_, index) => (
                    <Cell
                      key={index}
                      fill={CHART_COLORS[index % CHART_COLORS.length]}
                    />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

      </section>


    </div>
  );
};

export default RetailerDashboard;
