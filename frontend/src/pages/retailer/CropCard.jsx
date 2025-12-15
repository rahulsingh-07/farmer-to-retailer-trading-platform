// CropCard.jsx
import React from "react";
import { useNavigate } from "react-router-dom";
import "../../css/CropCard.css";

const CropCard = ({ crop }) => {
  const navigate = useNavigate();

  const mainImage =
    (Array.isArray(crop.images) && crop.images[0]) ||
    (Array.isArray(crop.imageUrl) && crop.imageUrl[0]?.imageUrl) ||
    crop.imageUrl ||
    "https://via.placeholder.com/300";

  const handleClick = () => {
    navigate(`/crops/${crop.id}`);
  };

  return (
    <div className="fk-card" onClick={handleClick}>
      <div className="fk-card-left">
        <img src={mainImage} alt={crop.cropName} />
      </div>

      <div className="fk-card-right">
        <h2 className="fk-title">
          {crop.cropName} {crop.variety ? `(${crop.variety})` : ""}
        </h2>

        {/* <div className="fk-rating-row">
          <span className="fk-rating-badge">4.4★</span>
          <span className="fk-rating-text">2,612 Ratings & 246 Reviews</span>
        </div> */}

        <ul className="fk-bullets">
          <li>Category: {crop.category}</li>
          <li>Location: {crop.location}</li>
          <li>
            Quantity: {crop.quantity} {crop.unit}
          </li>
          <li>Base price: ₹{Number(crop.pricePerUnit || 0).toLocaleString()}/{crop.unit}</li>
          <li>
            Highest bid: ₹{Number(crop.currentHighestBid || 0).toLocaleString()}
          </li>
        </ul>

        <div className="fk-bottom-row">
          <div className="fk-price-block">
            <span className="fk-price">
              ₹{Number(crop.currentHighestBid || crop.pricePerUnit || 0).toLocaleString()}
            </span>
            {crop.currentHighestBid && (
              <span className="fk-strike">
                ₹{Number(crop.pricePerUnit || 0).toLocaleString()}
              </span>
            )}
            <span className="fk-off">{crop.currentHighestBid ? "Bidding live" : "No bids yet"}</span>
          </div>

          <div className="fk-right-meta">
            <span className="fk-days">
              {crop.daysLeft > 0 ? `${crop.daysLeft} days left` : "Auction closed"}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CropCard;
