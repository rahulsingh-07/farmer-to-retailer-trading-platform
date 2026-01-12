import React from "react";
import {
  CheckCircle,
  CreditCard,
  Package,
  Truck,
  Home,
  CircleCheck
} from "lucide-react";
import { Rating } from "primereact/rating";
import { InputTextarea } from "primereact/inputtextarea";
import { Button } from "primereact/button";
import { useState } from "react";
import "./OrderStatusTracker.css";
import { toast } from "react-toastify";
import { addReview } from "../retailer/retailerService";

const STEPS = [
  { key: "PENDING", label: "Order Placed", icon: CircleCheck },
  { key: "CONFIRMED", label: "Order Confirmed", icon: CheckCircle },
  { key: "PAYMENT_PENDING", label: "Payment Initiated", icon: CreditCard },
  { key: "PAID", label: "Payment Confirmed", icon: Package },
  { key: "SHIPPED", label: "Shipped", icon: Truck },
  { key: "DELIVERED", label: "Delivered", icon: Home },
];
const STEP_TIME_MAP = {
  PENDING: "createdAt",
  CONFIRMED: "confirmedAt",
  PAYMENT_PENDING: "paymentAt",
  PAID: "paymentAt",
  SHIPPED: "shippedAt",
  DELIVERED: "deliveredAt"
};

// status: one of the keys above
// role: "FARMER" | "RETAILER"
export default function OrderStatusTracker({
  status = "PENDING",
  role,
  orderId,
  review,
  timestamps,
  reviewed,
  onConfirmOrder,
  onPay,
  onShip,
  onDelivered
}) {
  const [rating, setRating] = useState(null);
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const currentIndex = STEPS.findIndex(step => step.key === status);

  // Role-based action permissions
  const canConfirmOrder = role === "FARMER" && status === "PENDING";
  const canPay = role === "RETAILER" && status === "CONFIRMED";
  const canShip = role === "FARMER" && status === "PAID";
  const canMarkDelivered = role === "FARMER" && status === "SHIPPED";

  const formatDateTime = (date) => {
    if (!date) return null;
    return new Date(date).toLocaleString("en-IN", {
      day: "2-digit",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit"
    });
  };


  const submitReview = async () => {
    if (!rating) {
      toast.error("Please give a rating");
      return;
    }

    try {
      setSubmitting(true);
      await addReview(orderId, { rating, comment });
      toast.success("Review submitted successfully");

      // Reset local inputs
      setRating(null);
      setComment("");

      // Optional: trigger parent to reload order so `review` prop is updated
    } catch (err) {
      console.error(err);
      toast.error(err?.response?.data?.message || "Failed to submit review");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="status-tracker">
      {/* Steps */}
      {STEPS.map((step, index) => {
        const Icon = step.icon;
        const isCompleted = index < currentIndex;
        const isActive = index === currentIndex;
        const timeKey = STEP_TIME_MAP[step.key];
        const stepTime = timestamps?.[timeKey];

        return (
          <div className="status-step" key={step.key}>
            <div className="status-left">
              <span className={`status-dot ${isCompleted ? "completed" : isActive ? "active" : ""}`}>
                <Icon size={16} />
              </span>
              {index < STEPS.length - 1 && (
                <span className={`status-line ${isCompleted ? "completed" : ""}`} />
              )}
            </div>

            <div className="status-content">
              <h4 className={isCompleted || isActive ? "highlight" : ""}>{step.label}</h4>
              {stepTime && (isCompleted || isActive) && (
                <p className="status-time">
                  {formatDateTime(stepTime)}
                </p>
              )}

              {step.key === "PENDING" && canConfirmOrder && (
                <button className="action-btn" onClick={onConfirmOrder}>
                  Confirm Order <CircleCheck size={14} />
                </button>
              )}
              {step.key === "CONFIRMED" && canPay && (
                <button className="action-btn" onClick={onPay}>
                  Pay Now <CreditCard />
                </button>
              )}
              {step.key === "PAID" && canShip && (
                <button className="action-btn" onClick={onShip}>
                  Mark as Shipped
                </button>
              )}
              {step.key === "SHIPPED" && canMarkDelivered && (
                <button className="action-btn" onClick={onDelivered}>
                  Mark as Delivered
                </button>
              )}
            </div>
          </div>
        );
      })}

      {/* Review section: only show after delivered */}
      {status === "DELIVERED" && (
        <div className="review-section">

          {/* CASE 1: Review already exists (Farmer + Retailer both see this) */}
          {review && (
            <div className="review-display">
              <Rating
                value={review.rating}
                readOnly
                cancel={false}
              />
              {review.comment && (
                <p className="review-comment">{review.comment}</p>
              )}
            </div>
          )}

          {/* CASE 2: No review yet → ONLY retailer can give review */}
          {!review && role === "RETAILER" && (
            <div className="review-input">
              <Rating
                value={rating}
                onChange={(e) => setRating(e.value)}
                cancel={false}
              />

              <InputTextarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                rows={3}
                placeholder="Write your review"
              />

              <Button
                label={submitting ? "Submitting..." : "Submit Review"}
                onClick={submitReview}
                disabled={submitting}
              />
            </div>
          )}

          {/* CASE 3: No review + Farmer → show nothing */}
        </div>
      )}

    </div>
  );
}
