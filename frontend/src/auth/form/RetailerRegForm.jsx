import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from '../../context/AuthContext';
import '../../css/FarmerRegistration.css';

export default function retailerReForm() {
  const navigate = useNavigate();
  const {registerRetailer}=useAuth();
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    phoneNumber: "",
    shopName: "",
    businessAddress: "",
    gstNumber: "",
    tradeLicenseUrl: "",
  });

  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [message, setMessage] = useState("");

  const validateForm = () => {
    const newErrors = {};
    if (!formData.fullName.trim()) newErrors.fullName = "Name cannot be empty";
    if (!formData.email.trim()) newErrors.email = "Email is required";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) newErrors.email = "Enter valid email";
    if (!formData.phoneNumber.trim()) {
  newErrors.phoneNumber = "Phone number is required";
} 
else if (!/^[0-9]{10}$/.test(formData.phoneNumber)) {
  newErrors.phoneNumber = "Phone number must be 10 digits only";
}
    if (!formData.businessAddress.trim()) newErrors.businessAddress = "Address is required";
    if (!formData.gstNumber.trim()) newErrors.gstNumber = "GST Number is required";
    if (!formData.tradeLicenseUrl.trim()) newErrors.tradeLicenseUrl = "Document URL is required";
    
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
    setMessage("");

    try {
      // Use your API helper
      await registerRetailer(formData);
      setSubmitted(true);
      setMessage("✅ Registration submitted successfully! Please wait **24 hours** for admin approval. Your credentials will be sent to your registered email.");
      
      // Reset form and redirect after delay
      setTimeout(() => {
        navigate("/");
      }, 4000);
    } catch (err) {
      setMessage(`❌ ${err.message || 'Registration failed. Please try again.'}`);
    } finally {
      setSubmitting(false);
    }
  };

  if (submitted) {
    return (
      <div className="success-screen">
        <div className="success-card">
          <div className="success-icon">✅</div>
          <h2 className="success-title">Registration Successful!</h2>
          <p className="success-message">{message}</p>
          <div className="progress-bar">
            <div className="progress-fill"></div>
          </div>
          <p className="redirecting-text">Redirecting to home in <span id="countdown">4</span>s...</p>
        </div>
      </div>
    );
  }
  

  return (
    <div className="reg-page">
      <div className="floating-crops-reg">
        <img src="/crops/fruits.png" 
             alt="Tomato" className="crop-reg tomato-reg" />
        <img src="/crops/veg.png" 
             alt="Carrot" className="crop-reg carrot-reg" />
      </div>
      
      <div className="reg-container">
        <div className="reg-card">
          <div className="reg-header">
            <h1 className="reg-title">Retailer Registration</h1>
            <p className="reg-subtitle">Join FarmFresh Connect and start sourcing fresh produce directly from farmers</p>
          </div>

          {message && (
            <div className={`message ${message.includes('❌') ? 'error' : 'success'}`}>
              {message}
            </div>
          )}

          <form onSubmit={handleSubmit} className="reg-form">
            <div className="form-grid">
              <div className="form-group">
                <label className="form-label">
                  Full Name <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="fullName"
                  placeholder="Enter your full name"
                  value={formData.fullName}
                  onChange={handleChange}
                  className={`form-input ${errors.fullName ? 'error' : ''}`}
                />
                {errors.fullName && <p className="error-text">{errors.fullName}</p>}
              </div>

              <div className="form-group">
                <label className="form-label">
                  Email <span className="required">*</span>
                </label>
                <input
                  type="email"
                  name="email"
                  placeholder="your.email@example.com"
                  value={formData.email}
                  onChange={handleChange}
                  className={`form-input ${errors.email ? 'error' : ''}`}
                />
                {errors.email && <p className="error-text">{errors.email}</p>}
              </div>

              <div className="form-group">
                <label className="form-label">
                  Phone Number <span className="required">*</span>
                </label>
                <input
                  type="tel"
                  maxLength="10"
                  name="phoneNumber"
                  placeholder="+91 98765 43210"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                  className={`form-input ${errors.phoneNumber ? 'error' : ''}`}
                />
                {errors.phoneNumber && <p className="error-text">{errors.phoneNumber}</p>}
              </div>

              <div className="form-group">
                <label className="form-label">
                  Shop Name <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="shopName"
                  placeholder="Enter your full name"
                  value={formData.shopName}
                  onChange={handleChange}
                  className={`form-input ${errors.shopName ? 'error' : ''}`}
                />
                {errors.shopName && <p className="error-text">{errors.shopName}</p>}
              </div>

              <div className="form-group full-width">
                <label className="form-label">
                  Business Address <span className="required">*</span>
                </label>
                <textarea
                  name="businessAddress"
                  placeholder="Enter your complete businessAddress"
                  rows="3"
                  value={formData.businessAddress}
                  onChange={handleChange}
                  className={`form-textarea ${errors.businessAddress ? 'error' : ''}`}
                />
                {errors.businessAddress && <p className="error-text">{errors.businessAddress}</p>}
              </div>

              <div className="form-group">
                <label className="form-label">
                  GST Number <span className="required">*</span>
                </label>
                <input
                  type="text"
                  name="gstNumber"
                  placeholder="Enter your full name"
                  value={formData.gstNumber}
                  onChange={handleChange}
                  className={`form-input ${errors.gstNumber ? 'error' : ''}`}
                />
                {errors.gstNumber && <p className="error-text">{errors.gstNumber}</p>}
              </div>

              <div className="form-group full-width">
                <label className="form-label">
                  Trade License URL <span className="required">*</span>
                </label>
                <input
                  type="url"
                  name="tradeLicenseUrl"
                  placeholder="https://example.com/your-document.pdf"
                  value={formData.tradeLicenseUrl}
                  onChange={handleChange}
                  className={`form-input ${errors.tradeLicenseUrl ? 'error' : ''}`}
                />
                {errors.tradeLicenseUrl && <p className="error-text">{errors.tradeLicenseUrl}</p>}
              </div>
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="submit-btn"
            >
              {submitting ? (
                <>
                  <span className="spinner"></span>
                  Submitting...
                </>
              ) : (
                "Register as Retailer"
              )}
            </button>
          </form>

          <div className="reg-footer">
            <p>Already registered? <a href="/login">Login here</a></p>
          </div>
        </div>
      </div>
    </div>
  );
}
