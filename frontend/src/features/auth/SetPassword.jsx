import React, { useState, useEffect,useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import './SetPassword.css';
import { validateResetToken, setNewPassword } from '../../services/authService';

const SetPassword = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [token, setToken] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState('validate'); // 'validate' | 'form' | 'success'
  const hasValidatedRef = useRef(false);

useEffect(() => {
  const urlToken = searchParams.get("token");
  if (!urlToken) {
    toast.error("Invalid password reset link");
    navigate("/login");
    return;
  }

  if (hasValidatedRef.current) return;
  hasValidatedRef.current = true;

  setToken(urlToken);
  validateToken(urlToken);
}, [searchParams, navigate]);


  const validateToken = async (token) => {
  try {
    const response = await validateResetToken(token);

    // response shape:
    // { success, message, data: true/false }

    if (response.data === true) {
      setStep("form");
    } else {
      throw new Error(response.message || "Invalid or expired token");
    }
  } catch (err) {
    toast.error(err.message);
    navigate("/login");
  }
};




  const handleSubmit = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      toast.error('Passwords do not match');
      return;
    }

    if (password.length < 6) {
      toast.error('Password must be at least 6 characters');
      return;
    }

    try {
      setLoading(true);

      const response = await setNewPassword(token, password);

      toast.success(response.message || "Password set successfully");
      setStep("success");

      setTimeout(() => navigate("/login"), 3000);
    } catch (err) {
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Token validation step
  if (step === 'validate') {
    return (
      <div className="set-password-page">
        <div className="loading-container">
          <div className="spinner-large"></div>
          <p>Validating password reset link...</p>
        </div>
      </div>
    );
  }

  // Success step
  if (step === 'success') {
    return (
      <div className="set-password-page">
        <div className="success-container">
          <div className="success-icon">✅</div>
          <h2>Password Set Successfully!</h2>
          <p>Redirecting to login...</p>
        </div>
      </div>
    );
  }

  // Password form step
  return (
    <div className="set-password-page">
      <div className="set-password-card">
        <div className="form-header">
          <h1>Set Your New Password</h1>
          <p>This is a one-time link. Create a strong password to access your account.</p>
        </div>

        <form onSubmit={handleSubmit} className="password-form">
          <div className="form-group">
            <label>New Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter new password (min 6 chars)"
              className="form-input"
              required
              minLength={6}
            />
          </div>

          <div className="form-group">
            <label>Confirm Password</label>
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Confirm new password"
              className="form-input"
              required
            />
          </div>

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? 'Setting Password...' : 'Set Password'}
          </button>
        </form>

        <div className="form-footer">
          <button onClick={() => navigate('/login')} className="back-btn">
            Back to Login
          </button>
        </div>
      </div>
    </div>
  );
};

export default SetPassword;
