import React, { useEffect, useMemo, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import api from "../../utils/api";
import { toast } from "react-toastify";
import "../../css/MyCrop.css";

const MyCrop = () => {
  const { token, user } = useAuth();
  const [crops, setCrops] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const headers = useMemo(
    () => ["Crop", "Category", "Quantity", "Price", "Location", "Status",""],
    []
  );

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

      {loading ? (
        <div className="panel loading">Loading your crops...</div>
      ) : crops.length === 0 ? (
        <div className="panel empty">
          <div className="icon">🌱</div>
          <h3>No crops yet</h3>
          <p>Add your first crop to start receiving bids.</p>
        </div>
      ) : (
        <div className="panel">
          <div className="table">
            <div className="table-head">
              {headers.map((h) => (
                <div key={h} className="cell head">
                  {h}
                </div>
              ))}
            </div>
            <div className="table-body">
              {crops.map((crop) => (
                <div key={crop.id || crop.cropId || crop.name} className="row">
                  <div className="cell main">
                    <div className="title">{crop.cropName || crop.name}</div>
                    {crop.variety && <div className="meta">Variety: {crop.variety}</div>}
                  </div>
                  <div className="cell">{crop.category || "—"}</div>
                  <div className="cell">
                    {crop.quantity} {crop.unit || "kg"}
                  </div>
                  <div className="cell">₹{crop.pricePerUnit}/ {crop.unit || "kg"}</div>
                  <div className="cell">{crop.location}</div>
                  <div className="cell status">{crop.status || "ACTIVE"}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MyCrop;
