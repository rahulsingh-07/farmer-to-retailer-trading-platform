import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom'; // Add this import
import '../css/FarmerLanding.css';

const FarmerLandingPage = () => {
  const [showStats, setShowStats] = useState(false);
  const [scrollY, setScrollY] = useState(0);
  const [statsData, setStatsData] = useState([
  { value: 0, label: "Farmers", color: "#16a34a" },
  { value: 0, label: "Retailers", color: "#2563eb" },
]);


  
  useEffect(() => {
    const handleScroll = () => {
      setScrollY(window.scrollY);
      if (window.scrollY > 300 && !showStats) {
        setShowStats(true);
      }
    };
    window.addEventListener("scroll", handleScroll);

    return () => window.removeEventListener("scroll", handleScroll);
  }, [showStats]);

  const fetchStats = async () => {
    try {
      const response = await fetch("http://localhost:8081/auth/totalUsers");
      const data = await response.json();

      setStatsData([
        { value: data.totalFarmer, label: "Farmers", color: "#16a34a" },
        { value: data.totalRetailer, label: "Retailers", color: "#2563eb" },
      ]);
    } catch (error) {
      console.error("Error loading stats", error);
    }
  };

  useEffect(() => {
    if (showStats) {
      fetchStats();
    }
  }, [showStats]);

  return (
    <div className="landing-page">
      {/* Hero Section */}
      <section className="hero-section">
        <div className="hero-content">
          <div className="hero-badge">🚀 Launching Soon</div>
          <h1 className="hero-title">
            Connect <span className="highlight">Farmers</span> Directly to 
            <span className="highlight"> Retailers</span>
          </h1>
          <p className="hero-subtitle">
            FarmFresh Connect eliminates middlemen, ensuring farmers get 30% more profit 
            and retailers get freshest produce at wholesale prices.
          </p>
          <div className="hero-buttons">
            <Link to="/registerFarmer">
              <button className="cta-primary">Join as Farmer</button>
            </Link>
            <Link to="/registerRetailer">
              <button className="cta-secondary">Join as Retailer</button>
            </Link>
          </div>
        </div>
        <div className="hero-visual">
          <div className="floating-crops">
            <img src="/crops/fruits.png" 
                 alt="Tomato" className="crop tomato" />
            <img src="/crops/grains.png" 
                 alt="Carrot" className="crop carrot" />
            <img src="/crops/flour.png" 
                 alt="Apple" className="crop apple" />
            <img src="/crops/veg.png" 
                 alt="Chilli" className="crop chilli" />
          </div>
          <div className="hero-farmer-illustration"></div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="stats-section">
        <div className="stats-container">
          {statsData.map((stat, index) => (
            <div key={index} className={`stat-card ${showStats ? 'animate' : ''}`}>
              <div className="stat-number" style={{color: stat.color}}>
                {showStats ? stat.value : '0'}
              </div>
              <div className="stat-label">{stat.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Features Section */}
      <section className="features-section">
        <h2 className="section-title">Why Choose FarmFresh Connect?</h2>
        <div className="features-grid">
          <div className="feature-card">
            <div className="feature-icon">🌾</div>
            <h3>Direct Connection</h3>
            <p>No middlemen. Farmers connect directly with verified retailers.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">📱</div>
            <h3>Real-time Pricing</h3>
            <p>Live market rates and transparent bidding system.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">🚚</div>
            <h3>Logistics Partnered</h3>
            <p>Cold chain logistics ensures fresh delivery nationwide.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">💰</div>
            <h3>Instant Payments</h3>
            <p>Payments credited within 24 hours after delivery confirmation.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">📊</div>
            <h3>Smart Analytics</h3>
            <p>Demand forecasting and price trend analysis for farmers.</p>
          </div>
          <div className="feature-card">
            <div className="feature-icon">🛡️</div>
            <h3>Verified Partners</h3>
            <p>All retailers KYC verified with quality assurance.</p>
          </div>
        </div>
      </section>

      {/* Farmer How It Works */}
<section className="how-it-works-section">
  <h2 className="section-title">Farmers: How It Works</h2>
  <div className="steps-container">
    <div className="step">
      <div className="step-number">1</div>
      <h3>List Your Harvest</h3>
      <p>Register as a farmer and list your fresh crops directly from the farm.</p>
    </div>
    <div className="step">
      <div className="step-number">2</div>
      <h3>Retailers Connect</h3>
      <p>Local retailers discover and place orders at fair prices.</p>
    </div>
    <div className="step">
      <div className="step-number">3</div>
      <h3>Harvest & Deliver</h3>
      <p>Harvest fresh and deliver through trusted local partners.</p>
    </div>
    <div className="step">
      <div className="step-number">4</div>
      <h3>Get Paid Fast</h3>
      <p>Receive payments instantly after safe delivery.</p>
    </div>
  </div>
</section>

{/* Retailer How It Works */}
<section className="how-it-works-section">
  <h2 className="section-title">Retailers: How It Works</h2>
  <div className="steps-container">
    <div className="step">
      <div className="step-number">1</div>
      <h3>Browse Fresh Produce</h3>
      <p>Sign up and discover farm-fresh produce from verified farmers.</p>
    </div>
    <div className="step">
      <div className="step-number">2</div>
      <h3>Place Orders</h3>
      <p>Order exactly what you need at competitive wholesale prices.</p>
    </div>
    <div className="step">
      <div className="step-number">3</div>
      <h3>Receive Fresh</h3>
      <p>Get same-day fresh deliveries through our logistics network.</p>
    </div>
    <div className="step">
      <div className="step-number">4</div>
      <h3>Pay Securely</h3>
      <p>Pay only after quality confirmation - no upfront risk.</p>
    </div>
  </div>
</section>

{/* Dual CTA Section */}
<section className="cta-section">
  <div className="cta-content">
    <h2>Ready to Connect Farm to Market?</h2>
    <p>Join 15,000+ farmers & 5,000+ retailers on FarmFresh.</p>
    <div className="cta-buttons">
      <Link to="/registerFarmer">
        <button className="cta-primary-large">Start Selling</button>
      </Link>
      <Link to="/registerRetailer">
        <button className="cta-secondary-large">Start Buying</button>
      </Link>
    </div>
  </div>
</section>


      {/* Footer */}
      <footer className="footer">
        <div className="footer-content">
          <div className="footer-brand">
            <h3>FarmFresh Connect</h3>
            <p>Connecting farms to tables, transparently.</p>
          </div>
          <div className="footer-links">
            <a href="#">Privacy Policy</a>
            <a href="#">Terms of Service</a>
            <a href="#">Contact</a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default FarmerLandingPage;
