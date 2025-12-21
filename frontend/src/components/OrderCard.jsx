import React from "react";
import { useNavigate } from "react-router-dom";
import PropTypes from "prop-types";
import "../css/Orders.css";

const formatDate = (value) => {
  if (!value) return "—";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? "—" : date.toLocaleString();
};

const statusClass = (status) => {
  const key = (status || "PENDING").toLowerCase();
  if (key === "confirmed") return "status-confirmed";
  if (key === "shipped") return "status-shipped";
  return "status-pending";
};

const OrderCard = ({ order, kind }) => {
  const navigate = useNavigate();
  const images = Array.isArray(order?.imageUrl)
    ? order.imageUrl
        .map((img) => (typeof img === "string" ? img : img?.imageUrl))
        .filter(Boolean)
    : [];
  const mainImage = images[0] || "https://via.placeholder.com/320x200?text=Crop";
  const orderId = order?.orderId || order?.id;
  const cropName = order?.cropName || "Crop";
  const quantity = order?.quantity !== undefined && order?.quantity !== null
    ? `${order.quantity}`
    : "—";
  const counterparty = kind === "farmer" ? order?.retailerName || "Retailer" : order?.farmerName || "Farmer";

  const handleOpenDetails = () => {
    if (!orderId) return;
    const basePath = kind === "retailer" ? "/retailer/order" : "/farmer/order";
    navigate(`${basePath}/${orderId}`);
  };

  return (
    <button
      type="button"
      className={`order-card ${statusClass(order?.status)} ${orderId ? "clickable" : ""}`}
      onClick={handleOpenDetails}
      disabled={!orderId}
      aria-label={orderId ? `Open order ${orderId}` : undefined}
    >
      <div className="order-media">
        <img src={mainImage} alt={cropName} loading="lazy" />
        {images.length > 1 && <span className="media-count"></span>}
      </div>

      <div className="order-card-header">
        <span className="status-chip">{order?.status || "PENDING"}</span>
        <span className="order-id">#{(order?.orderId || "").toString().slice(0, 8)}</span>
      </div>

      <div className="order-meta">
        <div>
          <p className="label">Counterparty</p>
          <p className="value">{counterparty}</p>
        </div>
        <div>
          <p className="label">Created</p>
          <p className="value">{formatDate(order?.createdAt)}</p>
        </div>
      </div>

      <div className="order-crop">
        <h3>{cropName}</h3>
        <p className="muted">{order?.variety || order?.category || ""}</p>
        <div className="order-crop-grid">
          <span>Qty: {quantity}</span>
          <span>Category: {order?.category || "—"}</span>
          <span>Variety: {order?.variety || "—"}</span>
          {images.length > 0 && <span>{images.length} image{images.length > 1 ? "s" : ""}</span>}
        </div>
      </div>

      <div className="order-timeline">
        <div>
          <p className="label">Confirmed</p>
          <p className="value">{formatDate(order?.confirmedAt)}</p>
        </div>
        <div>
          <p className="label">Shipped</p>
          <p className="value">{formatDate(order?.shippedAt)}</p>
        </div>
      </div>
    </button>
  );
};

OrderCard.propTypes = {
  order: PropTypes.shape({
    orderId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    status: PropTypes.string,
    retailerName: PropTypes.string,
    farmerName: PropTypes.string,
    createdAt: PropTypes.oneOfType([PropTypes.string, PropTypes.number, PropTypes.instanceOf(Date)]),
    confirmedAt: PropTypes.oneOfType([PropTypes.string, PropTypes.number, PropTypes.instanceOf(Date)]),
    shippedAt: PropTypes.oneOfType([PropTypes.string, PropTypes.number, PropTypes.instanceOf(Date)]),
    cropName: PropTypes.string,
    category: PropTypes.string,
    variety: PropTypes.string,
    quantity: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    imageUrl: PropTypes.arrayOf(
      PropTypes.oneOfType([
        PropTypes.string,
        PropTypes.shape({ imageUrl: PropTypes.string })
      ])
    ),
  }).isRequired,
  kind: PropTypes.oneOf(["farmer", "retailer"]).isRequired,
};

export default OrderCard;
