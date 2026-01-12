import React, { useState, useEffect } from "react";
import { toast } from "react-toastify";
import "./PendingUsersTable.css";
import { getPendingUsers, updateUserStatus } from "./adminService";
import Pagination from "../../components/common/Pagination";
import { User, Mail, Phone, Workflow} from "lucide-react";
const PendingUsersTable = () => {
  // ===== STATE =====
  const [pendingUsers, setPendingUsers] = useState([]);

  const [filters, setFilters] = useState({
    page: 0,
    size: 5,
  });

  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState({});
  const [selectedUser, setSelectedUser] = useState(null);
  const [isViewOpen, setIsViewOpen] = useState(false);

  // ===== FETCH =====
  const fetchPendingUsers = async (page = filters.page, size = filters.size) => {
    try {
      setLoading(true);

      const response = await getPendingUsers(page, size);
      const pageData = response.data;

      setPendingUsers(pageData.content || []);
      setFilters((prev) => ({
      ...prev,
      page: pageData.number,
    }));
      setTotalPages(pageData.totalPages);
      setTotalElements(pageData.totalElements);
    } catch (error) {
      console.error(error);
      toast.error(error.message || "Failed to load pending users");
    } finally {
      setLoading(false);
    }
  };

  // ===== INITIAL LOAD =====
  useEffect(() => {
    fetchPendingUsers(0, filters.size);
  }, []);

  useEffect(() => {
    if (isViewOpen) {
      document.body.classList.add("modal-open");
    } else {
      document.body.classList.remove("modal-open");
    }

    return () => {
      document.body.classList.remove("modal-open");
    };
  }, [isViewOpen]);

  // ===== HANDLERS =====
  const handlePageChange = (page) => {
    fetchPendingUsers(page, filters.size);
  };

  const handleSizeChange = (size) => {
  setFilters({
    page: 0,
    size: size,
  });

  fetchPendingUsers(0, size);
};


  const handleRefresh = () => {
    fetchPendingUsers(filters.page, filters.size);
  };

  const handleUpdateStatus = async (userId, status) => {
    try {
      setUpdating((prev) => ({ ...prev, [userId]: true }));

      await updateUserStatus(userId, status);

      toast.success(
        status === "INACTIVE"
          ? "User approved! Password setup link sent."
          : "User rejected successfully"
      );

      fetchPendingUsers(filters.page, filters.size);
    } catch (error) {
      toast.error(error.message || "Request failed");
      fetchPendingUsers(filters.page, filters.size);
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

  // ===== TABLE CONTENT =====
  let tableContent;

  if (loading) {
    tableContent = <div className="loading-state">Loading users...</div>;
  } else if (pendingUsers.length === 0) {
    tableContent = (
      <div className="empty-state">
        <div className="empty-icon">🌾</div>
        <h3>No pending registrations</h3>
        <p>All users are approved</p>
      </div>
    );
  } else {
    tableContent = (
      <table className="admin-table">
        <thead>
          <tr>
            <th><User /> Name</th>
            <th><Mail /> Email</th>
            <th><Phone /> Phone</th>
            <th><Workflow />Status</th>
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
                  onClick={() =>
                    handleUpdateStatus(user.userId, "INACTIVE")
                  }
                  disabled={!!updating[user.userId]}
                >
                  {updating[user.userId] ? "⏳" : "✅ Approve"}
                </button>

                <button
                  className="action-btn reject-btn"
                  onClick={() =>
                    handleUpdateStatus(user.userId, "REJECTED")
                  }
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

  // ===== RENDER =====
  return (
    <>
      <div className="pending-card">
        <div className="table-header">
          <div>
            <h2>Pending Users</h2>
          </div>

          <div className="table-actions">
            <label>
              Page size:
              <select
                value={filters.size}
                onChange={(e) =>
                  handleSizeChange(Number(e.target.value))
                }
                disabled={loading}
              >
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={20}>20</option>
                <option value={50}>50</option>
              </select>
            </label>

            <button onClick={handleRefresh} disabled={loading}>
              Refresh
            </button>
          </div>
        </div>

        <div className="table-container">{tableContent}</div>

        <Pagination
          currentPage={filters.page}
          totalPages={totalPages}
          totalElements={totalElements}
          pageSize={filters.size}
          onPageChange={handlePageChange}
          loading={loading}
        />
      </div>

      {/* ===== MODAL ===== */}
      {isViewOpen && selectedUser && (
        <div className="modal-overlay-wrapper">
          {/* Overlay */}
          <div
            className="modal-overlay"
            onClick={closeModal}
            aria-hidden="true"
          />

          {/* Modal */}
          <div
            className="modal-card"
            role="dialog"
            aria-modal="true"
            onClick={(e) => e.stopPropagation()} // 🔴 CRITICAL
          >
            <div className="modal-header">
              <h3>User Details</h3>
              <button className="modal-close" onClick={closeModal}>
                ✕
              </button>
            </div>

            <div className="modal-body">
              <p><b>Name:</b> {selectedUser.fullName}</p>
              <p><b>Email:</b> {selectedUser.email}</p>
              <p><b>Phone:</b> {selectedUser.phoneNumber}</p>
              <p><b>Role:</b> {selectedUser.role}</p>
              <p><b>Status:</b> {selectedUser.status}</p>

              {selectedUser.role === "RETAILER" && selectedUser.tradeLicenseUrl && (
                <p>
                  <b>Trade License:</b>{" "}
                  <a
                    href={selectedUser.tradeLicenseUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="license-link"
                  >
                    View Document
                  </a>
                </p>
              )}

              {selectedUser.role === "FARMER" && selectedUser.pmKisanId && (
                <p><b>PM Kisan ID:</b> {selectedUser.pmKisanId}</p>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default PendingUsersTable;
