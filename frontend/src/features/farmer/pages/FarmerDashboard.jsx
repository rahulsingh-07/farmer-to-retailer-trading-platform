import { useEffect, useState } from "react";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend
} from "recharts";
import { Link } from "react-router-dom";
import { useAuth } from "../../../context/AuthContext";
import { Clock, CreditCard, Truck, CheckCircle } from "lucide-react";
import { getFarmerStats } from "../farmerService";
import "./FarmerDashboard.css";

const COLORS = ["#22c55e", "#f59e0b", "#3b82f6", "#ef4444"];

export default function FarmerDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();
  useEffect(() => {
    async function load() {
      try {
        const data = await getFarmerStats();
        setStats(data);
      } catch (e) {
        console.error("Failed to load farmer dashboard", e);
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  if (loading) return <div className="dashboard-loading">Loading...</div>;
  if (!stats) return <div className="dashboard-loading">No data</div>;

  /* ================= HERO METRICS ================= */
  const heroDeliveries = Number(stats.totalShippedOrders) || 0;
  const heroPending = Number(stats.totalPendingOrders) || 0;

  /* ================= WORKFLOW CARDS ================= */
  const workflowCards = [
  {
    id: 1,
    label: "Pending Orders",
    value: stats.totalPendingOrders,
    link: "/farmer/orders?status=PENDING",
    icon: Clock,
    bg: "#f5d7b2",
    color: "#f97316"
  },
  {
    id: 2,
    label: "Waiting Payment",
    value: stats.totalWaitingPayment,
    link: "/farmer/orders?status=WAITING_PAYMENT",
    icon: CreditCard,
    bg: "#f6f2c9",
    color: "#ca8a04"
  },
  {
    id: 3,
    label: "Shipped",
    value: stats.totalShippedOrders,
    link: "/farmer/orders?status=SHIPPED",
    icon: Truck,
    bg: "#e2ecfa",
    color: "#2563eb"
  },
  {
    id: 4,
    label: "Completed",
    value: stats.totalCompletedDelivery,
    link: "/farmer/orders?status=COMPLETED",
    icon: CheckCircle,
    bg: "#e3fdf1",
    color: "#16a34a"
  }
];


  /* ================= CHART DATA ================= */
  const barData = [
    { name: "Total Crops", value: stats.totalCrops },
    { name: "Active Auctions", value: stats.totalActiveAuction },
    { name: "Active Orders", value: stats.totalActiveOrders }
  ];

  const pieData = [
    { name: "Pending", value: stats.totalPendingOrders },
    { name: "Waiting Payment", value: stats.totalWaitingPayment },
    { name: "Shipped", value: stats.totalShippedOrders },
    { name: "Completed", value: stats.totalCompletedDelivery }
  ];

  return (
    <div className="farmer-dashboard">

      {/* ================= HERO SECTION ================= */}
      <header className="fd-hero">
        <div className="fd-hero-copy">
          <p className="fd-kicker">Farmer workspace</p>
          <h1>Welcome, {user?.username || "Farmer"} 👋</h1>
          <p className="fd-subtitle">
            Monitor crops, auctions, and deliveries in real time.
          </p>

          <div className="fd-cta-row">
            <Link to="/farmer/crops" className="fd-btn primary">
              My Crops
            </Link>
            <Link to="/farmer/addCrop" className="fd-btn ghost">
              Add Crop
            </Link>
          </div>
        </div>
      </header>

      {/* ================= WORKFLOW CARDS ================= */}
      <section className="fd-stats-grid">
        {workflowCards.map(card => (
          <Link key={card.id} to={card.link} className="fd-card" style={{ backgroundColor: card.bg, color: card.color }}> 
            <div>
            <div className="fd-card-label">{card.label}</div>
            <div className="fd-card-value">{card.value}</div>
            </div>
            <card.icon size={24} />
          </Link>
        ))}
      </section>

      {/* ================= CHARTS ================= */}
      <section className="dashboard-container">
        <div className="dashboard-grid">

          {/* BAR CHART */}
          <div className="dashboard-card">
            <h2 className="dashboard-title">Farm Overview</h2>
            <ResponsiveContainer width="100%" height={280} >
              <BarChart data={barData}>
                <XAxis dataKey="name" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#f4d212ff" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* PIE CHART */}
          <div className="dashboard-card">
            <h2 className="dashboard-title">Order Distribution</h2>
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  outerRadius={90}
                  label
                >
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
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
}
