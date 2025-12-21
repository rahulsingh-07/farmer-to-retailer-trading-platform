import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import "../../css/PendingUsersTable.css";

const PendingUsersTable = () => {
  // Independent state
  const [pendingUsers, setPendingUsers] = useState([]);
  const [pagination, setPagination] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    size: 10,
  });
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState({});
  const [selectedUser, setSelectedUser] = useState(null);
  const [isViewOpen, setIsViewOpen] = useState(false);

  // Fetch pending users
  const fetchPendingUsers = async (page = 0, size = 10) => {
    try {
      setLoading(true);
      const token = localStorage.getItem("token");

      const response = await fetch(
        `http://localhost:8081/admin/pendingUsers?page=${page}&size=${size}`,
        {
          method: "GET",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      if (!response.ok) {
        throw new Error("Failed to fetch pending users");
      }

      const data = await response.json();
      setPendingUsers(data.content || []);
      setPagination({
        currentPage: data.number || 0,
        totalPages: data.totalPages || 0,
        totalElements: data.totalElements || 0,
        size: data.size || size,
      });
    } catch (error) {
      console.error("Error fetching pending users:", error);
      toast.error("Failed to load users");
    } finally {
      setLoading(false);
    }
  };

  // Initial load
  useEffect(() => {
    fetchPendingUsers(0, 10);
  }, []);

  const handlePageChange = (page) => {
    fetchPendingUsers(page, pagination.size);
  };

  const handleSizeChange = (size) => {
    fetchPendingUsers(0, size);
  };

  const handleRefresh = () => {
    fetchPendingUsers(pagination.currentPage, pagination.size);
  };

  const handleUpdateStatus = async (userId, status) => {
    try {
      setUpdating((prev) => ({ ...prev, [userId]: true }));
      const token = localStorage.getItem("token");
      
      await fetch(
        `http://localhost:8081/admin/user/${userId}/status?status=${status}`,
        {
          method: "PATCH",
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      toast.success(
        status === "ACTIVE" 
          ? "User approved! Password setup link sent." 
          : "User rejected successfully"
      );
      
      // Refresh current page after update
      fetchPendingUsers(pagination.currentPage, pagination.size);
    } catch (error) {
      toast.error(error.message || "Request rejected");
      fetchPendingUsers(pagination.currentPage, pagination.size);
    } finally {
      setUpdating((prev) => ({ ...prev, [userId]: false }));
    }
  };

  const handleViewUser = (user) => {
    setSelectedUser(user);
    setIsViewOpen(true);
  };

  const closeModal = () => {
    setIsViewOpen(false);
    setSelectedUser(null);
  };

  const handleOverlayKeyDown = (event) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      closeModal();
    }
  };

  let tableContent;
  if (loading) {
    tableContent = <div className="loading-state">Loading users...</div>;
  } else if (pendingUsers.length === 0) {
    tableContent = (
      <div className="empty-state">
        <div className="empty-icon">🌾</div>
        <h3>No pending registrations</h3>
        <p>All Users are approved!</p>
      </div>
    );
  } else {
    tableContent = (
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
          {pendingUsers.map((user) => (
            <tr key={user.userId}>
              <td className="farmer-name">{user.fullName}</td>
              <td>{user.email}</td>
              <td>{user.phoneNumber}</td>
              <td>{user.status}</td>
              <td className="actions-cell">
                <button
                  className="action-btn view-btn"
                  onClick={() => handleViewUser(user)}
                  disabled={!!updating[user.userId]}
                >
                  👁 View
                </button>
                <button
                  className="action-btn approve-btn"
                  onClick={() => handleUpdateStatus(user.userId, "ACTIVE")}
                  disabled={!!updating[user.userId]}
                >
                  {updating[user.userId] ? "⏳" : "✅ Approve"}
                </button>
                <button
                  className="action-btn reject-btn"
                  onClick={() => handleUpdateStatus(user.userId, "INACTIVE")}
                  disabled={!!updating[user.userId]}
                >
                  {updating[user.userId] ? "⏳" : "❌ Reject"}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }

  return (
    <>
      <div className="pending-card">
        <div className="table-header">
          <div className="table-title-row">
            <div>
              <p className="table-kicker">Registrations</p>
              <h2>Pending Users</h2>
            </div>

            <div className="table-actions">
              <label className="page-size">
                <span className="page-size-label">Page size</span>
                <select
                  value={pagination.size}
                  onChange={(e) => handleSizeChange(Number.parseInt(e.target.value, 10))}
                  disabled={loading}
                >
                  <option value={5}>5</option>
                  <option value={10}>10</option>
                  <option value={20}>20</option>
                  <option value={50}>50</option>
                </select>
              </label>

              <button
                className="refresh-btn"
                onClick={handleRefresh}
                disabled={loading}
              >
                {loading ? "Refreshing..." : "Refresh"}
              </button>
            </div>
          </div>

          <div className="table-meta">
            <span>{pagination.totalElements} total</span>
            <span className="table-page-meta">
              Page {pagination.currentPage + 1} of {Math.max(pagination.totalPages, 1)}
            </span>
          </div>

          <div className="pagination-controls">
            <button
              className="ghost-btn"
              onClick={() => handlePageChange(0)}
              disabled={pagination.currentPage === 0 || loading}
            >
              ⏮ First
            </button>
            <button
              className="ghost-btn"
              onClick={() => handlePageChange(pagination.currentPage - 1)}
              disabled={pagination.currentPage === 0 || loading}
            >
              ← Prev
            </button>
            <button
              className="ghost-btn"
              onClick={() => handlePageChange(pagination.currentPage + 1)}
              disabled={pagination.currentPage >= pagination.totalPages - 1 || loading}
            >
              Next →
            </button>
            <button
              className="ghost-btn"
              onClick={() => handlePageChange(Math.max(pagination.totalPages - 1, 0))}
              disabled={pagination.currentPage >= pagination.totalPages - 1 || loading}
            >
              Last ⏭
            </button>
          </div>
        </div>

        <div className="table-container">
          {tableContent}
        </div>
      </div>

      {/* Modal - Independent */}
      {isViewOpen && selectedUser && (
        <div className="modal-overlay-wrapper">
          <button
            type="button"
            className="modal-overlay"
            aria-label="Close dialog"
            onClick={closeModal}
            onKeyDown={handleOverlayKeyDown}
          />
          <div className="modal-card">
            <div className="modal-header">
              <h3>User Details</h3>
              <button className="modal-close" onClick={closeModal}>✕</button>
            </div>
            <div className="modal-body">
              <div className="modal-field">
                <span className="field-label">Name</span>
                <span className="field-value">{selectedUser.fullName}</span>
              </div>
              <div className="modal-field">
                <span className="field-label">Email</span>
                <span className="field-value">{selectedUser.email}</span>
              </div>
              <div className="modal-field">
                <span className="field-label">Phone</span>
                <span className="field-value">{selectedUser.phoneNumber}</span>
              </div>
              <div className="modal-field">
                <span className="field-label">Status</span>
                <span className="field-value">{selectedUser.status}</span>
              </div>
              {selectedUser.address && (
                <div className="modal-field">
                  <span className="field-label">Address</span>
                  <span className="field-value">{selectedUser.address}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default PendingUsersTable;
