import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom'; // ✅ Route param
import { toast } from 'react-toastify';
import AppCarousel from '../../components/common/AppCarousel';
import { MapPin, Clock1, IndianRupee, ShoppingBasket } from "lucide-react";
import BackBtn from '../../components/common/BackBtn';
import OrderStatusTracker from './OrderStatusTracker';
import { getRetailerOrderById } from '../retailer/retailerService';
import { getFarmerOrderById, confirmOrderApi, shipOrderApi, deliverOrderApi } from '../farmer/farmerService';
import './OrderDetails.css';
import OtpInput from '../../components/common/OtpInput';
import { useAuth } from '../../context/AuthContext'
import { useRazorpayPayment } from '../../hooks/useRazorpayPayment';

export default function OrderDetails() {
  const { orderId } = useParams(); // ✅ /retailer/orders/{orderId}
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [currentStatus, setCurrentStatus] = useState('PENDING');
  const { user } = useAuth();
  const { startPayment } = useRazorpayPayment();
  const [otpDigits, setOtpDigits] = useState(Array(6).fill(""));
  const [confirming, setConfirming] = useState(false);
  const [showOtpModal, setShowOtpModal] = useState(false);


  useEffect(() => {
    const fetchOrder = async () => {
      if (!orderId || !user) {
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        let orderData;

        //Retailer → getRetailerOrderById | Farmer → getFarmerOrderById
        if (user.role === 'RETAILER' || user.username?.startsWith('RETL')) {
          const data = await getRetailerOrderById(orderId);
          orderData = data.data;

        } else {
          const data = await getFarmerOrderById(orderId);
          orderData = data.data;
        }
        setOrder(orderData);
        setCurrentStatus(orderData?.status || 'PENDING');
      } catch (err) {
        toast.error(err.message || 'Order not found');
        console.error('Order fetch error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchOrder();
  }, [orderId, user]);


  console.log('Order Details - Order:', order);

  //Farmer: Confirm Order (PENDING → CONFIRMED)
  const handleConfirmOrder = async () => {
    if (!window.confirm("Confirm this order? Retailer will be notified.")) return;

    try {
      const res = await confirmOrderApi(orderId);
      toast.success(res.message || "Order confirmed successfully!");
      // Trigger parent refresh or status update
      window.location.reload(); // Or use state updater
    } catch (error) {
      toast.error("Failed to confirm order");
      console.error(error);
    }
  };

  // Retailer: Pay for Order (CONFIRMED → PAYMENT_CONFIRMED)
  const handlePay = async () => {
    const token = user?.token || user?.accessToken || localStorage.getItem('token');
    if (!order) return;
    try {
      await startPayment({
        cropOrderId: orderId,
        token: token
      });
    } catch (err) {
      toast.error("Payment initiation failed");
      console.error(err);
    }
  };


  //Farmer: Mark Shipped (PAYMENT_CONFIRMED → SHIPPED)
  const handleShip = async () => {
    const confirmed = window.confirm("Mark this order as shipped? An OTP will be sent to your email.");
    if (!confirmed) return;
    try {
      await shipOrderApi(orderId);

      toast.success("Order marked as shipped. OTP sent to your email.");
      // better than full reload
      setOrder(prev => ({
        ...prev,
        status: "SHIPPED"
      }));

    } catch (error) {
      toast.error(
        error?.response?.data?.message || "Failed to mark order as shipped"
      );
      console.error(error);
    }
  };

  //Farmer: Confirm Delivery (SHIPPED → DELIVERED)

  const handleConfirmDelivery = async () => {
    const otp = otpDigits.join("");

    if (otp.length !== 6) {
      toast.error("Please enter all 6 digits");
      return;
    }

    try {
      setConfirming(true);
      const res = await deliverOrderApi(orderId, { otp });

      toast.success(res.message || "Delivery confirmed");

      setShowOtpModal(false);
      setOtpDigits(Array(6).fill(""));   // reset
      setCurrentStatus("DELIVERED");
      setOrder(prev => ({ ...prev, status: "DELIVERED" }));
    } catch (err) {
      toast.error(err?.response?.data?.message || "Invalid OTP");
    } finally {
      setConfirming(false);
    }
  };

  if (loading) {
    return (
      <div className="order-details-loading">
        <div className="skeleton-order">
          <div className="skeleton-image"></div>
          <div className="skeleton-content">
            <div className="skeleton-line"></div>
            <div className="skeleton-line"></div>
            <div className="skeleton-line"></div>
          </div>
        </div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="order-not-found">
        <h2>Order not found</h2>
        <BackBtn />
      </div>
    );
  }

  // ✅ Dynamic carousel from order.imageUrl
  const carouselSlides = Array.isArray(order.imageUrl)
    ? order.imageUrl.map(img => ({ image: img.imageUrl || img }))
    : [];

  return (
    <>
      <nav className="nav-bar">
        <div><BackBtn /></div>
        {(user.role === 'RETAILER' || user.username?.startsWith('RETL'))
          ? `Order #${order?.orderId}`
          : `Sale #${order?.orderId}`
        }
        <div></div>
      </nav>

      <div className='order-details-container'>
        <div className='order-content'>
          {/* Dynamic crop images */}
          {carouselSlides.length > 0 ? (
            <AppCarousel
              slides={carouselSlides}
              showCaptions={false}
            />
          ) : (
            <div className="no-images-placeholder">
              <img src="/placeholder-order.png" alt="Order" />
            </div>
          )}

          <div className='order-details'>
            <h2>{order.cropName} - {order.variety}</h2>
            <p><strong><ShoppingBasket /> Quantity:</strong> {order.quantity} kg</p>
            <p><strong><MapPin /> Pick up Location:</strong> {order.location}</p>
            {order.cropType === "FIXED" ? (
              <p>
                <strong><IndianRupee /> Price:</strong>{" "}
                ₹{Number(order.finalPrice || 0).toLocaleString()}
              </p>
            ) : (
              <>
                <p>
                  <strong><IndianRupee /> Final Price:</strong>{" "}
                  ₹{Number(order.finalPrice || 0).toLocaleString()}
                </p>
                <p>
                  <strong><IndianRupee /> Base Price:</strong>{" "}
                  ₹{Number(order.pricePerUnit || 0).toLocaleString()}
                </p>
              </>
            )}
            <p><strong><Clock1 /> Ordered:</strong> {new Date(order.createdAt).toLocaleDateString('en-IN')}</p>

            <p><strong><MapPin /> Delivery Location:</strong> {order.retailerAddress}</p>


          </div>
        </div>

        {/* OrderStatusTracker with real status */}
        <div className='status-tracker'>
          <OrderStatusTracker
            status={currentStatus}
            role={user.role === 'RETAILER' || user.username?.startsWith('RETL') ? "RETAILER" : "FARMER"}
            orderId={order.orderId}
            review={order.review}
            reviewed={order.reviewed}
            timestamps={{
              createdAt: order.createdAt,
              confirmedAt: order.confirmedAt,
              paymentAt: order.paymentAt,
              shippedAt: order.shippedAt,
              deliveredAt: order.deliveredAt
            }}
            orderAmount={order.totalAmount}
            onConfirmOrder={handleConfirmOrder}
            onPay={handlePay}
            onShip={handleShip}
            onDelivered={() => setShowOtpModal(true)}
          />
        </div>
      </div>
      {showOtpModal && (
        <div className="otp-modal-backdrop">
          <div className="otp-modal">
            <h3>Confirm Delivery</h3>
            <p>Enter the OTP shared by the retailer</p>

            <OtpInput
              length={6}
              value={otpDigits}
              onChange={setOtpDigits}
              disabled={confirming}
            />

            <div className="otp-actions">
              <button
                className="btn-cancel"
                onClick={() => setShowOtpModal(false)}
                disabled={confirming}
              >
                Cancel
              </button>

              <button
                className="btn-confirm"
                onClick={handleConfirmDelivery}
                disabled={confirming}
              >
                {confirming ? "Confirming..." : "Submit"}
              </button>
            </div>
          </div>
        </div>
      )}


    </>
  );
}
