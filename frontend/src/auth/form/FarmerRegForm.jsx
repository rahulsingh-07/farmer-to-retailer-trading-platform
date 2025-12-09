import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from '../../context/AuthContext';
import '../../css/FarmerRegistration.css'; // New CSS file

export default function FarmerRegForm() {
  const navigate = useNavigate();
  const {registerFarmer}=useAuth();
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    phoneNumber: "",
    address: "",
    documentUrl: "",
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
    if (!formData.address.trim()) newErrors.address = "Address is required";
    if (!formData.documentUrl.trim()) newErrors.documentUrl = "Document URL is required";
    
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
      await registerFarmer(formData);
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
        <img src="/crops/grains.png" 
             alt="Carrot" className="crop-reg carrot-reg" />
      </div>
      
      <div className="reg-container">
        <div className="reg-card">
          <div className="reg-header">
            <h1 className="reg-title">Farmer Registration</h1>
            <p className="reg-subtitle">Join FarmFresh Connect and start selling directly to retailers</p>
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

              <div className="form-group full-width">
                <label className="form-label">
                  Address <span className="required">*</span>
                </label>
                <textarea
                  name="address"
                  placeholder="Enter your complete address"
                  rows="3"
                  value={formData.address}
                  onChange={handleChange}
                  className={`form-textarea ${errors.address ? 'error' : ''}`}
                />
                {errors.address && <p className="error-text">{errors.address}</p>}
              </div>

              <div className="form-group full-width">
                <label className="form-label">
                  Document URL <span className="required">*</span>
                </label>
                <input
                  type="url"
                  name="documentUrl"
                  placeholder="https://example.com/your-document.pdf"
                  value={formData.documentUrl}
                  onChange={handleChange}
                  className={`form-input ${errors.documentUrl ? 'error' : ''}`}
                />
                {errors.documentUrl && <p className="error-text">{errors.documentUrl}</p>}
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
                "Register as Farmer"
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
