import React, { useEffect, useMemo, useState } from "react";
import { toast } from "react-toastify";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/Notifications.css";

const statusOptions = [
  { value: "ALL", label: "All" },
  { value: "READ", label: "Read" },
  { value: "UNREAD", label: "Unread" },
];

const Notifications = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const basePath = "/farmer/notifications";

  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [statusOpen, setStatusOpen] = useState(false);
  const [actionOpen, setActionOpen] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const data = await api.get("/user/notifications", token);
      setNotifications(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      toast.error(err.message || "Failed to load notifications");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const filtered = useMemo(() => {
    return notifications.filter((n) => {
      if (statusFilter === "READ") return !!n.read;
      if (statusFilter === "UNREAD") return !n.read;
      return true;
    });
  }, [notifications, statusFilter]);

  const toggleSelect = (id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });
  };

  const deleteSelected = async () => {
    if (selectedIds.size === 0) return;
    const ids = Array.from(selectedIds);
    const query = ids.map((id) => `ids=${encodeURIComponent(id)}`).join("&");
    setLoading(true);
    try {
      const res = await api.delete(`/user/notifications/delete?${query}`, token);
      const message = res?.message || "Deleted successfully";
      setNotifications((prev) => prev.filter((n) => !selectedIds.has(n.id)));
      setSelectedIds(new Set());
      toast.success(message);
    } catch (err) {
      toast.error(err.message || "Delete failed");
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (iso) => {
    if (!iso) return "";
    const date = new Date(iso);
    return date.toLocaleString();
  };

  return (
    <div className="notifications-page">
      {/* Header */}
      <div className="notif-header">
        <h2 className="notif-title">Farmer Notifications</h2>

        <div className="notif-header-right">
          {/* Actions dropdown */}
          <div className="notif-dropdowns">
            <div className="dropdown">
              <button
                className="dropdown-btn"
                onClick={() => {
                  setActionOpen((v) => !v);
                  setStatusOpen(false);
                }}
              >
                Actions
              </button>
              {actionOpen && (
                <div className="dropdown-menu">
                  <button onClick={deleteSelected}>Delete Marked</button>
                </div>
              )}
            </div>
          </div>

          {/* Status dropdown */}
          <div className="notif-dropdowns">
            <div className="dropdown">
              <button
                className="dropdown-btn secondary"
                onClick={() => {
                  setStatusOpen((v) => !v);
                  setActionOpen(false);
                }}
              >
                {statusOptions.find((o) => o.value === statusFilter)?.label || "All"}
              </button>
              {statusOpen && (
                <div className="dropdown-menu">
                  {statusOptions.map((opt) => (
                    <button
                      key={opt.value}
                      onClick={() => {
                        setStatusFilter(opt.value);
                        setStatusOpen(false);
                      }}
                    >
                      {opt.label}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* List */}
      <div className="notif-list">
        {(() => {
          if (loading) {
            return (
              <div className="notif-skeleton-wrap">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="notif-card skeleton" />
                ))}
              </div>
            );
          }

          if (filtered.length === 0) {
            return <div className="notif-empty">No notifications</div>;
          }

          return filtered.map((n) => {
            const selected = selectedIds.has(n.id);
            return (
              <div
                key={n.id}
                className={`notif-card ${n.read ? "read" : "unread"}`}
              >
                <div className="notif-cell checkbox-cell">
                  <input
                    type="checkbox"
                    checked={selected}
                    onChange={() => toggleSelect(n.id)}
                  />
                </div>

                <div className="notif-cell avatar-cell">
                  <div className="avatar-circle">
                    {(n.bidderFullName || "?").slice(0, 1).toUpperCase()}
                  </div>
                </div>

                <div className="notif-cell name-cell">
                  {n.bidderFullName || "Anonymous"}
                </div>

                <div className="notif-cell type-cell">{n.type || ""}</div>

                <div className="notif-cell auction-cell">{n.auctionId || "—"}</div>

                <div className="notif-cell time-cell">{formatTime(n.createdAt)}</div>

                <div className="notif-cell status-cell">
                  {n.read ? "Read" : "Unread"}
                </div>

                <div className="notif-cell action-cell">
                  <button
                    className="details-btn"
                    onClick={() => {
                      navigate(`${basePath}/${n.id}`, {
                        state: { notification: n },
                      });
                    }}
                  >
                    Details
                  </button>
                </div>
              </div>
            );
          });
        })()}
      </div>
    </div>
  );
};

export default Notifications;
