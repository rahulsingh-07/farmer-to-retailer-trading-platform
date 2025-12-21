import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import { toast } from "react-toastify";
import "../../css/MyCrop.css";

const getCropKey = (crop) => crop.cropId || crop.id || crop.cropID || crop.name || crop.cropName;

const CropCard = ({ crop, bgIndexMap, getImages, formatDaysLeft, navigate }) => {
  const id = crop.cropId || crop.id || crop.cropID;
  const key = getCropKey(crop);
  const unit = crop.unit || "kg";
  const price = Number(crop.pricePerUnit || 0);
  const quantity = crop.quantity ? `${crop.quantity} ${unit}` : "—";
  const status = (crop.status || crop.auctionStatus || "ACTIVE").toString();
  const normalizedStatus = status.toUpperCase();
  const isSold = normalizedStatus === "SOLD";
  const statusLabel = isSold ? "Sold" : status;
  const harvest = crop.harvestDate || "—";
  const daysLeftLabel = formatDaysLeft(crop.daysLeft);
  const images = getImages(crop);
  const hasMultipleImages = images.length > 1;
  const imageIndex = key ? bgIndexMap[key] ?? 0 : 0;
  const coverImage = images[imageIndex] || images[0];
  const hasImage = Boolean(coverImage);

  return (
    <button
      type="button"
      key={key}
      className={`mycrop-card ${hasImage ? "" : "no-image"} ${hasMultipleImages ? "has-multiple" : ""} ${
        isSold ? "sold" : ""
      }`}
      onClick={() => id && navigate(`/farmer/crops/${id}`)}
      disabled={!id}
      style={{
        ...(hasImage ? { backgroundImage: `url(${coverImage})` } : {}),
        border: isSold ? "2px solid #0ea5e9" : undefined,
        opacity: isSold ? 0.94 : 1,
      }}
    >
      <div className="mycrop-card-overlay">
        <div className="mycrop-card-top">
          <span className={`mycrop-status ${isSold ? "is-sold" : ""}`}>
            {isSold ? "✔ Sold" : statusLabel}
          </span>
          <span className="mycrop-price">₹{price.toLocaleString()} / {unit}</span>
        </div>
        <div className="mycrop-card-body">
          <h3 className="mycrop-title">{crop.cropName || crop.name}</h3>
          {crop.variety && <p className="mycrop-meta">Variety: {crop.variety}</p>}
          <div className="mycrop-meta-grid">
            <span>{crop.category || "—"}</span>
            <span>{quantity}</span>
            <span>{crop.location || "—"}</span>
            <span>Harvest: {harvest}</span>
            <span>{daysLeftLabel}</span>
          </div>
        </div>
      </div>
    </button>
  );
};

const MyCrop = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [crops, setCrops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [bgIndexMap, setBgIndexMap] = useState({});

  const getImages = (crop) => {
    if (!crop) return [];
    if (Array.isArray(crop.imageUrl)) {
      return crop.imageUrl.map((img) => (typeof img === "string" ? img : img?.imageUrl)).filter(Boolean);
    }

    if (crop.imageUrl) {
      return [crop.imageUrl];
    }

    return [];
  };

  const formatDaysLeft = (daysLeft) => {
    if (daysLeft === undefined || daysLeft === null) return "—";
    if (Number(daysLeft) < 0) return "Sale closed";
    return `${daysLeft} days left`;
  };

  const fetchCrops = async () => {
    try {
      setLoading(true);
      setError("");
      const data = await api.get("/farmer/crops", token);
      setCrops(Array.isArray(data) ? data : data?.content || []);
    } catch (err) {
      const msg = err.message || "Unable to load your crops.";
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCrops();
  }, []);

  useEffect(() => {
    if (!crops.length) return undefined;

    const interval = setInterval(() => {
      setBgIndexMap((prev) => {
        const updated = { ...prev };
        let changed = false;

        crops.forEach((crop) => {
          const key = crop.cropId || crop.id || crop.cropID || crop.name || crop.cropName;
          if (!key) return;

          const images = getImages(crop);
          const count = images.length;

          if (count > 1) {
            const nextIndex = ((prev[key] ?? 0) + 1) % count;
            if (nextIndex !== prev[key]) {
              updated[key] = nextIndex;
              changed = true;
            }
          } else if (prev[key] !== undefined) {
            delete updated[key];
            changed = true;
          }
        });

        return changed ? updated : prev;
      });
    }, 4500);

    return () => clearInterval(interval);
  }, [crops]);

  return (
    <div className="mycrops-page">
      <div className="mycrops-header">
        <div>
          <p className="kicker">My inventory</p>
          <h1>My Crops</h1>
          <p className="subtitle">
            View every crop you've listed. Keep quantities and pricing accurate so retailers can bid confidently.
          </p>
        </div>
        <button className="ghost" onClick={fetchCrops} disabled={loading}>
          {loading ? "Refreshing..." : "Refresh"}
        </button>
      </div>

      {error && <div className="banner error">{error}</div>}

      {loading && <div className="panel loading">Loading your crops...</div>}

      {!loading && crops.length === 0 && (
        <div className="panel empty">
          <div className="icon">🌱</div>
          <h3>No crops yet</h3>
          <p>Add your first crop to start receiving bids.</p>
        </div>
      )}

      {!loading && crops.length > 0 && (
        <div className="panel">
          <div className="mycrops-grid">
            {crops.map((crop) => (
              <CropCard
                key={getCropKey(crop)}
                crop={crop}
                bgIndexMap={bgIndexMap}
                getImages={getImages}
                formatDaysLeft={formatDaysLeft}
                navigate={navigate}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default MyCrop;
