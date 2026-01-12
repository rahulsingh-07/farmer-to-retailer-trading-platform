import React, { useEffect, useState } from "react";
import { toast } from "react-toastify";
import { getMyBids } from "../retailerService";
import MyBidCard from "./MyBidCard";
import "./MyBidPage.css";

export default function MyBidsPage() {
  const [bids, setBids] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBids = async () => {
      try {
        setLoading(true);
        const res = await getMyBids();
        setBids(res?.data || []);
      } catch (err) {
        toast.error("Failed to load your bids");
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchBids();
  }, []);

  if (loading) return <p>Loading your bids...</p>;
  if (!bids.length) return <p>You have not placed any bids yet.</p>;

  return (
    <div className="my-bids-container">
      <h2>My Active Bids</h2>

      <div className="bids-grid">
        {bids.map(bid => (
          <MyBidCard key={bid.auctionId} bid={bid} />
        ))}
      </div>
    </div>
  );
}
