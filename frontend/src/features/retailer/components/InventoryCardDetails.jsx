import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { useParams } from 'react-router-dom';
import AppCarousel from '../../../components/common/AppCarousel';
import { MapPin, Clock1, ReceiptIndianRupee, IndianRupee, ShoppingBasket, FileSpreadsheet } from "lucide-react";
import Button from '../../../components/common/Button';
import BackBtn from '../../../components/common/BackBtn';
import { buyNow, getCropById, placeBid } from '../retailerService';
import { useAuth } from '../../../context/AuthContext';
import { useRazorpayPayment } from '../../../hooks/useRazorpayPayment';
import './InventoryCardDetails.css';

export default function InventoryCardDetails() {
  const { cropId } = useParams();
  const [crop, setCrop] = useState(null);
  const [bidAmount, setBidAmount] = useState('');
  const [loading, setLoading] = useState(true);
  const { startPayment } = useRazorpayPayment();

  // Fetch crop details
  useEffect(() => {
    const fetchCrop = async () => {
      try {
        setLoading(true);
        const response = await getCropById(cropId);
        setCrop(response.data); // Correctly set crop from response.data
      } catch (err) {
        toast.error('Crop not found');
        console.error('Crop fetch error:', err.response || err);
      } finally {
        setLoading(false);
      }
    };

    if (cropId) fetchCrop();
  }, [cropId]);

    const calculateTimeLeft = (endTime) => {
  if (!endTime || crop?.cropType?.toUpperCase() !== "AUCTION") return "N/A";

  const end = new Date(endTime.split('.')[0]); // 👈 FIX
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

  const displayTimeLeft = calculateTimeLeft(crop?.auctionEndTime);

  // Handle bid input changes
  const handleBidChange = (e) => {
    const value = e.target.value;
    if (/^\d*\.?\d*$/.test(value) || value === '') {
      setBidAmount(value);
    }
  };

  // Place bid
  const handlePlaceBid = async () => {
    if (!bidAmount || Number(bidAmount) <= Number(crop?.currentHighestBid || 0)) {
      toast.error('Bid must be higher than current bid!');
      return;
    }

    if (!crop?.auctionId) {
      toast.error('Auction not available');
      return;
    }

    try {
      const bigDecimalAmount = Number(bidAmount).toFixed(2);
      await placeBid(crop.auctionId, { amount: bigDecimalAmount });
      toast.success(`Bid placed: ₹${bigDecimalAmount}`);
      setBidAmount('');

      // Refresh crop details
      const updatedCropResp = await getCropById(cropId);
      setCrop(updatedCropResp.data);
    } catch (err) {
      toast.error(err.message || 'Failed to place bid');
      console.error('Bid error:', err.response || err);
    }
  };

  // Handle payment for fixed price crops
  const handleBuyNow = async () => {
  try {
    const res = await buyNow(cropId);

    if (!res?.data) {
      throw new Error("Empty response from server");
    }

    const order = res.data;
    if (!order) {
      throw new Error("Invalid order response");
    }

    await startPayment({
      cropOrderId: order,
      token:localStorage.getItem('token')
    });

  } catch (err) {
    toast.error(err.message || "Buy now failed");
  }
};


  if (loading) return <div className="loading">Loading crop details...</div>;
  if (!crop) return <div className="error">Crop not found</div>;

  const carouselSlides = Array.isArray(crop.imageUrl)
    ? crop.imageUrl.map(img => ({ image: img.imageUrl || img }))
    : [];

  const isFixedPrice = crop.cropType === 'FIXED';

  return (
    <>
      <nav className="nav-bar">
        <div><BackBtn /></div>
        Crop Details
        <div></div>
      </nav>

      <div className='inventory-cardDetails'>
        <div className='carousel-and-bid'>
          <AppCarousel slides={carouselSlides} height="450px" showCaptions={false} />

          {/* BID / BUY SECTION */}
          <div className="bid-section">
            <div className="current-bid-info">
              <span>
                {isFixedPrice
                  ? `Fixed Price: ₹${Number(crop.pricePerUnit || 0).toLocaleString()}/${crop.unit || ''}`
                  : `Current Bid: ₹${Number(crop.currentHighestBid || crop.pricePerUnit || 0).toLocaleString()}/${crop.unit || ''}`
                }
              </span>
              {!isFixedPrice && <span className="bid-suggestion">Suggested: +₹6</span>}
            </div>

            {isFixedPrice ? (
              <div className="buy-input-group">
                <Button
                  label="Buy Now"
                  icon={<ShoppingBasket />}
                  bgColor="Green"
                  hoverBgColor="#111827"
                  onClick={handleBuyNow}
                  disabled={loading}
                />
              </div>
            ) : (
              <div className="bid-input-group">
                <input
                  type="number"
                  className="bid-input"
                  value={bidAmount}
                  onChange={handleBidChange}
                  placeholder="Enter bid amount"
                  min={Number(crop.currentHighestBid || crop.pricePerUnit || 0) + 1}
                  step="1"
                />
                <span className="unit-label">/{crop.unit || ''}</span>
                <Button
                  label="Place Bid"
                  icon={<ReceiptIndianRupee />}
                  bgColor="Green"
                  hoverBgColor="#111827"
                  onClick={handlePlaceBid}
                  disabled={!bidAmount || Number(bidAmount) <= Number(crop.currentHighestBid || 0)}
                />
              </div>
            )}
          </div>
        </div>

        <div className='crop-details'>
          <h2>{crop.cropName} - {crop.variety}</h2>
          <p><strong><ShoppingBasket /> Quantity:</strong> {crop.quantity} {crop.unit}</p>
          <p><strong><MapPin /> Location:</strong> {crop.location}</p>
          <p><strong><IndianRupee /> Price:</strong> ₹{Number(crop.pricePerUnit || 0).toLocaleString()}/{crop.unit || ''}</p>
          <p>
          {crop.cropType === "AUCTION" && (
              <span className="time" title={displayTimeLeft}>
               <strong><Clock1 /> Time Left: </strong>{displayTimeLeft}
              </span>
            )}
          </p>
          <p><strong><FileSpreadsheet /> Description:</strong> {crop.description || "N/A"}</p>
        </div>
      </div>
    </>
  );
}
