import React, { useState, useEffect } from "react";
import { useAuth } from "../../context/AuthContext";
import "../../css/AdminDashboard.css";
import PendingUsersTable from "../../pages/admin/PendingUsersTable";

const AdminDashboard = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalPending: 0,
    totalUsers: 0,
    totalAdmins: 0,
  });

  // Fetch only stats on mount
  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const token = localStorage.getItem("token");
      const response = await fetch("http://localhost:8081/admin/totalUsers", {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error("Failed to fetch stats");
      }

      const data = await response.json();
      setStats({
        totalPending: data.totalPending,
        totalUsers: data.totalUsers,
        totalAdmins: data.totalAdmin,
      });
    } catch (error) {
      console.error("Error fetching stats:", error);
    }
  };

  return (
    <div className="admin-layout">
      <div className="admin-main">
        <header className="admin-topbar">
          <h1 className="admin-title">Dashboard</h1>
          <div className="admin-topbar-right">
            <div className="admin-profile">
              <div className="profile-avatar">
                {user?.username?.[0]?.toUpperCase() || "A"}
              </div>
              <div className="profile-info">
                <span className="profile-name">{user?.username || "Admin"}</span>
                <span className="profile-role">Administrator</span>
              </div>
            </div>
          </div>
        </header>

        <main className="admin-content">
          {/* Stats Cards */}
          <div className="stats-row">
            <div className="stat-card">
              <div className="stat-icon">⏳</div>
              <div className="stat-value">{stats.totalPending}</div>
              <div className="stat-label">Pending Users</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">👥</div>
              <div className="stat-value">{stats.totalUsers}</div>
              <div className="stat-label">Total Users</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">👨‍💼</div>
              <div className="stat-value">{stats.totalAdmins}</div>
              <div className="stat-label">Admins</div>
            </div>
          </div>

          {/* Table - Completely Independent */}
          <div className="main-content">
            <PendingUsersTable />
          </div>
        </main>
      </div>
    </div>
  );
};

export default AdminDashboard;
