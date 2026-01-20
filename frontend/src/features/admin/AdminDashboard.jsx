import React, { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from "recharts";
import {
  Hourglass,
  Users,
  Shield,
  BadgeCheck
} from "lucide-react";

import { getAdminStats } from "./adminService";
import "./AdminDashboard.css";

const COLORS = ["#f59e0b", "#3b82f6", "#22c55e", "#ef4444"];

const AdminDashboard = () => {
  const { user } = useAuth();

  const [stats, setStats] = useState({
    totalPendingUsers: 0,
    totalUsers: 0,
    totalApproved: 0,
    totalAdmin: 0,
  });

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await getAdminStats();
      const data = response.data;

      setStats({
        totalPendingUsers: data.totalPendingUsers ?? 0,
        totalUsers: data.totalUsers ?? 0,
        totalAdmin: data.totalAdmin ?? 0,
        totalApproved: data.totalApproved ?? 0,
      });
    } catch (error) {
      console.error("Error fetching stats:", error.message);
    }
  };

  const adminCards = [
  {
    id: 1,
    label: "Pending Users",
    value: stats.totalPendingUsers,
    icon: Hourglass,
    bg: "#fff7ed",
    color: "#f97316"
  },
  {
    id: 2,
    label: "Total Users",
    value: stats.totalUsers,
    icon: Users,
    bg: "#eff6ff",
    color: "#2563eb"
  },
  {
    id: 3,
    label: "Admins",
    value: stats.totalAdmin,
    icon: Shield,
    bg: "#fefce8",
    color: "#ca8a04"
  },
  {
    id: 4,
    label: "Approved by You",
    value: stats.totalApproved,
    icon: BadgeCheck,
    bg: "#ecfdf5",
    color: "#16a34a"
  }
];


  /* ================= CHART DATA ================= */
  const barData = [
    { name: "Users", value: stats.totalUsers },
    { name: "Admins", value: stats.totalAdmin },
    { name: "Approved", value: stats.totalApproved },
  ];

  const pieData = [
    { name: "Pending", value: stats.totalPendingUsers },
    { name: "Approved", value: stats.totalApproved },
  ];

  return (
    <div className="admin-dashboard">
      {/* ================= HERO ================= */}
      <header className="ad-hero">
        <div className="ad-hero-copy">
          <p className="ad-kicker">Admin workspace</p>
          <h1>Welcome, {user?.username || "Admin"} 👋</h1>
          <p className="ad-subtitle">
            Monitor platform users, approvals, and system health from one place.
          </p>
        </div>
      </header>

      {/* ================= HERO CARDS ================= */}
      <section className="ad-stats-grid">
  {adminCards.map(card => {
    const Icon = card.icon;

    return (
      <div
        key={card.id}
        className="ad-card"
        style={{ backgroundColor: card.bg }}
      >
        <div className="ad-card-top">
          <Icon size={28} color={card.color} />
        </div>

        <div
          className="ad-card-value"
          style={{ color: card.color }}
        >
          {card.value}
        </div>

        <div className="ad-card-label">
          {card.label}
        </div>
      </div>
    );
  })}
</section>


      {/* ================= CHARTS ================= */}
      <section className="ad-charts">
        <div className="ad-chart-card">
          <h3 className="ad-chart-title">User Overview</h3>
          <div className="ad-chart-wrapper">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={barData}>
                <XAxis dataKey="name" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#f4d212ff"  radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        <div className="ad-chart-card">
          <h3 className="ad-chart-title">Approval Distribution</h3>
          <div className="ad-chart-wrapper">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  outerRadius={90}
                  label
                >
                  {pieData.map((_, index) => (
                    <Cell key={index} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Legend />
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </section>

    </div>
  );
};

export default AdminDashboard;
