import { toast } from 'react-toastify';

export const useRazorpayPayment = () => {

  const startPayment = async ({cropOrderId, token }) => {
    try {
      //Create order from backend
      const res = await fetch(
        `http://localhost:8081/payment/order/${cropOrderId}/create`,
        {
          method: "POST",
          headers: token ? {
            'Authorization': `Bearer ${token}`,  // ✅ JWT header
            'Content-Type': 'application/json'
          } : {}
        }
      );

      if (!res.ok) {
        const errorData = await res.text();
        console.error('Backend error:', errorData);
        throw new Error(errorData || "Order creation failed");
      }

      const resJson = await res.json();
      const paymetnData=resJson.data;
      

      // Razorpay checkout options
      const options = {
        key: import.meta.env.VITE_RAZORPAY_KEY_ID,
        amount: Number.parseInt(paymetnData.amount),
        currency: paymetnData.currency,
        order_id: paymetnData.orderId,
        name: "Crop Marketplace",
        description: `Payment for Crop Order ${cropOrderId}`,

        handler: async (response) => {
          try {
            const verifyRes = await fetch("http://localhost:8081/payment/verify-payment", {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${localStorage.getItem("token")}`,
              },
              body: JSON.stringify({
                razorpayOrderId: response.razorpay_order_id,
                razorpayPaymentId: response.razorpay_payment_id,
                razorpaySignature: response.razorpay_signature,
                orderId: cropOrderId
              }),
            });

            const verifyJson = await verifyRes.json();
            if (verifyRes.ok) {
              toast.success(verifyJson.message || "Payment successful");
            } else {
              toast.error(verifyJson.error || "Payment verification failed");
            }
          } catch (verifyErr) {
            toast.error("Payment verification failed");
          }
        },

        prefill: {
          name: "Test User",
          email: "test@example.com",
          contact: "9999999999",
        },

        theme: { color: "#2e7d32" }
      };
     
      if (!window.Razorpay) {
        toast.error("Payment service not loaded");
        return;
      }

      const rzp = new window.Razorpay(options);

      rzp.on("payment.failed", async (response) => {
        console.error("Payment failed:", response);
        toast.error(response.error.description || "Payment failed");
      });

      rzp.open();

    } catch (err) {
      console.error("Payment error:", err);
      toast.error("Payment initiation failed");
    }
  };

  return { startPayment };
};