import React, { useState, useEffect } from 'react'
import AppCarousel from '../../../components/common/AppCarousel';
import { toast } from 'react-toastify';
import './CropDetails.css';

import { useParams } from 'react-router-dom';
import { MapPin, Clock1, FilePenLine, IndianRupee, ShoppingBasket, X, Save, CalendarDays } from "lucide-react";
import Button from '../../../components/common/Button';
import BackBtn from '../../../components/common/BackBtn';
import { getCropById, updateCrop } from '../farmerService';

export default function CropDetails() {
  const { cropId } = useParams();
  const [crop, setCrop] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editForm, setEditForm] = useState({
    pricePerUnit: '',
    quantity: '',
    description: ''
  });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const fetchCrop = async () => {
      try {
        setLoading(true);
        const response = await getCropById(cropId);
        const cropData = response.data;
        setCrop(cropData);
        // Pre-fill edit form
        setEditForm({
          quantity: cropData.quantity || '',
          description: cropData.description || '',
          pricePerUnit: cropData.pricePerUnit || ''
        });
      } catch (err) {
        toast.error('Crop not found');
        console.error('Crop fetch error:', err);
      } finally {
        setLoading(false);
      }
    };

    if (cropId) {
      fetchCrop();
    }
  }, [cropId]);

  if (loading) {
    return (
      <div className="crop-details-loading">
        <div className="skeleton-crop">
          <div className="skeleton-carousel"></div>
          <div className="skeleton-content">
            <div className="skeleton-line long"></div>
            <div className="skeleton-line"></div>
            <div className="skeleton-line"></div>
          </div>
        </div>
      </div>
    );
  }

  if (!crop) {
    return (
      <div className="crop-not-found">
        <h2>Crop not found</h2>
        <BackBtn />
      </div>
    );
  }

  const carouselSlides = Array.isArray(crop.imageUrl)
    ? crop.imageUrl.map(img => ({ image: img.imageUrl || img }))
    : [];

  //Format auction time left
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


  const canEditCrop = () => {
    if (crop.cropType === 'FIXED') return true;

    if (crop.cropType === 'AUCTION') {
      return !crop.bids || crop.bids.length === 0;
    }

    return false;
  };

  const handleEditClick = () => {
    setShowEditModal(true);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveEdit = async () => {
    try {
      setSaving(true);

      const response = await updateCrop(cropId, editForm);

      toast.success(response.message);

      setCrop(prev => ({
        ...prev,
        ...editForm
      }));

      setShowEditModal(false);
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Failed to update crop');
    } finally {
      setSaving(false);
    }
  };


  const handleCloseModal = () => {
    setShowEditModal(false);
  };

  const topBidders = Array.isArray(crop.bids)
    ? crop.bids.slice(0, 10).map(bid => ({
      bidderId: bid.bidderId,
      amount: bid.amount,
      createdAt: bid.createdAt
    }))
    : [];
  const isFixedPrice = crop.cropType === 'FIXED';
  return (
    <>
      <nav className="nav-bar">
        <div><BackBtn /></div>
        {crop.cropName} Details
        <div></div>
      </nav>

      <div className='crop-details-container'>
        <div className='cropDetails-main-content'>
          {carouselSlides.length > 0 ? (
            <AppCarousel
              slides={carouselSlides}
              height="450px"
              showCaptions={false}
            />
          ) : (
            <div className="no-images-placeholder">
              <img src="/placeholder-crop.png" alt="Crop" />
            </div>
          )}
          <div className='crop-details'>
            <h2>{crop.cropName} - {crop.variety}</h2>
            <p><strong><ShoppingBasket /> Quantity:</strong> {crop.quantity} {crop.unit}</p>
            <p><strong><MapPin /> Location:</strong> {crop.location}</p>
            <p><strong><IndianRupee /> Base Price:</strong> ₹{Number(crop.pricePerUnit).toLocaleString()}/{crop.unit}</p>
            <p>
              {!isFixedPrice && crop && (
              <>
                <strong>
                  <IndianRupee /> Current Bid:
                </strong>{" "}
                ₹{Number(crop.currentHighestBid ?? 0).toLocaleString()}/{crop.unit ?? ""}
              </>
            )}
            </p>

            <p>
              {crop.cropType === "AUCTION" && (
                <span className="time" title={displayTimeLeft}>
                  <strong><Clock1 /> Auction Ends: </strong>{displayTimeLeft}
                </span>
              )}
            </p>
            <p><strong> <CalendarDays/> Harvest Date:</strong> {crop.harvestDate ? new Date(crop.harvestDate).toLocaleDateString('en-IN') : 'N/A'}</p>
            <p className="crop-description">{crop.description}</p>
            {canEditCrop() && (
              <div className="edit-crop-btn-container">
                <Button
                  label="Edit Crop"
                  icon={<FilePenLine />}
                  bgColor="Green"
                  hoverBgColor="#111827"
                  onClick={handleEditClick}
                />
              </div>
            )}
          </div>

        </div>

        {crop.bids && crop.bids.length > 0 && (
          <div className='bidder'>
            <div className='bidder-title'>
              Top {Math.min(10, crop.bids.length)} Highest Bidders
              <span className="current-leader">
                Current Leader: ₹{Number(crop.currentHighestBid).toLocaleString()}
              </span>
            </div>
            <div className='bidder-list'>
              {topBidders.map((bid, index) => (
                <div key={bid.bidderId || index} className="bidder-item">
                  <span className="bidder-rank">#{index + 1}</span>
                  <span className="bidder-amount">
                    <IndianRupee size={14} />{Number(bid.amount).toLocaleString()}
                  </span>
                  <span className="bidder-time">
                    {new Date(bid.createdAt).toLocaleString()}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Edit Modal */}
      {showEditModal && (
        <div className="edit-modal-overlay" onClick={handleCloseModal}>
          <div className="edit-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Edit Crop Details</h3>
              <button className="modal-close" onClick={handleCloseModal}>
                <X size={24} />
              </button>
            </div>

            <div className="modal-body">
              <div className="form-group">
                <label>Base Price per {crop.unit}</label>
                <input
                  type="number"
                  name="pricePerUnit"
                  value={editForm.pricePerUnit}
                  onChange={handleInputChange}
                  className="form-input"
                  placeholder="Enter base price"
                  min="0"
                  step="0.01"
                />
              </div>

              <div className="form-group">
                <label>Quantity ({crop.unit})</label>
                <input
                  type="number"
                  name="quantity"
                  value={editForm.quantity}
                  onChange={handleInputChange}
                  className="form-input"
                  placeholder="Enter quantity"
                  min="0"
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                  name="description"
                  value={editForm.description}
                  onChange={handleInputChange}
                  className="form-textarea"
                  rows="4"
                  placeholder="Enter crop description"
                />
              </div>
            </div>

            <div className="modal-footer">
              <Button
                label="Cancel"
                bgColor="gray"
                onClick={handleCloseModal}
                disabled={saving}
              />
              <Button
                label={saving ? "Saving..." : "Save Changes"}
                icon={saving ? null : <Save />}
                bgColor="Green"
                onClick={handleSaveEdit}
                disabled={saving}
              />
            </div>
          </div>
        </div>
      )}
    </>

  )
}
