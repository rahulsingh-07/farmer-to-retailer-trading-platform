import React, { useState } from 'react';
import { Link ,useNavigate} from 'react-router-dom';
import { User, Mail, Phone, CheckCircle, Building2 } from 'lucide-react';
import { toast } from 'react-toastify';
import { createAdminUser } from './adminService';
import '../auth/Register.css';
export default function AddAdmin() {
  const navigate = useNavigate();
  
    const [formData, setFormData] = useState({
      fullName: "",
      email: "",
      phoneNumber: "",
    });
  
    const [errors, setErrors] = useState({});
    const [submitting, setSubmitting] = useState(false);
  
  
  
    const validateForm = () => {
      const newErrors = {};
      if (!formData.fullName.trim()) newErrors.fullName = "Name cannot be empty";
      if (!formData.email.trim()) newErrors.email = "Email is required";
      else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) newErrors.email = "Enter valid email";
      if (!formData.phoneNumber.trim()) {
        newErrors.phoneNumber = "Phone number is required";
      } else if (!/^[0-9]{10}$/.test(formData.phoneNumber)) {
        newErrors.phoneNumber = "Phone number must be 10 digits only";
      }
      
      setErrors(newErrors);
      return Object.keys(newErrors).length === 0;
    };
  
    const handleChange = (e) => {
      const { name, value } = e.target;
      setFormData({ ...formData, [name]: value });
      if (errors[name]) {
        setErrors({ ...errors, [name]: "" });
      }
    };
  
    const handleSubmit = async (e) => {
      e.preventDefault();
  
      if (!validateForm()) return;
  
      setSubmitting(true);
  
      try {
        await createAdminUser({
          fullName: formData.fullName,
          email: formData.email,
          phoneNumber: formData.phoneNumber
        });
  
        toast.success(
          "Registration submitted successfully! Link sent to your email."
        );
  
        navigate("/");
      } catch (err) {
        toast.error(
          err.response?.data?.message ||
          "Registration failed. Please try again."
        );
      }finally {
        setSubmitting(false);
      }
    };
  
    return (
      <div className='register-page'>
        <div className='register-img'>
          <img src='/adminImg.jpg' alt="admin" />
        </div>
  
        <div className='register-context'>
          <h2>
            <User className="inline-block mr-3" size={32} />
             Add Admin
          </h2>
  
          <form onSubmit={handleSubmit} className="register-form">
            {/* Full Name */}
            <div className="form-group">
              <label className="form-label" htmlFor="fullName">
                Full Name <span className="required">*</span>
              </label>
              <div className="input-wrapper">
                <User className="input-icon" size={18} style={{ left: "12px" }} />
                <input
                  id="fullName"
                  name="fullName"
                  type="text"
                  className={`form-input ${errors.fullName ? 'error' : ''}`}
                  placeholder="Enter your full name"
                  value={formData.fullName}
                  onChange={handleChange}
                  disabled={submitting}
                />
              </div>
              {errors.fullName && <p className="error-text">{errors.fullName}</p>}
            </div>
  
            {/* Email */}
            <div className="form-group">
              <label className="form-label" htmlFor="email">
                Email <span className="required">*</span>
              </label>
              <div className="input-wrapper">
                <Mail className="input-icon" size={18} style={{ left: "12px" }} />
                <input
                  id="email"
                  name="email"
                  type="email"
                  className={`form-input ${errors.email ? 'error' : ''}`}
                  placeholder="Enter your email"
                  value={formData.email}
                  onChange={handleChange}
                  disabled={submitting}
                />
              </div>
              {errors.email && <p className="error-text">{errors.email}</p>}
            </div>
  
            {/* Phone Number */}
            <div className="form-group">
              <label className="form-label" htmlFor="phoneNumber">
                Phone Number <span className="required">*</span>
              </label>
              <div className="input-wrapper">
                <Phone className="input-icon" size={18} style={{ left: "12px" }} />
                <input
                  id="phoneNumber"
                  name="phoneNumber"
                  type="tel"
                  maxLength="10"
                  minLength={10}
                  className={`form-input ${errors.phoneNumber ? 'error' : ''}`}
                  placeholder="Enter 10-digit phone number"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  disabled={submitting}
                />
              </div>
              {errors.phoneNumber && <p className="error-text">{errors.phoneNumber}</p>}
            </div>
  
            <button
              type="submit"
              disabled={submitting}
              className="login-btn"
            >
              {submitting ? (
                <>
                  <span className="spinner" aria-hidden="true"></span>
                  <span className="spinner-text">Registering...</span>
                </>
              ) : (
                <>
                  <CheckCircle className="inline-block mr-2" size={20} style={{ left: "12px" }} />
                  Create Admin Account
                </>
              )}
            </button>
          </form>
        </div>
      </div>
    );
}
