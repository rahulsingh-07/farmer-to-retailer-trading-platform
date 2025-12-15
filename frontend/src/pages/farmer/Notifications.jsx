import React, { useEffect, useMemo, useState } from "react";
import { toast } from "react-toastify";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/Notifications.css";

const statusOptions = [
  { label: "All", value: "ALL" },
  { label: "Unread", value: "UNREAD" },
  { label: "Read", value: "READ" },
];

const Notifications = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const basePath = location.pathname.startsWith("/retailer")
    ? "/retailer/notifications"
    : "/farmer/notifications";
  const [loading, setLoading] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [actionOpen, setActionOpen] = useState(false);
  const [statusOpen, setStatusOpen] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());

  const fetchNotifications = async () => {
    setLoading(true);
    try {
      const data = await api.get("/farmer/notifications", token);
      const normalized = (data || []).map((item) => ({
        ...item,
        read: Boolean(item.read),
      }));
      setNotifications(normalized);
    } catch (err) {
      toast.error(err.message || "Unable to load notifications");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filtered = useMemo(() => {
    if (statusFilter === "READ") return notifications.filter((n) => n.read);
    if (statusFilter === "UNREAD") return notifications.filter((n) => !n.read);
    return notifications;
  }, [notifications, statusFilter]);

  const toggleSelect = (id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const markReadLocal = () => {
    if (selectedIds.size === 0) return;
    setNotifications((prev) =>
      prev.map((n) => (selectedIds.has(n.id) ? { ...n, read: true } : n))
    );
    setSelectedIds(new Set());
    toast.info("Marked as read (local). Connect backend when ready.");
  };

  const deleteLocal = () => {
    if (selectedIds.size === 0) return;
    setNotifications((prev) => prev.filter((n) => !selectedIds.has(n.id)));
    setSelectedIds(new Set());
    toast.info("Deleted (local). Connect backend when ready.");
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
                  <button onClick={markReadLocal}>Mark as read</button>
                  <button onClick={deleteLocal}>Delete</button>
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
                {statusOptions.find((o) => o.value === statusFilter)?.label ||
                  "All"}
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
        {loading ? (
          <div className="notif-skeleton-wrap">
            {[1, 2, 3].map((i) => (
              <div key={i} className="notif-card skeleton" />
            ))}
          </div>
        ) : filtered.length === 0 ? (
          <div className="notif-empty">No notifications</div>
        ) : (
          filtered.map((n) => {
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
                    {(n.bidderFullName || "?")
                      .slice(0, 1)
                      .toUpperCase()}
                  </div>
                </div>

                <div className="notif-cell name-cell">
                  {n.bidderFullName || "Anonymous"}
                </div>

                <div className="notif-cell type-cell">
                  {n.type || ""}
                </div>

                <div className="notif-cell auction-cell">
                  {n.auctionId || "—"}
                </div>

                <div className="notif-cell time-cell">
                  {formatTime(n.createdAt)}
                </div>

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
          })
        )}
      </div>
    </div>
  );
};

export default Notifications;
