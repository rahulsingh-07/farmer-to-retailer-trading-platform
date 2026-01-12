import React from "react";
import { useNavigate } from "react-router-dom";
import "./CropCard.css";
import { useAuth } from "../../../context/AuthContext"; 
import PropTypes from "prop-types";
import { MapPin, Clock1, IndianRupee, ShoppingBasket } from "lucide-react";

export default function CropCard({ crop }) {
  const navigate = useNavigate();
  const { user } = useAuth(); 
  if (!crop) return null;

  const {
    id,
    cropName = "Unknown Crop",
    variety = "Unknown Variety",
    cropType = "",
    location = "Unknown Location",
    quantity = "N/A",
    unit = "kg",
    pricePerUnit = 0,
    currentHighestBid,
    timeLeft,
    imageUrl,
  } = crop;

  // Calculate dynamic time left for AUCTION crops
  const calculateTimeLeft = (endTime) => {
  if (!endTime || crop?.cropType?.toUpperCase() !== "AUCTION") return "N/A";

  const end = new Date(endTime.split('.')[0]); // 👈 FIX
  const now = new Date();
  const diffMs = end - now;

  if (diffMs <= 0) return "Ended";

  const days = Math.floor(diffMs / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diffMs / (1000 * 60 * 60)) % 24);
  const minutes = Math.floor((diffMs / (1000 * 60)) % 60);

  if (days > 0) return `${days}d ${hours}h left`;
  if (hours > 0) return `${hours}h ${minutes}m left`;
  return `${minutes}m left`;
};

  const displayTimeLeft = calculateTimeLeft(timeLeft);

  const mainImage = React.useMemo(() => {
    if (typeof imageUrl === "string" && imageUrl) return imageUrl;
    if (imageUrl?.imageUrl) return imageUrl.imageUrl; // in case object
    return "https://via.placeholder.com/300x200?text=No+Image";
  }, [imageUrl]);

  // Navigate to crop details
  const handleCardClick = (e) => {
    e.preventDefault();
    if (!id) return;

    const role = Array.isArray(user?.role) ? user.role[0] : user?.role;

    if (role === "RETAILER") {
      navigate(`/retailer/crop-details/${id}`);
    } else {
      navigate(`/farmer/crop-details/${id}`);
    }
  };

  return (
    <div
      className="crop-card"
      role="button"
      tabIndex={0}
      onClick={handleCardClick}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") handleCardClick(e);
      }}
    >
      <div className="crop-card-image">
        <img
          src={mainImage}
          alt={`${cropName} - ${variety}`}
          loading="lazy"
          onError={(e) => {
            e.target.src = "https://via.placeholder.com/300x200?text=No+Image";
          }}
        />
      </div>

      <div className="crop-card-body">
        <h3 className="crop-title" title={cropName}>
          {cropName.length > 25 ? `${cropName.substring(0, 25)}...` : cropName}
        </h3>
        <p className="crop-variety">
          Variety: <span>{variety}</span>
        </p>

        <div className="crop-meta">
          <span title={quantity}>
            <ShoppingBasket size={14} /> {quantity} {unit}
          </span>
          <span title={location}>
            <MapPin size={14} /> {location.length > 20 ? `${location.substring(0, 20)}...` : location}
          </span>
        </div>

        <div className="crop-footer">
  <span className="price">
    <IndianRupee size={14} />
    {cropType === "AUCTION" ? (
      <>
        {Number(currentHighestBid && currentHighestBid > 0 ? currentHighestBid : pricePerUnit).toLocaleString()}/{unit}
        {pricePerUnit && pricePerUnit !== currentHighestBid && (
          <span className="base-price">
            {" "}
            <s>{Number(pricePerUnit).toLocaleString()}</s>
          </span>
        )}
      </>
    ) : (
      <>
        {Number(pricePerUnit).toLocaleString()}/{unit}
      </>
    )}
  </span>

  {cropType === "AUCTION" && (
    <span className="time" title={displayTimeLeft}>
      <Clock1 size={14} /> {displayTimeLeft}
    </span>
  )}
</div>

      </div>
    </div>
  );
}

CropCard.propTypes = {
  crop: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    cropName: PropTypes.string,
    variety: PropTypes.string,
    cropType: PropTypes.string,
    location: PropTypes.string,
    quantity: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    unit: PropTypes.string,
    pricePerUnit: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    currentHighestBid: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    timeLeft: PropTypes.string, // LocalDateTime string from backend
    imageUrl: PropTypes.oneOfType([
      PropTypes.string,
      PropTypes.shape({ imageUrl: PropTypes.string }),
    ]),
  }),
};
