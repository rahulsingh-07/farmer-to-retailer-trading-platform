import React, { useCallback, useEffect, useState } from "react";
import PropTypes from "prop-types";
import { useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import BidModal from "./BidModal";
import "../../css/CropDetail.css";
import { BsGraphUpArrow } from "react-icons/bs";

const CropDetailPage = ({ user: propUser, token: propToken }) => {
  const { id } = useParams();
  const { user: ctxUser, token: ctxToken } = useAuth();
  const user = propUser || ctxUser;
  const token = propToken || ctxToken;

  const [crop, setCrop] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [showBidModal, setShowBidModal] = useState(false);
  const [bidAmount, setBidAmount] = useState("");

  const normalizeImages = (data) => {
    if (Array.isArray(data.images)) {
      return data.images.map((img) => (typeof img === "string" ? img : img?.imageUrl)).filter(Boolean);
    }
    if (Array.isArray(data.imageUrl)) {
      return data.imageUrl.map((img) => (typeof img === "string" ? img : img?.imageUrl)).filter(Boolean);
    }
    if (data.imageUrl) {
      return [data.imageUrl];
    }
    return [];
  };

  const refreshCrop = useCallback(async () => {
    try {
      const data = await api.get(`/public/crop/${id}`); // public detail endpoint
      const imgs = normalizeImages(data);
      setCrop({
        ...data,
        images: imgs,
        status: data.status || data.auctionStatus,
      });
      if (imgs.length > 0) {
        setCurrentIndex(0);
      }
      return data;
    } catch (err) {
      console.error(err);
      toast.error("Failed to load crop details");
      throw err;
    }
  }, [id]);

  useEffect(() => {
    refreshCrop();
  }, [refreshCrop]);

  const handleBid = async () => {
    const amountNum = Number(bidAmount);

    // Ensure we validate against the freshest numbers before sending
    let latest;
    try {
      latest = await refreshCrop();
    } catch (err) {
      return; // already toasted
    }

    const latestHighest = Number(latest?.currentHighestBid || 0);
    const base = Number(latest?.pricePerUnit || 0);
    const minRequired = (latestHighest > 0 ? latestHighest : base) + 5;

    if (Number.isNaN(amountNum) || amountNum < minRequired) {
      toast.error(
        `Bid must be at least ₹${minRequired.toLocaleString()} (current ₹${(latestHighest || base).toLocaleString()})`
      );
      return;
    }
    const role = (user?.role || "").toString().toUpperCase();
    if (!token) {
      toast.error("You must be logged in to place a bid.");
      return;
    }
    if (!role.includes("RETAILER")) {
      toast.error("Only retailers can place bids.");
      return;
    }

    try {
      const response = await api.post(
        `/public/auctions/${crop.auctionId}/bid`,
        { amount: amountNum },
        token
      );
      const successMsg = response?.message || "Bid placed successfully";
      toast.success(successMsg);
      setShowBidModal(false);
      await refreshCrop();
    } catch (err) {
      console.log(user.role)
      console.error(err);
      toast.error(err.message || "Bid failed");
    }
  };

  if (!crop) {
    return <div className="detail-loading">Loading...</div>;
  }

  const images = crop.images || [];
  const status = (crop.status || crop.auctionStatus || "").toString().toUpperCase();
  const role = (user?.role || "").toString().toUpperCase();
  const canBid = status === "ACTIVE" && role.includes("RETAILER") && !!token;

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
  return (
    <div className="detail-container">
      {/* LEFT: gallery & CTA buttons */}
      <div className="detail-left">
        <div className="slider-wrapper">
          {images.length > 0 ? (
            <>
              <button
                type="button"
                className="slider-nav prev"
                onClick={handlePrev}
                aria-label="Previous image"
              >
                ‹
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
                        alt={`${crop.cropName}-${idx}`}
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
                ›
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
            className="detail-buy-btn"
            type="button"
            onClick={async () => {
              try {
                await refreshCrop();
              } catch (e) {
                // already toasted
              }
              setShowBidModal(true);
            }}
            disabled={!canBid}
          >
            <BsGraphUpArrow /> Place Bid
          </button>
        </div>
      </div>

      {/* RIGHT: product info */}
      <div className="detail-right">
        <h1 className="detail-title">
          {crop.cropName} {crop.variety ? `(${crop.variety})` : ""}
        </h1>

        {/* <div className="detail-rating-row">
          <span className="detail-rating-badge">4.4★</span>
          <span className="detail-rating-text">
            2,612 Ratings &amp; 246 Reviews
          </span>
        </div> */}

        <div className="detail-price-block">
          <div className="detail-price-row">
            <span className="detail-price">
              ₹
              {Number(
                crop.currentHighestBid || crop.pricePerUnit || 0
              ).toLocaleString()}
            </span>
            {crop.currentHighestBid && (
              <span className="detail-strike">
                ₹{Number(crop.pricePerUnit || 0).toLocaleString()}
              </span>
            )}
            {crop.currentHighestBid && (
              <span className="detail-off">Bidding live</span>
            )}
          </div>
          <div className="detail-subtext">
            Base price: ₹
            {Number(crop.pricePerUnit || 0).toLocaleString()} / {crop.unit}
          </div>
          <div className="detail-subtext">
            {crop.daysLeft > 0
              ? `${crop.daysLeft} days left • Status: ${crop.auctionStatus}`
              : `Auction closed • Status: ${crop.auctionStatus}`}
          </div>
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
                <td>Category</td>
                <td>{crop.category}</td>
              </tr>
              <tr>
                <td>Location</td>
                <td>{crop.location}</td>
              </tr>
              <tr>
                <td>Quantity</td>
                <td>
                  {crop.quantity} {crop.unit}
                </td>
              </tr>
              <tr>
                <td>Harvest date</td>
                <td>{crop.harvestDate}</td>
              </tr>
              <tr>
                <td>Farmer</td>
                <td>{crop.farmerName}</td>
              </tr>
            </tbody>
          </table>
        </div>

        {crop.description && (
          <div className="detail-section">
            <h3>Description</h3>
            <p className="detail-description">{crop.description}</p>
          </div>
        )}
      </div>

      {showBidModal && (
        <BidModal
          crop={crop}
          bidAmount={bidAmount}
          setBidAmount={setBidAmount}
          onClose={() => setShowBidModal(false)}
          onBid={handleBid}
        />
      )}
    </div>
  );
};

export default CropDetailPage;

CropDetailPage.propTypes = {
  user: PropTypes.shape({
    role: PropTypes.string,
  }),
  token: PropTypes.string,
};

CropDetailPage.defaultProps = {
  user: null,
  token: undefined,
};
