import React, { useEffect, useMemo, useState } from "react";
import PropTypes from "prop-types";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import "../../css/CropDetail.css";

const formatDateTime = (value) => {
  if (!value) return "-";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "-" : date.toLocaleString();
};

const formatMoney = (value) => {
  if (value === undefined || value === null) return "-";
  const numeric = Number(value);
  if (Number.isNaN(numeric)) return "-";
  return `₹${numeric.toLocaleString()}`;
};

const normalizeImages = (data) => {
  if (!data) return [];
  const pool = data.imageUrl || [];
  const arr = Array.isArray(pool) ? pool : [pool];
  return arr
    .map((img) => (typeof img === "string" ? img : img?.imageUrl))
    .filter(Boolean);
};

const statusClass = (status) => {
  const key = (status || "PENDING").toLowerCase();
  if (key === "confirmed") return "status-confirmed";
  if (key === "shipped") return "status-shipped";
  return "status-pending";
};

const paymentClass = (status) => {
  const key = (status || "PENDING").toLowerCase();
  if (key === "paid" || key === "completed") return "payment-paid";
  if (key === "failed") return "payment-failed";
  return "payment-pending";
};

const OrderDetails = ({ token: propToken }) => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { token: ctxToken } = useAuth();
  const token = propToken || ctxToken;

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    const fetchOrder = async () => {
      if (!id) return;
      setLoading(true);
      try {
        // Backend contract: GET /farmer/orders/{id}
        const data = await api.get(`/user/farmer/orders/${id}`, token);
        setOrder(data);
        const imgs = normalizeImages(data);
        if (imgs.length > 0) setCurrentIndex(0);
      } catch (err) {
        toast.error(err.message || "Failed to load order details");
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [id, token]);

  const images = useMemo(() => normalizeImages(order), [order]);

  const handleNext = () => {
    if (!images.length) return;
    setCurrentIndex((prev) => (prev + 1) % images.length);
  };

  const handlePrev = () => {
    if (!images.length) return;
    setCurrentIndex((prev) => (prev - 1 + images.length) % images.length);
  };

  const handleDot = (idx) => setCurrentIndex(idx);

  const handleConfirm = async () => {
    if (!id) return;
    setActionLoading(true);
    try {
      // Backend contract: POST /farmer/order/{id}/confirmed
      const result = await api.post(`/farmer/order/${id}/confirmed`, {}, token);
      setOrder((prev) => (prev ? { ...prev, status: "CONFIRMED", paymentStatus: prev.paymentStatus || "PENDING_PAYMENT" } : prev));
      toast.success(result?.message || "Order confirmed. Waiting for payment.");
      if (globalThis?.location?.reload) {
        globalThis.location.reload();
      }
    } catch (err) {
      toast.error(err.message || "Unable to confirm order");
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) return <div className="detail-loading">Loading...</div>;
  if (!order) return <div className="detail-loading">Order not found.</div>;

  return (
    <div className="order-page">
      <div className="detail-container order-top">
        <div className="detail-left">
          <div className="slider-wrapper">
            {images.length > 0 ? (
              <>
                <button type="button" className="slider-nav prev" onClick={handlePrev} aria-label="Previous image">
                  &#8249;
                </button>
                <div className="slider-viewport">
                  <div className="slider-track" style={{ transform: `translateX(-${currentIndex * 100}%)` }}>
                    {images.map((url, idx) => (
                      <div className="slide" key={`${url}-${idx}`}>
                        <img src={url} alt={`${order.cropName || "crop"}-${idx}`} className="main-img" />
                      </div>
                    ))}
                  </div>
                </div>
                <button type="button" className="slider-nav next" onClick={handleNext} aria-label="Next image">
                  &#8250;
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

          <div className="detail-actions">
            <button
              type="button"
              className="detail-add-btn"
              onClick={handleConfirm}
              disabled={actionLoading || (order.status || "").toUpperCase() === "CONFIRMED"}
            >
              {actionLoading ? "Confirming..." : "Confirm order (wait for payment)"}
            </button>
            <button type="button" className="detail-buy-btn" onClick={() => navigate(-1)}>
              Go back
            </button>
          </div>
        </div>

        <div className="detail-middle detail-pane">
          <div className="order-header-row">
            <div>
              <p className="kicker">Order summary</p>
              <h1 className="detail-title" style={{ margin: 0 }}>
                {order.cropName || "Order"} {order.variety ? `(${order.variety})` : ""}
              </h1>
              <p className="detail-subtext">Order ID: {order.orderId} • Auction: {order.auctionId || "-"}</p>
            </div>
            <div className="order-pill-row">
              <span className={`status-chip ${statusClass(order.status)}`}>{order.status || "PENDING"}</span>
              {order.paymentStatus && (
                <span className={`status-chip ${paymentClass(order.paymentStatus)}`}>
                  Payment: {order.paymentStatus}
                </span>
              )}
            </div>
          </div>

          <div className="detail-price-block">
            <div className="detail-price-row">
              <span className="detail-price">{formatMoney(order.finalPrice ?? order.pricePerUnit)}</span>
              {order.pricePerUnit && <span className="detail-strike">{formatMoney(order.pricePerUnit)}</span>}
            </div>
            <div className="detail-subtext">Quantity: {order.quantity ?? "-"} {order.unit || ""}</div>
            <div className="detail-subtext">Location: {order.location || "-"}</div>
          </div>

          <div className="detail-section">
            <h3>Timeline</h3>
            <table className="detail-table">
              <thead>
                <tr>
                  <th scope="col">Event</th>
                  <th scope="col">Date</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <th scope="row">Created</th>
                  <td>{formatDateTime(order.createdAt)}</td>
                </tr>
                <tr>
                  <th scope="row">Confirmed</th>
                  <td>{formatDateTime(order.confirmedAt)}</td>
                </tr>
                <tr>
                  <th scope="row">Shipped</th>
                  <td>{formatDateTime(order.shippedAt)}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div className="detail-right detail-pane">
          <div className="order-header-row">
            <div>
              <p className="kicker">Crop details</p>
              <h1 className="detail-title" style={{ margin: 0 }}>
                {order.cropName || "Order"} {order.variety ? `(${order.variety})` : ""}
              </h1>
              <p className="detail-subtext">Location: {order.location || "-"}</p>
              <p className="detail-subtext">Quantity: {order.quantity ?? "-"} {order.unit || ""}</p>
            </div>
          </div>

          <div className="detail-section">
            <table className="detail-table">
              <thead>
                <tr>
                  <th scope="col">Field</th>
                  <th scope="col">Value</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <th scope="row">Category</th>
                  <td>{order.category || "-"}</td>
                </tr>
                <tr>
                  <th scope="row">Variety</th>
                  <td>{order.variety || "-"}</td>
                </tr>
                <tr>
                  <th scope="row">Harvest date</th>
                  <td>{order.harvestDate || "-"}</td>
                </tr>
                <tr>
                  <th scope="row">Description</th>
                  <td>{order.description || "-"}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div className="detail-bottom">
        <div className="detail-card">
          <h3 className="bg-green-600 text-white text-2xl font-bold ">Farmer details</h3>
          <table className="detail-table">
            <thead>
              <tr>
                <th scope="col">Field</th>
                <th scope="col">Value</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <th scope="row">Name</th>
                <td>{order.farmerName || "-"}</td>
              </tr>
              <tr>
                <th scope="row">Email</th>
                <td>{order.farmerEmail || "-"}</td>
              </tr>
              <tr>
                <th scope="row">Phone</th>
                <td>{order.farmerPhoneNumber || "-"}</td>
              </tr>
              <tr>
                <th scope="row">Address</th>
                <td>{order.farmerAddress || "-"}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div className="detail-card">
          <h3 className="bg-green-600 text-white text-2xl font-bold">Retailer details</h3>
          <table className="detail-table">
            <thead>
              <tr>
                <th scope="col">Field</th>
                <th scope="col">Value</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <th scope="row">Name</th>
                <td>{order.retailerName || "-"}</td>
              </tr>
              <tr>
                <th scope="row">Email</th>
                <td>{order.retailerEmail || "-"}</td>
              </tr>
              <tr>
                <th scope="row">Phone</th>
                <td>{order.retailerPhoneNumber || "-"}</td>
              </tr>
              <tr>
                <th scope="row">Address</th>
                <td>{order.retailerAddress || "-"}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

OrderDetails.propTypes = {
  token: PropTypes.string,
};

OrderDetails.defaultProps = {
  token: undefined,
};

export default OrderDetails;
