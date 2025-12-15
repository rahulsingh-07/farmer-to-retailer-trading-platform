import React from "react";

// BidModal.jsx
const BidModal = ({ crop, bidAmount, setBidAmount, onClose, onBid }) => {
  const highest = Number(crop.currentHighestBid || 0);
  const displayHighest = highest > 0 ? highest : Number(crop.pricePerUnit || 0);
  const numericBid = Number(bidAmount || 0);
  const tooLow = Number.isFinite(numericBid) && numericBid <= displayHighest;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="bid-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Place Bid for {crop.cropName}</h3>
          <button className="close-btn" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="modal-body">
          <div className="current-bid-display">
            <strong>
              {highest > 0 ? "Current Highest:" : "Base Price:"} ₹{displayHighest.toLocaleString()}
            </strong>
            <p className="bid-rule">Your bid must be higher than this amount</p>
          </div>

          <div className="bid-input-group">
            <label>Enter your bid (₹)</label>
            <div className="input-wrapper">
              <span className="currency">₹</span>
              <input
                type="number"
                value={bidAmount}
                onChange={(e) => setBidAmount(e.target.value)}
                min={displayHighest + 1}
                placeholder={displayHighest ? displayHighest + 100 : "Enter your bid"}
                className="bid-input"
              />
            </div>
          </div>

          {tooLow && (
            <p className="bid-warning">Your bid must be higher than ₹{displayHighest.toLocaleString()}.</p>
          )}

          <div className="auction-timer">
            ⏰ {crop.daysLeft}d {crop.hoursLeft}h left
          </div>
        </div>

        <div className="modal-footer">
          <button className="cancel-btn" onClick={onClose}>
            Cancel
          </button>
          <button
            className="confirm-bid-btn"
            onClick={onBid}
            disabled={tooLow}
          >
            Place Bid Now
          </button>
        </div>
      </div>
    </div>
  );
};

export default BidModal;