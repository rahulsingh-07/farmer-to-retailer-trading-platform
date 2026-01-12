import React from "react";
import { IndianRupee, Clock, Crown } from "lucide-react";
import "./MyBidCard.css";
import { useNavigate } from "react-router-dom";

export default function MyBidCard({ bid }) {
    const isLeading = bid.yourBid === bid.currentHighestBid;
    const navigate = useNavigate();

    const calculateTimeLeft = (expiry) => {
        const end = new Date(expiry.split('.')[0]); // 👈 FIX
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

    const onClickPlaceBid = () => {
        navigate("/retailer/crop-details/:cropId".replace(":cropId", bid.cropId));
    }

    return (
        <div className={`bid-card ${isLeading ? "leading" : "outbid"}`}>

            {/* 🔹 CARD HEADER */}
            <div className="bid-card-header">

                <span>
                    <span className={`status-badge ${bid.status.toLowerCase()}`}> {bid.status} </span>

                    {isLeading ? (
                        <span className="badge leading"><Crown size={19} /> Leading</span>
                    ) : (
                        <span className="badge outbid">Outbid</span>
                    )}
                </span>
                <h3 className="crop-title">
                    {bid.cropName} <span className="variety">({bid.variety})</span>
                </h3>
            </div>

            {/* 🔹 BODY */}
            <div className="bid-card-body">
                <p>
                    <strong>Quantity:</strong> {bid.quantity}{bid.unit}
                </p>
                <p>
                    <IndianRupee size={16} />
                    <strong> Current Highest:</strong> ₹{Number(bid.currentHighestBid).toLocaleString()}
                </p>

                <p className="your-bid">
                    <IndianRupee size={16} />
                    <strong> Your Bid:</strong> ₹{Number(bid.yourBid).toLocaleString()}

                </p>

                <p className="time-left">
                    <Clock size={16} />
                    <strong> Time Left:</strong> {calculateTimeLeft(bid.expiry)}
                </p>
                {bid.status === "ACTIVE" ? (
                    <button className="place-bid-btn" onClick={onClickPlaceBid}>
                        Place Higher Bid
                    </button>
                ) : (
                    <button className="auction-end-btn" disabled>
                        Auction Ended
                    </button>
                )}

            </div>
        </div>
    );
}
