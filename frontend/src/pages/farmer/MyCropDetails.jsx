import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import { toast } from "react-toastify";
import "../../css/MyCropDetails.css";

const MyCropDetails = () => {
  const { id } = useParams();
  const { token } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchCrop = async () => {
      try {
        setLoading(true);
        setError("");
        const detail = await api.get(`/farmer/crop/${id}`, token);
        setData(detail);
      } catch (err) {
        const msg = err.message || "Unable to load crop details.";
        setError(msg);
        toast.error(msg);
      } finally {
        setLoading(false);
      }
    };

    fetchCrop();
  }, [id, token]);

  if (loading) {
    return <div className="mycropdetail-page"><div className="panel">Loading crop details...</div></div>;
  }

  if (error) {
    return (
      <div className="mycropdetail-page">
        <div className="panel error">{error}</div>
        <Link className="link-btn" to="/farmer/myCrops">Back to My Crops</Link>
      </div>
    );
  }

  if (!data) {
    return null;
  }

  const {
    cropId,
    cropName,
    category,
    variety,
    quantity,
    unit,
    pricePerUnit,
    location,
    harvestDate,
    description,
    createdAt,
    auctionId,
    auctionStartTime,
    auctionEndTime,
    currentHighestBid,
    highestBidderId,
    auctionStatus,
    bids,
  } = data;

  const fmtDate = (value) => (value ? new Date(value).toLocaleString() : "—");
  const fmtNumber = (value) => {
    const num = Number(value || 0);
    return Number.isFinite(num) ? num.toLocaleString() : "—";
  };

  return (
    <div className="mycropdetail-page">
      <div className="page-header">
        <div>
          <p className="kicker">Crop details</p>
          <h1>{cropName || "Crop"}</h1>
          <p className="subtitle">ID: {cropId || id}</p>
        </div>
        <Link className="link-btn" to="/farmer/myCrops">Back to My Crops</Link>
      </div>

      <div className="panel">
        <div className="detail-grid">
          <div>
            <p className="label">Category</p>
            <p className="value">{category || "—"}</p>
          </div>
          <div>
            <p className="label">Variety</p>
            <p className="value">{variety || "—"}</p>
          </div>
          <div>
            <p className="label">Quantity</p>
            <p className="value">{quantity ? `${quantity} ${unit || "kg"}` : "—"}</p>
          </div>
          <div>
            <p className="label">Price per unit</p>
            <p className="value">₹{fmtNumber(pricePerUnit)} / {unit || "kg"}</p>
          </div>
          <div>
            <p className="label">Location</p>
            <p className="value">{location || "—"}</p>
          </div>
          <div>
            <p className="label">Harvest date</p>
            <p className="value">{harvestDate || "—"}</p>
          </div>
          <div>
            <p className="label">Created at</p>
            <p className="value">{fmtDate(createdAt)}</p>
          </div>
          <div>
            <p className="label">Auction status</p>
            <p className="value">{auctionStatus || "—"}</p>
          </div>
        </div>

        {description && (
          <div className="description-block">
            <p className="label">Description</p>
            <p className="value">{description}</p>
          </div>
        )}

        <div className="auction-grid">
          <div>
            <p className="label">Auction ID</p>
            <p className="value">{auctionId || "—"}</p>
          </div>
          <div>
            <p className="label">Start</p>
            <p className="value">{fmtDate(auctionStartTime)}</p>
          </div>
          <div>
            <p className="label">End</p>
            <p className="value">{fmtDate(auctionEndTime)}</p>
          </div>
          <div>
            <p className="label">Highest bid</p>
            <p className="value">₹{fmtNumber(currentHighestBid)}</p>
          </div>
          <div>
            <p className="label">Highest bidder</p>
            <p className="value">{highestBidderId || "—"}</p>
          </div>
        </div>
      </div>

      <div className="panel">
        <div className="panel-header">
          <div>
            <p className="kicker">Bids</p>
            <h3>Recent bids</h3>
          </div>
        </div>
        {Array.isArray(bids) && bids.length > 0 ? (
          <div className="bids-table">
            <div className="bids-head">
              <span>Bidder</span>
              <span>Amount</span>
              <span>Time</span>
            </div>
            {bids.map((bid) => (
              <div key={bid.bidId} className="bids-row">
                <span>{bid.bidderName || "—"}</span>
                <span>₹{fmtNumber(bid.amount)}</span>
                <span>{fmtDate(bid.createdAt)}</span>
              </div>
            ))}
          </div>
        ) : (
          <p className="value">No bids yet.</p>
        )}
      </div>
    </div>
  );
};

export default MyCropDetails;
