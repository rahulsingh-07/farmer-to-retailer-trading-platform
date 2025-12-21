import React, { useEffect, useMemo, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/CropDetail.css";
import "../../css/NotificationDetails.css";

const placeholderImg = "https://via.placeholder.com/600x600?text=No+Image";

const normalizeImages = (data) => {
  if (!data) return [];
  const imgs = Array.isArray(data.imageUrl) ? data.imageUrl : [];
  return imgs
    .map((img) => (typeof img === "string" ? img : img?.imageUrl))
    .filter(Boolean);
};

const normalizeDetail = (raw = {}) => {
  const normalized = {
    ...raw,
    notificationId: raw.notificationId || raw.id,
    status: raw.status || raw.auctionStatus,
    auctionId: raw.auctionId,
    cropId: raw.cropId,
    cropName: raw.cropName,
    category: raw.category,
    variety: raw.variety,
    quantity: raw.quantity,
    unit: raw.unit,
    bidderFullName: raw.bidderFullName,
    bidderPhoneNumber: raw.bidderPhoneNumber,
    bidderAddress: raw.bidderAddress,
    imageUrl: raw.imageUrl || [],
    // Payout & Balance fields
    payoutAmount: raw.payoutAmount || raw.amount || 0,
    payoutStatus: raw.payoutStatus || 'pending',
    balance: raw.balance !== undefined ? raw.balance : null,
    lastUpdated: raw.lastUpdated || raw.updatedAt,
  };
  return normalized;
};

const NotificationDetails = () => {
  const { id } = useParams();
  const location = useLocation();
  const { token, loading: authLoading } = useAuth();

  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState(
    location.state?.notification ? normalizeDetail(location.state.notification) : null
  );
  const [currentIndex, setCurrentIndex] = useState(0);

  const images = useMemo(() => normalizeImages(detail), [detail]);

  useEffect(() => {
    const fetchDetail = async () => {
      if (!id || authLoading || !token) return;
      setLoading(true);
      try {
        const data = await api.get(`/user/notifications/${id}`, token);
        setDetail(normalizeDetail(data));
      } catch (err) {
        const message = err?.message || "Failed to load notification";
        toast.error(message);
      } finally {
        setLoading(false);
      }
    };

    fetchDetail();
  }, [id, token, authLoading]);

  useEffect(() => {
    if (images.length > 0) {
      setCurrentIndex(0);
    }
  }, [images]);

  const handleNext = () => {
    if (images.length === 0) return;
    const nextIndex = (currentIndex + 1) % images.length;
    setCurrentIndex(nextIndex);
  };

  const handlePrev = () => {
    if (images.length === 0) return;
    const prevIndex = (currentIndex - 1 + images.length) % images.length;
    setCurrentIndex(prevIndex);
  };

  const handleDot = (idx) => {
    setCurrentIndex(idx);
  };

  if (loading && !detail) {
    return <div className="detail-loading">Loading notification...</div>;
  }

  if (!detail) {
    return <div className="detail-loading">Notification not found.</div>;
  }

  const status = (detail.status || detail.auctionStatus || "").toString();
  const readState = detail.read ? "Read" : "Unread";

  return (
    <div className="detail-container nd-container">
      {/* LEFT: gallery similar to crop detail */}
      <div className="detail-left nd-equal">
        <div className="slider-wrapper">
          {images.length > 0 ? (
            <>
              <button
                type="button"
                className="slider-nav prev"
                onClick={handlePrev}
                aria-label="Previous image"
              >
                {"<"}
              </button>
              <div className="slider-viewport">
                <div
                  className="slider-track"
                  style={{ transform: `translateX(-${currentIndex * 100}%)` }}
                >
                  {images.map((url, idx) => (
                    <div className="slide" key={`${url}-${idx}`}>
                      <img
                        src={url}
                        alt={`img-${idx}`}
                        className="main-img"
                      />
                    </div>
                  ))}
                </div>
              </div>
              <button
                type="button"
                className="slider-nav next"
                onClick={handleNext}
                aria-label="Next image"
              >
                {">"}
              </button>
              <div className="slider-dots">
                {images.map((url, idx) => (
                  <button
                    key={`${url}-${idx}-dot`}
                    type="button"
                    className={`dot ${idx === currentIndex ? "dot-active" : ""}`}
                    onClick={() => handleDot(idx)}
                    aria-label={`Go to image ${idx + 1}`}
                  />
                ))}
              </div>
            </>
          ) : (
            <span className="thumb-placeholder">No images</span>
          )}
        </div>
      </div>

      {/* RIGHT: notification + crop + bidder + PAYOUT */}
      <div className="detail-right nd-equal">
        <h1 className="detail-title">{detail.cropName || "Notification"}</h1>

        <div className="nd-pill-row">
          <span className="nd-pill nd-pill-status">{status || ""}</span>
          <span className="nd-pill nd-pill-read">{readState}</span>
          {detail.type && <span className="nd-pill nd-pill-type">{detail.type}</span>}
        </div>

        <p className="nd-message">{detail.message || "No message provided."}</p>
        <div className="detail-subtext nd-meta-inline">
          <span>
            Created: {detail.createdAt ? new Date(detail.createdAt).toLocaleString() : "--"}
          </span>
        </div>

        <div className="detail-section">
          <h3>Crop details</h3>
          <table className="detail-table">
            <thead>
              <tr>
                <th scope="col">Field</th>
                <th scope="col">Value</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Crop</td>
                <td>{detail.cropName || "--"}</td>
              </tr>
              <tr>
                <td>Category</td>
                <td>{detail.category || "--"}</td>
              </tr>
              <tr>
                <td>Variety</td>
                <td>{detail.variety || "--"}</td>
              </tr>
              <tr>
                <td>Quantity</td>
                <td>{detail.quantity ? `${detail.quantity} ${detail.unit || ""}` : "--"}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div className="detail-section">
          <h3>Bidder details</h3>
          <table className="detail-table">
            <thead>
              <tr>
                <th scope="col">Field</th>
                <th scope="col">Value</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Full name</td>
                <td>{detail.bidderFullName || "--"}</td>
              </tr>
              <tr>
                <td>Phone</td>
                <td>{detail.bidderPhoneNumber || "--"}</td>
              </tr>
              <tr>
                <td>Address</td>
                <td>{detail.bidderAddress || "--"}</td>
              </tr>
            </tbody>
          </table>
        </div>

        {/* PROFESSIONAL PAYOUT & BALANCE SECTION */}
        {(detail.payoutAmount || detail.balance !== null) && (
          <div className="detail-section">
            <h3>💰 Payout & Balance</h3>
            <div className="payout-balance-card">
              <div className="payout-row">
                <div className="payout-label">Payout Amount</div>
                <div className="payout-amount">
                  ₹{Number(detail.payoutAmount || 0).toLocaleString('en-IN')}
                  <span className="payout-status">
                    {detail.payoutStatus === 'paid' ? '✅ Paid' : '⏳ Pending'}
                  </span>
                </div>
              </div>
              
              <div className="divider"></div>
              
              <div className="balance-row">
                <div className="balance-label">
                  <span>Available Balance</span>
                  <span className="balance-date">
                    {detail.lastUpdated ? new Date(detail.lastUpdated).toLocaleDateString('en-IN') : ''}
                  </span>
                </div>
                <div className="balance-amount">
                  ₹{detail.balance !== null ? Number(detail.balance).toLocaleString('en-IN') : '0'}
                  {detail.balance !== null && (
                    <span className={`balance-indicator ${detail.balance >= 0 ? 'positive' : 'negative'}`}>
                      {detail.balance >= 0 ? '↑' : '↓'}
                    </span>
                  )}
                </div>
              </div>
              
              <div className="payout-actions">
                {detail.payoutStatus !== 'paid' && detail.payoutAmount > 0 && (
                  <button className="btn-payout">
                    Request Payout
                  </button>
                )}
                <button className="btn-statement">
                  View Statement
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default NotificationDetails;
