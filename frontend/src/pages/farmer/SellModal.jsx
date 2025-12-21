import React from "react";
import PropTypes from "prop-types";
import "../../css/NotificationDetails.css";

const SellModal = ({ notification, onClose, onConfirm, disabled }) => {
	const handleConfirm = () => {
		if (disabled) return;
		if (onConfirm) onConfirm(notification);
	};

	return (
		<div className="nd-modal-backdrop">
			<div className="nd-modal">
				<h3>Sell This Auction</h3>
				<p>
					Are you sure you want to sell auction {notification?.auctionId || "--"} to {notification?.bidderFullName || "the bidder"}?
				</p>
				<div className="nd-modal-actions">
					<button className="nd-btn nd-btn-secondary" onClick={onClose}>
						Cancel
					</button>
					<button className="nd-btn nd-btn-primary" onClick={handleConfirm} disabled={disabled}>
						Confirm Sell
					</button>
				</div>
			</div>
		</div>
	);
};

SellModal.propTypes = {
	notification: PropTypes.shape({
		auctionId: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
		bidderFullName: PropTypes.string,
	}),
	onClose: PropTypes.func.isRequired,
	onConfirm: PropTypes.func,
	disabled: PropTypes.bool,
};

SellModal.defaultProps = {
	notification: null,
	onConfirm: undefined,
	disabled: false,
};

export default SellModal;
