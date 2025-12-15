import React, { useEffect, useMemo, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/CropDetail.css";
import "../../css/NotificationDetails.css";
import SellModal from "./SellModal";

const placeholderImg = "https://via.placeholder.com/600x600?text=No+Image";

const normalizeImages = (data) => {
  if (!data) return [];
  if (Array.isArray(data.imageUrl)) {
    return data.imageUrl
      .map((img) => (typeof img === "string" ? img : img?.imageUrl))
      .filter(Boolean);
  }
  return [];
};

const NotificationDetails = () => {
  const { id } = useParams();
  const location = useLocation();
  const { token } = useAuth();

  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState(location.state?.notification || null);
  const [activeImg, setActiveImg] = useState("");
  const [showSell, setShowSell] = useState(false);

  const images = useMemo(() => normalizeImages(detail), [detail]);

  useEffect(() => {
    const fetchDetail = async () => {
      if (!id) return;
      setLoading(true);
      try {
        const data = await api.get(`/farmer/notifications/${id}`, token);
        setDetail(data);
      } catch (err) {
        toast.error(err.message || "Failed to load notification");
      } finally {
        setLoading(false);
      }
    };

    fetchDetail();
  }, [id, token]);

  useEffect(() => {
    if (images.length > 0) {
      setActiveImg(images[0]);
    } else {
      setActiveImg(placeholderImg);
    }
  }, [images]);

  if (loading && !detail) {
    return <div className="detail-loading">Loading notification...</div>;
  }

  if (!detail) {
    return <div className="detail-loading">Notification not found.</div>;
  }

  const status = (detail.status || detail.auctionStatus || "").toString();
  const statusUpper = status.toUpperCase();
  const typeUpper = (detail.type || "").toString().toUpperCase();
  const canSell = statusUpper !== "ACTIVE" || typeUpper === "BID_WON";
  const readState = detail.read ? "Read" : "Unread";

  return (
    <div className="detail-container nd-container">
      {/* LEFT: gallery similar to crop detail */}
      <div className="detail-left nd-equal">
        <div className="thumb-list">
          {images.length > 0 ? (
            images.map((url, idx) => (
              <button
                key={idx}
                type="button"
                className={`thumb-item ${activeImg === url ? "thumb-active" : ""}`}
                onClick={() => setActiveImg(url)}
              >
                <img src={url} alt={`img-${idx}`} />
              </button>
            ))
          ) : (
            <span className="thumb-placeholder">No images</span>
          )}
        </div>

        <div className="main-img-wrapper">
          <img
            src={activeImg || placeholderImg}
            alt={detail.cropName || "Notification"}
            className="main-img"
          />
        </div>

        <div className="detail-actions">
          <button
            className="detail-buy-btn"
            type="button"
            disabled={!canSell}
            onClick={() => setShowSell(true)}
          >
            Sell
          </button>
        </div>
      </div>

      {/* RIGHT: notification + crop + bidder */}
      <div className="detail-right nd-equal">
        <h1 className="detail-title">{detail.cropName || "Notification"}</h1>

        <div className="nd-pill-row">
          <span className="nd-pill nd-pill-status">{status || ""}</span>
          <span className="nd-pill nd-pill-read">{readState}</span>
          {detail.type && <span className="nd-pill nd-pill-type">{detail.type}</span>}
        </div>

        <p className="nd-message">{detail.message || "No message provided."}</p>
        <div className="detail-subtext nd-meta-inline">
          <span>Notification ID: {detail.notificationId || detail.id || "--"}</span>
          <span>
            Created: {detail.createdAt ? new Date(detail.createdAt).toLocaleString() : "--"}
          </span>
        </div>

        <div className="detail-section">
          <h3>Crop details</h3>
          <table className="detail-table">
            <tbody>
              <tr>
                <td>Auction ID</td>
                <td>{detail.auctionId || "--"}</td>
              </tr>
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
      </div>

      {showSell && (
        <SellModal
          notification={detail}
          disabled={!canSell}
          onClose={() => setShowSell(false)}
          onConfirm={() => {
            toast.info("Implement sell API integration");
            setShowSell(false);
          }}
        />
      )}
    </div>
  );
};

export default NotificationDetails;
