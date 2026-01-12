import React, { useEffect, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from "recharts";
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
        <div className="ad-card">
          <span className="ad-card-icon">⏳</span>
          <div className="ad-card-value">{stats.totalPendingUsers}</div>
          <div className="ad-card-label">Pending Users</div>
        </div>

        <div className="ad-card">
          <span className="ad-card-icon">👥</span>
          <div className="ad-card-value">{stats.totalUsers}</div>
          <div className="ad-card-label">Total Users</div>
        </div>

        <div className="ad-card">
          <span className="ad-card-icon">👨‍💼</span>
          <div className="ad-card-value">{stats.totalAdmin}</div>
          <div className="ad-card-label">Admins</div>
        </div>

        <div className="ad-card">
          <span className="ad-card-icon">✅</span>
          <div className="ad-card-value">{stats.totalApproved}</div>
          <div className="ad-card-label">Approved by You</div>
        </div>
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
