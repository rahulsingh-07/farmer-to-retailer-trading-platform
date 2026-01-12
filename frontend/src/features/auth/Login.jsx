import {useAuth} from '../../context/AuthContext.jsx';
import { useState,useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, LockKeyhole, UserPlus, KeyRound } from 'lucide-react';
import { toast } from 'react-toastify';
import './login.css';
export default function Login() { 
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const navigate = useNavigate();
  const { login: authLogin } = useAuth(); 

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
  console.log("Submitting login form with:", { username, password });
  if (!validateForm()) return;

  setSubmitting(true);
  try {
    const loggedInUser = await authLogin({ username, password });
    console.log("Login successful:", loggedInUser);

    const userRoles = loggedInUser?.role || [];
    console.log("user", loggedInUser);
    console.log("User roles:", userRoles);
    let dashboardPath = "/retailer/dashboard";
    if (userRoles.includes("FARMER")) dashboardPath = "/farmer/dashboard";
    if (userRoles.includes("ADMIN")) dashboardPath = "/admin/dashboard";
    if (userRoles.includes("RETAILER")) dashboardPath = "/retailer/dashboard";

    navigate(dashboardPath, { replace: true });
  } catch (err) {
    toast.error(err.message || "Login failed");
    setErrors({ general: err.message });
  } finally {
    setSubmitting(false);
  }
};



  return (
    <div className='login-page'>
      <div className='login-img'>
        <img src='/loginImg.jpg' alt="login illustration" />
      </div>

      <div className='login-context'>
        <h2> <User size={40} /> Login</h2>
        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">

            <label className="form-label" htmlFor="login-username">
              Username <span className="required">*</span>
            </label>
            <div className="input-wrapper">
              <User className="input-icon" size={18} style={{ left:"12px", }} />
              <input
                id="login-username"
                type="text"
                className={`form-input ${errors.username ? 'error' : ''}`}
                placeholder="Enter your username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
            </div>
            {errors.username && <p className="error-text">{errors.username}</p>}
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="login-password">
              Password <span className="required">*</span>
            </label>
            <div className="input-wrapper">

              <LockKeyhole className="input-icon" size={18} style={{ left: "12px" }} />
              <input
                id="login-password"
                type="password"
                className={`form-input ${errors.password ? 'error' : ''}`}
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
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
            <Link to="/registerFarmer" className="secondary-cta">
              <UserPlus /> Register as Farmer
            </Link>
          </p>
          <div className="secondary-cta-group">
            <Link to="/registerRetailer" className="secondary-cta"> <UserPlus /> Register as Retailer</Link>
            <Link to="/forgot-password" className="secondary-cta"><KeyRound /> Forgot password?</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
