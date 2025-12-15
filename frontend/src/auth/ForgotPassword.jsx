import React, { useState } from "react";
import { toast } from "react-toastify";
import api from "../utils/api";
import "../css/ForgotPassword.css";

const ForgotPassword = () => {
    const [email, setEmail] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [message, setMessage] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage("");

        if (!email.trim()) {
            toast.error("Email is required");
            return;
        }

        try {
            setSubmitting(true);
            const res = await api.post("/auth/forgetPassword", { email: email.trim() });
            const successMsg = res?.message || "Reset link sent to your email";
            setMessage(successMsg);
            toast.success(successMsg);
        } catch (err) {
            const errorMsg = err.response?.data?.message || "Unable to send reset link";
            setMessage(errorMsg);
            toast.error(errorMsg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="fp-page">
            <div className="floating-crops-login">
                <img src="/crops/fruits.png" alt="Tomato" className="crop-login tomato-login" />
                <img src="/crops/veg.png" alt="Carrot" className="crop-login carrot-login" />
            </div>

            <div className="fp-container">
                <div className="fp-card">
                    <div className="fp-header">
                        <p className="fp-kicker">Password reset</p>
                        <h1>Forgot your password?</h1>
                        <p className="fp-subtitle">
                            Enter your account email and we'll send a secure link to reset your password.
                        </p>
                    </div>

                    {message && (
                        <div className={`fp-banner ${message.toLowerCase().includes("unable") ? "error" : "success"}`}>
                            {message}
                        </div>
                    )}

                    <form className="fp-form" onSubmit={handleSubmit}>
                        <label className="fp-label">Email address</label>
                        <input
                            type="email"
                            className="fp-input"
                            placeholder="you@example.com"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />

                        <button type="submit" className="fp-btn" disabled={submitting}>
                            {submitting ? "Sending..." : "Send reset link"}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default ForgotPassword;
