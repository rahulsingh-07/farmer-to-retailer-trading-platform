import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import '../css/Login.css'; // New CSS file

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const { login, logout, user } = useAuth();
  const navigate = useNavigate();

  const validateForm = () => {
    const newErrors = {};
    if (!username.trim()) newErrors.username = "Username is required";
    if (!password.trim()) newErrors.password = "Password is required";
    else if (password.length < 6) newErrors.password = "Password must be at least 6 characters";
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setSubmitting(true);
    
    try {
      await login({ username, password });
      toast.success("Welcome back!");
      navigate('/dashboard');
    } catch (err) {
      const errmsg = err.message || 'Failed to login';
      toast.error(errmsg);
      setErrors({ ...errors, general: errmsg });
    } finally {
      setSubmitting(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Logged In State
  if (user) {
    return (
      <div className="logged-in-screen">
        <div className="floating-crops-login">
          <img src="/crops/fruits.png" 
               alt="Tomato" className="crop-login tomato-login" />
          <img src="/crops/veg.png" 
               alt="Carrot" className="crop-login carrot-login" />
        </div>
        
        <div className="logged-in-container">
          <div className="logged-in-card">
            <div className="success-icon-large">✅</div>
            <h2 className="logged-in-title">
              Welcome Back, <span className="username-highlight">{user.username}</span>!
            </h2>
            <p className="logged-in-subtitle">You're successfully logged into FarmFresh Connect</p>
            
            <Link to="/dashboard" className="dashboard-cta">
              Go to Dashboard
            </Link>
            
            <button onClick={handleLogout} className="logout-cta">
              Logout
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="login-page">
      <div className="floating-crops-login">
        <img src="/crops/fruits.png" 
             alt="Tomato" className="crop-login tomato-login" />
        <img src="/crops/veg.png" 
             alt="Carrot" className="crop-login carrot-login" />
      </div>
      
      <div className="login-container">
        <div className="login-card">
          <div className="login-header">
            <h1 className="login-title">Welcome Back</h1>
            <p className="login-subtitle">Sign in to your FarmFresh Connect account</p>
          </div>

          {errors.general && (
            <div className="message error">
              ❌ {errors.general}
            </div>
          )}

          <form onSubmit={handleSubmit} className="login-form">
            <div className="form-group">
              <label className="form-label" htmlFor="login-username">
                Username <span className="required">*</span>
              </label>
              <input
                id="login-username"
                type="text"
                value={username}
                onChange={(e) => {
                  setUsername(e.target.value);
                  if (errors.username) setErrors({ ...errors, username: '' });
                }}
                placeholder="Enter your username"
                className={`form-input ${errors.username ? 'error' : ''}`}
                disabled={submitting}
              />
              {errors.username && <p className="error-text">{errors.username}</p>}
            </div>

            <div className="form-group">
              <label className="form-label" htmlFor="login-password">
                Password <span className="required">*</span>
              </label>
              <input
                id="login-password"
                type="password"
                value={password}
                onChange={(e) => {
                  setPassword(e.target.value);
                  if (errors.password) setErrors({ ...errors, password: '' });
                }}
                placeholder="Enter your password"
                className={`form-input ${errors.password ? 'error' : ''}`}
                disabled={submitting}
              />
              {errors.password && <p className="error-text">{errors.password}</p>}
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="login-btn"
            >
              {submitting ? (
                <>
                  <span className="spinner" aria-hidden="true"></span>
                  <span className="spinner-text">Signing In...</span>
                </>
              ) : (
                "Sign In"
              )}
            </button>
          </form>

          <div className="login-footer">
            <p className="login-text">
              Don't have an account?{" "}
              <Link to="/registerFarmer" className="register-link">
                Register as Farmer
              </Link>
            </p>
            <div className="secondary-cta-group">
              <Link to="/registerRetailer" className="secondary-cta">Register as Retailer</Link>
              <Link to="/forgot-password" className="secondary-cta">Forgot password?</Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
