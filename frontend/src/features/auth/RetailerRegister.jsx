import React, { useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Phone, MapPin, FileText, CheckCircle, Upload, Building2 } from 'lucide-react';
import { toast } from 'react-toastify';
import { registerRetailer } from '../../services/authService'; // ✅ Use your service
import './Register.css';

export default function RetailerRegister() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    phoneNumber: "",
    businessAddress: "",
    tradeLicenseFile: null,
    tradeLicenseFileName: "",
  });

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [dragging, setDragging] = useState(false);

  // ✅ File upload handler (unchanged - perfect)
  const handleFileChange = useCallback((e) => {
    const file = e.target.files[0];
    if (file) {
      const allowedTypes = ['application/pdf', 'image/png', 'image/jpeg', 'image/jpg'];
      if (!allowedTypes.includes(file.type)) {
        toast.error('Please upload PDF or image files (PNG, JPG)');
        return;
      }
      if (file.size > 5 * 1024 * 1024) {
        toast.error('File size must be less than 5MB');
        return;
      }

      setFormData({
        ...formData,
        tradeLicenseFile: file,
        tradeLicenseFileName: file.name
      });
      if (errors.tradeLicenseFile) {
        setErrors({ ...errors, tradeLicenseFile: '' });
      }
      toast.success('✅ PDF uploaded successfully');
    }

  }, [formData, errors]);

  const handleDragOver = (e) => {
    e.preventDefault();
    setDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    setDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragging(false);
    const file = e.dataTransfer.files[0];
    if (file) {
      const syntheticEvent = { target: { files: [file] } };
      handleFileChange(syntheticEvent);
    }
  };

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
    if (!formData.businessAddress.trim()) newErrors.businessAddress = "Address is required";
    if (!formData.tradeLicenseFile) newErrors.tradeLicenseFile = "Trade license PDF is required";

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
      const fd = new FormData();

      fd.append("fullName", formData.fullName);
      fd.append("email", formData.email);
      fd.append("phoneNumber", formData.phoneNumber);
      fd.append("businessAddress", formData.businessAddress);
      fd.append("tradeLicense", formData.tradeLicenseFile);

      await registerRetailer(fd);


      toast.success(
        "Registration submitted successfully! Please wait 24 hours for admin approval."
      );

      navigate("/login");
    } catch (err) {
      toast.error(err.message || "Registration failed. Please try again.");
    } finally {
      setSubmitting(false);
    }
  };


  return (
    <div className='register-page'>
      <div className='register-img'>
        <img src='/retailerRegImg.jpg' alt="register retailer" />
      </div>

      <div className='register-context'>
        <h2>
          <Building2 className="inline-block mr-3" size={32} />
          Register as Retailer
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
                className={`form-input ${errors.phoneNumber ? 'error' : ''}`}
                placeholder="Enter 10-digit phone number"
                value={formData.phoneNumber}
                onChange={handleChange}
                disabled={submitting}
              />
            </div>
            {errors.phoneNumber && <p className="error-text">{errors.phoneNumber}</p>}
          </div>

          {/* Business Address */}
          <div className="form-group">
            <label className="form-label" htmlFor="businessAddress">
              Business Address <span className="required">*</span>
            </label>
            <div className="input-wrapper">
              <MapPin className="input-icon" size={18} style={{ left: "12px" }} />
              <input
                id="businessAddress"
                name="businessAddress"
                type="text"
                className={`form-input ${errors.businessAddress ? 'error' : ''}`}
                placeholder="Enter your business address"
                value={formData.businessAddress}
                onChange={handleChange}
                disabled={submitting}
              />
            </div>
            {errors.businessAddress && <p className="error-text">{errors.businessAddress}</p>}
          </div>

          {/* ✅ PDF File Upload */}
          <div className="form-group">
            <label className="form-label" htmlFor="tradeLicenseFile">
              Trade License Image <span className="required">*</span>
            </label>
            <div
              className={`file-upload-wrapper ${dragging ? 'drag-over' : ''} ${errors.tradeLicenseFile ? 'error' : ''}`}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
            >
              <div className="file-upload-area">
                <Upload className="file-upload-icon" size={32} style={{ left: "12px" }} />
                <input
                  id="tradeLicenseFile"
                  name="tradeLicenseFile"
                  type="file"
                  accept=" .png, .jpg, .jpeg"

                  className="file-input"
                  onChange={handleFileChange}
                  disabled={submitting}
                  required
                />
                <p className="file-upload-text">
                  {formData.tradeLicenseFileName
                    ? `✅ ${formData.tradeLicenseFileName}`
                    : 'Drag & drop file here or click to browse (Max 5MB)'
                  }
                </p>
                <p className="file-upload-hint">Only Image files are allowed</p>
              </div>
            </div>
            {errors.tradeLicenseFile && <p className="error-text">{errors.tradeLicenseFile}</p>}
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
                Register Retailer
              </>
            )}
          </button>
        </form>

        <div className="login-footer">
          <p className="login-text">
            Already have an account?{' '}
            <Link to="/login" className="register-link">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
