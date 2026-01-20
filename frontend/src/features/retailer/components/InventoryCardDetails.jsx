import React, { useState, useEffect, useCallback } from 'react';
import { toast } from 'react-toastify';
import { useParams } from 'react-router-dom';
import AppCarousel from '../../../components/common/AppCarousel';
import {
  MapPin,
  Clock1,
  ReceiptIndianRupee,
  IndianRupee,
  ShoppingBasket,
  FileSpreadsheet,
  CalendarDays
} from "lucide-react";
import Button from '../../../components/common/Button';
import BackBtn from '../../../components/common/BackBtn';
import CropCard from './CropCard';
import {
  buyNow,
  getCropById,
  placeBid,
  getFilteredCrops
} from '../retailerService';
import { useRazorpayPayment } from '../../../hooks/useRazorpayPayment';
import './InventoryCardDetails.css';

export default function InventoryCardDetails() {
  const { cropId } = useParams();

  const [crop, setCrop] = useState(null);
  const [crops, setCrops] = useState([]);
  const [bidAmount, setBidAmount] = useState('');
  const [loading, setLoading] = useState(true);

  const { startPayment } = useRazorpayPayment();

  /* =========================
     FETCH SIMILAR CROPS
  ========================== */
  const fetchSimilarCrops = useCallback(async (baseCrop) => {
    if (!baseCrop) return;

    try {
      const filters = {
        category: baseCrop.category,
        page: 0,
        size: 4
      };

      const data = await getFilteredCrops(filters);

      const result = Array.isArray(data?.content)
        ? data.content
          .filter(item => String(item.id) !== String(baseCrop.cropId)) // ⬅ skip same crop
          .slice(0, 4)
        : [];

      setCrops(result);
    } catch (err) {
      toast.error(err?.message || 'Failed to load similar crops');
    }
  }, []);

  /* =========================
     FETCH MAIN CROP
  ========================== */
  useEffect(() => {
    const fetchCrop = async () => {
      try {
        setLoading(true);
        const response = await getCropById(cropId);
        setCrop(response.data);
        fetchSimilarCrops(response.data);
      } catch (err) {
        toast.error('Crop not found');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    if (cropId) fetchCrop();
  }, [cropId, fetchSimilarCrops]);

  /* =========================
     AUCTION TIME
  ========================== */
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

  /* =========================
     BID HANDLERS
  ========================== */
  const handleBidChange = (e) => {
    const value = e.target.value;
    if (/^\d*\.?\d*$/.test(value)) setBidAmount(value);
  };

  const handlePlaceBid = async () => {
    if (!bidAmount || Number(bidAmount) <= Number(crop?.currentHighestBid || 0)) {
      toast.error('Bid must be higher than current bid');
      return;
    }

    try {
      await placeBid(crop.auctionId, {
        amount: Number(bidAmount).toFixed(2)
      });

      toast.success('Bid placed successfully');
      setBidAmount('');

      const updated = await getCropById(cropId);
      setCrop(updated.data);
    } catch (err) {
      toast.error(err.message || 'Bid failed');
    }
  };

  /* =========================
     BUY NOW
  ========================== */
  const handleBuyNow = async () => {
    try {
      const res = await buyNow(cropId);
      await startPayment({
        cropOrderId: res.data,
        token: localStorage.getItem('token')
      });
    } catch (err) {
      toast.error(err.message || 'Buy now failed');
    }
  };

  if (loading) return <div className="loading">Loading...</div>;
  if (!crop) return <div className="error">Crop not found</div>;

  const carouselSlides = Array.isArray(crop.imageUrl)
    ? crop.imageUrl.map(img => ({ image: img.imageUrl || img }))
    : [];

  const isFixedPrice = crop.cropType === 'FIXED';
  const carouselHeight = crop.cropType === 'FIXED' ? '468px' : '570px';


  /* =========================
     RENDER
  ========================== */
  return (
    <>
      <nav className="nav-bar">
        <BackBtn />
        Crop Details
        <div />
      </nav>

      <div className="inventory-layout">
        {/* LEFT : IMAGE */}
        <div className="left-panel">
          <AppCarousel
            slides={carouselSlides}
            height={carouselHeight}
            showCaptions={false}
          />
        </div>

        {/* RIGHT : DETAILS + BID */}
        <div className="right-panel">
          {/* CROP INFO CARD */}
          <div className="info-card">
            <h1 className="crop-title" style={{ fontSize: '2rem', fontWeight: 'bold' }}>
              {crop.cropName} - {crop.variety}
            </h1>

            <div className="info-row">
              <ShoppingBasket size={18} />
              <span><strong>Quantity:</strong> {crop.quantity} {crop.unit}</span>
            </div>

            <div className="info-row">
              <MapPin size={18} />
              <span><strong>Location:</strong> {crop.location}</span>
            </div>

            <div className="info-row">
              <IndianRupee size={18} />
              <span>
                <strong>Price:</strong> ₹{Number(crop.pricePerUnit).toLocaleString()}/{crop.unit}
              </span>
            </div>

            <div className="info-row">
              <CalendarDays size={18} />
              <span><strong>Harvest On:</strong> {new Date(crop.harvestDate).toLocaleDateString()}</span>
            </div>

            {crop.cropType === "AUCTION" && (
              <div className="info-row">
                <Clock1 size={18} />
                <span><strong>Time Left:</strong> {displayTimeLeft}</span>
              </div>
            )}

            <div className="info-row">
              <FileSpreadsheet size={18} />
              <span><strong>Description:</strong> {crop.description || "N/A"}</span>
            </div>
          </div>

          {/* BID CARD */}
          <div className="place-bid-card">
            <div className="bid-header">
              <div>
                <span className="label">
                  {isFixedPrice ? "Fixed Price" : "Current Bid"}
                </span>
                <h3>
                  ₹{Number(
                    isFixedPrice
                      ? crop.pricePerUnit
                      : crop.currentHighestBid || crop.pricePerUnit
                  ).toLocaleString()}
                  /{crop.unit}
                </h3>
              </div>

              {!isFixedPrice && (
                <button
                  className="suggested-chip"
                  onClick={() =>
                    setBidAmount(Number(crop.currentHighestBid || 0) + 6)
                  }
                >
                  +₹6 suggested
                </button>
              )}
            </div>

            {isFixedPrice ? (
              <Button style={{ width: "100%" }}
                label="Buy Now"
                icon={<ShoppingBasket />}
                bgColor="Green"
                hoverBgColor="#111827"
                onClick={handleBuyNow}
                fullWidth
              />
            ) : (
              <>
                <div className="bid-input">
                  <input
                    type="number"
                    placeholder="Enter bid amount"
                    value={bidAmount}
                    onChange={handleBidChange}
                    min={Number(crop.currentHighestBid || 0) + 1}
                  />
                  <span>/{crop.unit}</span>
                </div>

                <Button style={{ width: "100%" }}
                  label="Place Bid"
                  icon={<ReceiptIndianRupee />}
                  bgColor="Green"
                  hoverBgColor="#111827"
                  onClick={handlePlaceBid}
                  disabled={
                    !bidAmount ||
                    Number(bidAmount) <= Number(crop.currentHighestBid || 0)
                  }
                  fullWidth
                />
              </>
            )}
          </div>
        </div>
      </div>


      {/* =========================
          SIMILAR CROPS
      ========================== */}
      <div className="similar-products-section">
        {crops.length ? (
          crops.map(crop => (
            <CropCard
              key={crop.id}
              crop={{
                ...crop,
                image: crop.images?.[0] || null
              }}
            />
          ))
        ) : (
          <div className="no-similar-crops">
            <div>
              <p>No similar crops found</p>
            </div>
          </div>
        )}
      </div>
    </>
  );
}
