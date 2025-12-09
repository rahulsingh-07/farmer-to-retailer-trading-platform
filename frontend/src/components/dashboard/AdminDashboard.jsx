import React, { useState, useEffect } from "react";
import { useAuth } from "../../context/AuthContext";
import { toast } from "react-toastify";
import api from "../../utils/api";
import { Link } from "react-router-dom";
import "../../css/AdminDashboard.css";

const AdminDashboard = () => {
  const { user, logout } = useAuth();
  const [pendingUsers, setPendingUsers] = useState([]);
  const [selectedFarmer, setSelectedFarmer] = useState(null);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  const [stats, setStats] = useState({
    totalPending: 0,
    totalUsers: 0,
    totalAdmins: 0,
    approvedToday: 0,
  });
  const [loading, setLoading] = useState(true);
  const [newAdminForm, setNewAdminForm] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [updating, setUpdating] = useState({});

  // Fetch data on mount
  useEffect(() => {
    fetchPendingUsers();
    fetchStats();
  }, []);

  const fetchPendingUsers = async () => {
    try {
      setLoading(true);

      const token = localStorage.getItem("token")?.trim().replace(/\s/g, "");
      const response = await fetch(
        "http://localhost:8081/admin/pendingUsers",
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const errorMessage =
          errorData.message ||
          response.statusText ||
          "Failed to fetch pending users";
        throw new Error(errorMessage);
      }

      const data = await response.json();
      setPendingUsers(data);
    } catch (err) {
      toast.error(err.message || "Failed to fetch pending users");
    } finally {
      setLoading(false);
    }
  };

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


  const updateUserStatus = async (userId, status) => {
    try {
      setUpdating((prev) => ({ ...prev, [userId]: true }));

      const token = localStorage.getItem("token")?.trim().replace(/\s/g, "");
      const response = await fetch(
        `http://localhost:8081/admin/user/${userId}/status?status=${status}`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        const errorMessage =
          errorData.message || response.statusText || "Update failed";
        throw new Error(errorMessage);
      }

      toast.success(
        status === "ACTIVE"
          ? "User approved! Password setup link sent."
          : status === "INACTIVE"
            ? "User inactive successfully"
            : `User ${status.toLowerCase()} successfully`
      );

      fetchPendingUsers();
    } catch (err) {
      toast.error(err.message || "Update failed");
    } finally {
      setUpdating((prev) => ({ ...prev, [userId]: false }));
    }
  };

  const createNewAdmin = async (e) => {
    e.preventDefault();
    try {
      await api.post("/admin/newAdmin", newAdminForm);
      toast.success("New admin created successfully");
      setNewAdminForm({ username: "", email: "", password: "" });
    } catch (err) {
      toast.error(err.message || "Failed to create new admin");
    }
  };

  if (loading) {
    return <div className="loading-screen">Loading Admin Dashboard...</div>;
  }

  return (
    <div className="admin-layout">

      {/* MAIN AREA */}
      <div className="admin-main">
        {/* Top bar */}
        <header className="admin-topbar">
          <h1 className="admin-title">Dashboard</h1>
          <div className="admin-topbar-right">
            <button className="refresh-btn" onClick={fetchPendingUsers}>
              Refresh
            </button>
            <div className="admin-profile">
              <div className="profile-avatar">
                {user?.username?.[0]?.toUpperCase() || "A"}
              </div>
              <div className="profile-info">
                <span className="profile-name">
                  {user?.username || "Admin"}
                </span>
                <span className="profile-role">Administrator</span>
              </div>
            </div>
          </div>
        </header>

        {/* CONTENT */}
        <main className="admin-content">
          {/* Stats row */}
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
            {/* <div className="stat-card">
              <div className="stat-icon">✅</div>
              <div className="stat-value">{stats.approvedToday}</div>
              <div className="stat-label">Approved Today</div>
            </div> */}
          </div>

          {/* Main content grid */}
          <div className="main-content">
            {/* Pending users */}
            <div className="table-card">
              <div className="table-header">
                <h2>Pending User Registrations</h2>
                <span className="table-count">
                  {pendingUsers.length} pending
                </span>
              </div>

              <div className="table-container">
                {pendingUsers.length === 0 ? (
                  <div className="empty-state">
                    <div className="empty-icon">🌾</div>
                    <h3>No pending registrations</h3>
                    <p>All Users are approved!</p>
                  </div>
                ) : (
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Status</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {pendingUsers.map((farmer) => (
                        <tr key={farmer.userId}>
                          <td className="farmer-name">{farmer.fullName}</td>
                          <td>{farmer.email}</td>
                          <td>{farmer.phoneNumber}</td>
                          <td>{farmer.status}</td>
                          <td className="actions-cell">
                            <button
                              className="action-btn view-btn"
                              onClick={() => {
                                setSelectedFarmer(farmer);
                                setIsViewOpen(true);
                              }}
                            >
                              👁 View
                            </button>

                           <button
                              className="action-btn approve-btn"
                              onClick={() =>
                                updateUserStatus(farmer.userId, "ACTIVE")
                              }
                              disabled={!!updating[farmer.userId]}
                            >
                              {updating[farmer.userId] ? "⏳" : "Approve"}
                            </button>
                            <button
                              className="action-btn reject-btn"
                              onClick={() =>
                                updateUserStatus(farmer.userId, "INACTIVE")
                              }
                              disabled={!!updating[farmer.userId]}
                            >
                              {updating[farmer.userId] ? "⏳" : "Reject"}
                            </button>
                          </td>

                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>

          </div>
          {isViewOpen && selectedFarmer && (
  <div className="modal-overlay" onClick={() => setIsViewOpen(false)}>
    <div
      className="modal-card"
      onClick={(e) => e.stopPropagation()} // stop close when clicking inside
    >
      <div className="modal-header">
        <h3>Farmer Details</h3>
        <button
          className="modal-close"
          onClick={() => setIsViewOpen(false)}
        >
          ✕
        </button>
      </div>

      <div className="modal-body">
        <div className="modal-field">
          <span className="field-label">Name</span>
          <span className="field-value">{selectedFarmer.fullName}</span>
        </div>
        <div className="modal-field">
          <span className="field-label">Email</span>
          <span className="field-value">{selectedFarmer.email}</span>
        </div>
        <div className="modal-field">
          <span className="field-label">Phone</span>
          <span className="field-value">{selectedFarmer.phoneNumber}</span>
        </div>
        <div className="modal-field">
          <span className="field-label">Status</span>
          <span className="field-value">{selectedFarmer.status}</span>
        </div>
        {/* add extra fields from backend if available: address, docUrl, role, etc. */}
      </div>
    </div>
  </div>
)}

        </main>
      </div>
    </div>
  );
};

export default AdminDashboard;
