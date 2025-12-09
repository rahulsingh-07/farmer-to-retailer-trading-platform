import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import '../css/SetPassword.css';

const SetPassword = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [token, setToken] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState('validate'); // 'validate' | 'form' | 'success'

  useEffect(() => {
    const urlToken = searchParams.get('token');
    if (urlToken) {
      setToken(urlToken);
      validateToken(urlToken);
    } else {
      toast.error('Invalid password reset link');
      navigate('/login');
    }
  }, [searchParams, navigate]);

  const validateToken = async (token) => {
  try {
    const response = await fetch(`http://localhost:8081/auth/validate-token?token=${token}`);
    if (!response.ok) throw new Error('Invalid or expired token');

    const data = await response.json();
    if (data.valid) {
      setStep('form');
    } else {
      throw new Error('Invalid or expired token');
    }
  } catch (err) {
    toast.error('Password reset link expired or invalid');
    navigate('/login');
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
      
      const response = await fetch(`http://localhost:8081/auth/set-password?token=${token}`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ password })
});

const data = await response.json();
if (!response.ok) {
  throw new Error(data.message || 'Failed to set password');
}

toast.success(data.message || 'Password set successfully! You can now login.');
setStep('success');
setTimeout(() => navigate('/login'), 3000);
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
