import React from "react";
import PropTypes from "prop-types";

// BidModal.jsx
const BidModal = ({ crop, bidAmount, setBidAmount, onClose, onBid }) => {
  const handleOverlayKeyDown = (event) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      onClose();
    }
  };
  const highest = Number(crop.currentHighestBid || 0);
  const displayHighest = highest > 0 ? highest : Number(crop.pricePerUnit || 0);
  const numericBid = Number(bidAmount || 0);
  const minBid = displayHighest + 5;
  const tooLow = Number.isFinite(numericBid) && numericBid < minBid;

  return (
    <div className="modal-overlay-wrapper">
      <button
        type="button"
        className="modal-overlay"
        aria-label="Close dialog"
        onClick={onClose}
        onKeyDown={handleOverlayKeyDown}
      />
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
            <p className="bid-rule">Bid at least ₹{minBid.toLocaleString()} (₹5 more than the current/base price).</p>
          </div>

          <div className="bid-input-group">
            <label htmlFor="bid-amount-input">Enter your bid (₹)</label>
            <div className="input-wrapper">
              <span className="currency">₹</span>
              <input
                id="bid-amount-input"
                type="number"
                value={bidAmount}
                onChange={(e) => setBidAmount(e.target.value)}
                min={minBid}
                step={5}
                placeholder={minBid || "Enter your bid"}
                className="bid-input"
              />
            </div>
          </div>

          {tooLow && (
            <p className="bid-warning">Your bid must be at least ₹{minBid.toLocaleString()}.</p>
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

BidModal.propTypes = {
  crop: PropTypes.shape({
    cropName: PropTypes.string,
    currentHighestBid: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    pricePerUnit: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    daysLeft: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    hoursLeft: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
  }),
  bidAmount: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
  setBidAmount: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  onBid: PropTypes.func.isRequired,
};

BidModal.defaultProps = {
  crop: {},
};

export default BidModal;