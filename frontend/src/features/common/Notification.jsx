import React, { useState, useEffect, useRef } from 'react';
import { Bell, BellRing, SquareCheckBig, Clock, CheckCircle, X, Inbox } from 'lucide-react';
import { getNotifications, getNotification,deleteMultipleNotifications ,readMultipleNotifications} from '../../services/notificationService';
import './Notification.css';
import AppCarousel from '../../components/common/AppCarousel';
import { useNavigate } from 'react-router-dom'; // Add this import

const Notification = () => {
  const [notifications, setNotifications] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);
  const [filter, setFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);
  const [details, setDetails] = useState(null);
  const [detailsLoading, setDetailsLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const navigate = useNavigate();

  // Fetch real notifications from API
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        setLoading(true);
        const res = await getNotifications();
        const data = res.data;

        // Transform backend data to frontend format
        const transformedNotifications = data.map(notification => ({
          id: notification.id,
          title: getNotificationTitle(notification),
          message: `${notification.bidderFullName || 'System'} - Auction #${notification.auctionId?.slice(-8)}`,
          time: formatTime(notification.createdAt),
          unread: !notification.read,
          rawData: notification
        }));

        setNotifications(transformedNotifications);
      } catch (error) {
        console.error('Failed to fetch notifications:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchNotifications();
  }, []);
  // Close dropdown on outside click
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setDropdownOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // Generate title based on NotificationType enum
  const getNotificationTitle = (notification) => {
    const type = notification.type;
    switch (type) {
      case 'BID_PLACED': return 'New bid received';
      case 'AUCTION_WON': return 'Auction won';
      case 'AUCTION_ENDED': return 'Auction ended';
      case 'PAYMENT_RECEIVED': return 'Payment confirmed';
      case 'ORDER_CONFIRMED': return 'Order confirmed';
      case 'KYC_APPROVED': return 'KYC approved';
      default: return `New ${type.toLowerCase().replace(/_/g, ' ')}`;
    }
  };

  const openNotificationDetails = async (notificationId) => {
    try {
      setDetailsLoading(true);
      setShowModal(true);

      const response = await getNotification(notificationId);
      const data = response.data;
      setDetails(data);
    } catch (error) {
      console.error('Notification details error:', error);
      toast.error('Failed to load notification details');
      setShowModal(false);
    } finally {
      setDetailsLoading(false);
    }
  };

  // Format LocalDateTime to relative time
  const formatTime = (createdAt) => {
    const now = new Date();
    const time = new Date(createdAt);
    const diffMs = now - time;
    const diffMins = Math.floor(diffMs / 60000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} min ago`;
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} hr ago`;
    return `${Math.floor(diffHours / 24)} day ago`;
  };

  const unreadCount = notifications.filter(n => n.unread).length;
  const totalCount = notifications.length;

  const toggleSelect = (id) => {
    setSelectedIds(prev =>
      prev.includes(id) ? prev.filter(s => s !== id) : [...prev, id]
    );
  };

  const carouselSlides = Array.isArray(details?.imageUrl)
    ? details.imageUrl.map(img => ({ image: img.imageUrl || img }))
    : [];

  const markReadSelected = async () => {
   try {
    await readMultipleNotifications(selectedIds);

    // Optimistic update
    setNotifications(prev =>
      prev.filter(n => !selectedIds.includes(n.id))
    );
  } catch (error) {
    console.error('Failed to mark notifications as read:', error);
  } finally {
    setSelectedIds([]);
    setDropdownOpen(false);
  }
  };

  const deleteSelected = async () => {
     try {
    await deleteMultipleNotifications(selectedIds);

    // Optimistic update
    setNotifications(prev =>
      prev.filter(n => !selectedIds.includes(n.id))
    );
  } catch (error) {
    console.error('Failed to delete notifications:', error);
  } finally {
    setSelectedIds([]);
    setDropdownOpen(false);
  }
  };

 


  const filteredNotifications = notifications.filter(n =>
    filter === 'all' ||
    (filter === 'unread' && n.unread) ||
    (filter === 'read' && !n.unread)
  );

  if (loading) {
    return (
      <div className="loading">
        <Bell className="loading-icon" />
        Loading notifications...
      </div>
    );
  }

  return (
    <div className="notification-container">
      <div className="notify-sidebar-header">
        <Bell size={24} />
        <h2>Notifications</h2>
      </div>

      <div className="notification-page">
        {/* Left Sidebar */}
        <div className="notify-sidebar">
          <div className="filter-section">
            <button
              className={`filter-btn ${filter === 'all' ? 'active' : ''}`}
              onClick={() => setFilter('all')}
            >
              <Inbox size={16} /> Inbox ({totalCount})
            </button>
            <button
              className={`filter-btn ${filter === 'unread' ? 'active' : ''}`}
              onClick={() => setFilter('unread')}
            >
              <BellRing size={16} /> Unread ({unreadCount})
            </button>
            <button
              className={`filter-btn ${filter === 'read' ? 'active' : ''}`}
              onClick={() => setFilter('read')}
            >
              <SquareCheckBig size={16} /> Read ({totalCount - unreadCount})
            </button>
          </div>

          {/* Dropdown */}
          <div className="selected-action-dropdown" ref={dropdownRef}>
            <button
              className="selected-action-toggle"
              onClick={(e) => {
                e.stopPropagation();
                if (selectedIds.length > 0) setDropdownOpen(!dropdownOpen);
              }}
              disabled={selectedIds.length === 0}
            >
              <span className="toggle-text">
                {selectedIds.length === 0
                  ? 'Select items first'
                  : `${selectedIds.length} selected`
                }
              </span>
              <span className="chevron">▼</span>
            </button>

            {dropdownOpen && (
              <div className="selected-action-menu">
                <button
                  className="selected-action-item delete"
                  onClick={(e) => {
                    e.stopPropagation();
                    deleteSelected();
                  }}
                >
                  <X size={16} className="icon-small" />
                  Delete selected
                </button>
                <button
                  className="selected-action-item read"
                  onClick={(e) => {
                    e.stopPropagation();
                    markReadSelected();
                  }}
                >
                  <CheckCircle size={16} className="icon-small" />
                  Mark as read
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Main Panel */}
        <div className="notify-main-panel">
          <div className="notify-panel-content">
            {filteredNotifications.length === 0 ? (
              <div className="empty-state">
                <Bell className="empty-icon" />
                <h3>No notifications</h3>
                <p>You're all caught up!</p>
              </div>
            ) : (
              <div className="notifications-list">
                {filteredNotifications.map((notification) => (
                  <div
                    key={notification.id}
                    className={`notification-item ${notification.unread ? 'unread' : 'read'}`}
                    onClick={(e) => {
                      if (!e.target.closest('.notification-checkbox')) {
                        openNotificationDetails(notification.id);
                      }
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={selectedIds.includes(notification.id)}
                      onChange={(e) => {
                        e.stopPropagation();
                        toggleSelect(notification.id);
                      }}
                      className="notification-checkbox"
                    />

                    <div className="notification-content">
                      <div className="notification-header">
                        <h4 className="title">{notification.title}</h4>
                      </div>
                      <span className={`status ${notification.unread ? 'unread' : 'read'}`}>
                          {notification.unread ? 'Unread' : 'Read'}
                        </span>
                      <p className="message">{notification.message}</p>
                      <div className="timestamp">
                        {notification.unread && <div className="unread-dot" />}
                        <Clock className="clock-icon" size={12} />
                        <span>{notification.time}</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Modal */}
      {showModal && details && (
        <div className="notification-modal" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            {/* Header */}
            <div className="modal-header email-header">
              <h1 className="modal-title">{details.message}</h1>
              <p className="modal-subtitle">
                {details.type} • {details.status} • {formatTime(details.createdAt)}
              </p>
            </div>

            {/* Images */}
            <div className="modal-image">
              {carouselSlides.length > 0 ? (
                <AppCarousel
                  slides={carouselSlides}
                  height="420px"
                  showCaptions={false}
                />
              ) : (
                <div className="no-images-placeholder">
                  <img src="/placeholder-crop.png" alt="Crop" />
                </div>
              )}
            </div>

            {/* Body */}
            <div className="modal-body email-style">
              <p className="email-paragraph">
                This notification is related to the following crop listing:
              </p>

              <div className="email-card">
                <h3 className="email-card-title">🌾 Crop Details</h3>
                <p className="crop-name">{details.cropName}</p>
                <p className="muted">
                  {details.category} · {details.variety}
                </p>
                <p>
                  Quantity: <strong>{details.quantity} {details.unit}</strong>
                </p>
              </div>

              <p className="email-footnote">
                Notification ID: {details.notificationId}
              </p>
            </div>

            {/* Actions */}
            <div className="modal-actions">
              <button
                className="btn-secondary"
                onClick={() => setShowModal(false)}
              >
                Close
              </button>
              
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Notification;
