import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import BidModal from "./BidModal";
import "../../css/CropDetail.css";
import { FaShoppingCart } from "react-icons/fa";
import { BsGraphUpArrow } from "react-icons/bs";

const CropDetailPage = ({ user: propUser, token: propToken }) => {
  const { id } = useParams();
  const { user: ctxUser, token: ctxToken } = useAuth();
  const user = propUser || ctxUser;
  const token = propToken || ctxToken;

  const [crop, setCrop] = useState(null);
  const [activeImg, setActiveImg] = useState("");
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

  useEffect(() => {
    const fetchCrop = async () => {
      try {
        const data = await api.get(`/public/crop/${id}`); // public detail endpoint
        const imgs = normalizeImages(data);
        setCrop({
          ...data,
          images: imgs,
          status: data.status || data.auctionStatus,
        });
        if (imgs.length > 0) setActiveImg(imgs[0]);
      } catch (err) {
        console.error(err);
        toast.error("Failed to load crop details");
      }
    };

    fetchCrop();
  }, [id]);

  const handleBid = async () => {
    const amountNum = Number(bidAmount);
    if (Number.isNaN(amountNum) || amountNum <= (crop.currentHighestBid || 0)) {
      toast.error(
        `Bid must be higher than current highest: ₹${crop.currentHighestBid ?? 0}`
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
      await api.post(
        `/public/auctions/${crop.auctionId}/bid`,
        { amount: amountNum },
        token
      );
      const successMsg = response?.message || "Crop added successfully";
      toast.success(successMsg);
      setShowBidModal(false);

      // refresh crop info (e.g., updated highest bid)
      const updated = await api.get(`/public/crop/${id}`);
      const imgs = normalizeImages(updated);
      setCrop({ ...updated, images: imgs, status: updated.status || updated.auctionStatus });
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
  return (
    <div className="detail-container">
      {/* LEFT: gallery & CTA buttons */}
      <div className="detail-left">
        <div className="thumb-list">
          {images.length > 0 ? (
            images.map((url, idx) => (
              <button
                key={idx}
                type="button"
                className={`thumb-item ${
                  activeImg === url ? "thumb-active" : ""
                }`}
                onClick={() => setActiveImg(url)}
              >
                <img src={url} alt={`${crop.cropName}-${idx}`} />
              </button>
            ))
          ) : (
            <span className="thumb-placeholder">No images</span>
          )}
        </div>

        <div className="main-img-wrapper">
          <img
            src={
              activeImg ||
              images[0] ||
              "https://via.placeholder.com/600x600?text=No+Image"
            }
            alt={crop.cropName}
            className="main-img"
          />
        </div>

        <div className="detail-actions">
          <button className="detail-add-btn" type="button" disabled={!canBid} >
            <FaShoppingCart /> Add to Watchlist
          </button>
          <button
            className="detail-buy-btn"
            type="button"
            onClick={() => setShowBidModal(true)}
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
