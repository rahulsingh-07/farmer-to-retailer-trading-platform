import React from "react";
import { useNavigate } from "react-router-dom";
import "./OrderCard.css";
import { Clock1, ShoppingBasket, User } from "lucide-react";
import {useAuth} from '../../context/AuthContext';
export default function OrderCard({ order = {} }) {
  const navigate = useNavigate();
  const { user, isRetailer } = useAuth();
  
  // ✅ SAFE: Early return
  if (!order || !order.orderId) return null;

  const {
    orderId: id,
    farmerName = "Unknown Farmer",
    retailerName = "Unknown Retailer",
    status = "PENDING",
    cropName: name = "Unknown Crop",
    variety = "Unknown Variety",
    quantity = "N/A",
    imageUrl = "/placeholder-order.png",
    createAt,
  } = order;



  // ✅ FIXED: Safe status class (toLowerCase + fallback)
  const getStatusClass = (status) => {
    if (!status) return "unknown";
    try {
      return status.toString().toLowerCase();
    } catch {
      return "unknown";
    }
  };

  const statusClass = getStatusClass(status);

  const handleCardClick = () => {
     if (id) {
      if (user?.role === 'RETAILER' || user?.username?.startsWith('RETL')) {
        navigate(`/retailer/order-details/${id}`);  // Retailer path
      } else {
        navigate(`/farmer/order-details/${id}`);     // Farmer path
      }
    }
  };

 
  const formatDate = (dateString) => {
    try {
      if (!dateString) return "N/A";
      const date = new Date(dateString);
      return date.toLocaleDateString('en-IN'); // Indian format
    } catch {
      return "N/A";
    }
  };

  const orderDate = formatDate(createAt);

  return (
    <div
      className="order-card"
      role="button"
      tabIndex={0}
      onClick={handleCardClick}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          handleCardClick();
        }
      }}
    >
      <div className="order-card-image">
        <img
          src={imageUrl}
          alt={`${name} - ${variety}`}
          loading="lazy"
          onError={(e) => {
            e.target.src = "/placeholder-order.png";
          }}
        />
      </div>

      <div className="order-card-body">
        <div className="order-headline">
          <h3 className="order-title" title={name}>
            {name?.length > 25 ? `${name.substring(0, 25)}...` : name}
          </h3>
          <p className={`order-status ${statusClass}`}>
            {status || "Unknown"}
          </p>
        </div>
        
        <p className="order-variety">
          Variety: <span>{variety}</span>
        </p>

        <div className="order-farmer">
          <User size={14} /> <span title={user?.role === 'RETAILER' ? farmerName : retailerName}>
            {user?.role === 'RETAILER' ? farmerName : retailerName}</span>
        </div>
        <div className="order-meta">
          <span title={quantity}>
            <ShoppingBasket size={14} /> {quantity} kg
          </span>
          <span title={orderDate}>
            <Clock1 size={14} /> {orderDate}
          </span>
        </div>
      </div>
    </div>
  );
}
