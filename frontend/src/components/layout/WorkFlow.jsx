// components/Workflow.jsx
import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import contactBg from '../../assets/contact.jpg';
import './Workflow.css';

const Workflow = () => {
  const [scrollY, setScrollY] = useState(0);

  useEffect(() => {
    const handleScroll = () => setScrollY(window.scrollY);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const wrapperStyle = {
    backgroundImage: `linear-gradient(180deg, rgba(10, 23, 15, 0.72), rgba(10, 23, 15, 0.68)), radial-gradient(circle at 18% 22%, rgba(99, 163, 117, 0.4), transparent 32%), url(${contactBg})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat',
  };

  return (
    <div className="workflow-wrapper" style={wrapperStyle}>
      {/* Farmer Section */}
      <FarmerWorkflow scrollY={scrollY} />
      
      {/* Retailer Section */}
      <RetailerWorkflow scrollY={scrollY} />
    </div>
  );
};

// Farmer Workflow Section
const FarmerWorkflow = ({ scrollY }) => {
  const farmerSteps = [
    { title: 'Profile & KYC', icon: '👤', desc: 'Complete Aadhaar/PAN/Bank verification', color: '#10b981' },
    { title: 'Add Crop', icon: '🌾', desc: 'List with base price & bid option', color: '#f59e0b' },
    { title: 'My Crops', icon: '📋', desc: 'Manage active, sold & expired listings', color: '#3b82f6' },
    { title: 'Bids Received', icon: '💰', desc: 'View & accept best retailer offers', color: '#ef4444' },
    { title: 'Orders', icon: '🚚', desc: 'Track confirmed orders & delivery', color: '#8b5cf6' },
    { title: 'Payments', icon: '💳', desc: 'Wallet balance & instant settlements', color: '#06b6d4' },
    { title: 'Analytics', icon: '📊', desc: 'Sales trends & revenue insights', color: '#84cc16' }
  ];

  return (
    <section className="workflow-section farmer-section">
      <div className="container">
        <motion.div 
          className="section-header"
          initial={{ opacity: 0, y: 50 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.8 }}
        >
          <h2 className="section-title">👨‍🌾 Farmer Success Journey</h2>
          <p className="section-subtitle">
            From farm to market in 5 simple steps with maximum profits
          </p>
        </motion.div>

        <div className="workflow-grid">
          {/* Vertical Timeline */}
          <div className="timeline">
            {farmerSteps.map((step, index) => (
              <motion.div
                key={step.title}
                className="timeline-item"
                initial={{ opacity: 0, x: -50 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ delay: index * 0.2 }}
              >
                <div className="timeline-dot" style={{ background: step.color }} />
                <div className="timeline-line" />
              </motion.div>
            ))}
          </div>

          {/* Steps Content */}
          <div className="steps-container">
            {farmerSteps.map((step, index) => (
              <motion.div
                key={step.title}
                className="step-card"
                style={{ '--step-color': step.color }}
                initial={{ opacity: 0, scale: 0.8 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                whileHover={{ y: -10, scale: 1.02 }}
                transition={{ delay: index * 0.1 }}
              >
                <div className="step-icon" style={{ background: step.color }}>
                  {step.icon}
                </div>
                <div className="step-content">
                  <h3>{step.title}</h3>
                  <p>{step.desc}</p>
                </div>
                <div className="step-number">{index + 1}</div>
              </motion.div>
            ))}
          </div>
        </div>

        <motion.div 
          className="cta-section"
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          transition={{ delay: 1 }}
        >
          <Link to="/registerFarmer">
            <motion.button className="cta-primary-large" whileHover={{ scale: 1.05 }}>
              🚜 Start Selling Your Harvest
            </motion.button>
          </Link>
        </motion.div>
      </div>
    </section>
  );
};

// Retailer Workflow Section
const RetailerWorkflow = ({ scrollY }) => {
  const retailerSteps = [
    { title: 'Business KYC', icon: '🏪', desc: 'Verify shop with GST & address', color: '#ef4444' },
    { title: 'Browse Crops', icon: '🔍', desc: 'Filter by crop, location, price', color: '#f59e0b' },
    { title: 'Place Bid', icon: '⚡', desc: 'Compete for best deals with timer', color: '#10b981' },
    { title: 'Instant Buy', icon: '🛒', desc: 'Skip bidding for faster checkout', color: '#3b82f6' },
    { title: 'My Orders', icon: '📦', desc: 'Track delivery & order history', color: '#8b5cf6' },
    { title: 'Payments', icon: '💸', desc: 'UPI, wallet & invoice downloads', color: '#06b6d4' }
  ];

  return (
    <section className="workflow-section retailer-section">
      <div className="container">
        <motion.div 
          className="section-header"
          initial={{ opacity: 0, y: 50 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.8 }}
        >
          <h2 className="section-title">🏪 Retailer Success Journey</h2>
          <p className="section-subtitle">
            Fresh produce at competitive prices, delivered reliably
          </p>
        </motion.div>

        <div className="workflow-grid">
          {/* Vertical Timeline */}
          <div className="timeline">
            {retailerSteps.map((step, index) => (
              <motion.div
                key={step.title}
                className="timeline-item"
                initial={{ opacity: 0, x: -50 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ delay: index * 0.2 }}
              >
                <div className="timeline-dot" style={{ background: retailerSteps[index]?.color }} />
                <div className="timeline-line" />
              </motion.div>
            ))}
          </div>

          {/* Steps Content */}
          <div className="steps-container">
            {retailerSteps.map((step, index) => (
              <motion.div
                key={step.title}
                className="step-card"
                style={{ '--step-color': step.color }}
                initial={{ opacity: 0, scale: 0.8 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                whileHover={{ y: -10, scale: 1.02 }}
                transition={{ delay: index * 0.1 }}
              >
                <div className="step-icon" style={{ background: step.color }}>
                  {step.icon}
                </div>
                <div className="step-content">
                  <h3>{step.title}</h3>
                  <p>{step.desc}</p>
                </div>
                <div className="step-number">{index + 1}</div>
              </motion.div>
            ))}
          </div>
        </div>

        <motion.div 
          className="cta-section"
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          transition={{ delay: 1 }}
        >
          <Link to="/registerRetailer">
            <motion.button className="cta-secondary-large" whileHover={{ scale: 1.05 }}>
              🛒 Start Sourcing Fresh Produce
            </motion.button>
          </Link>
        </motion.div>
      </div>
    </section>
  );
};

export default Workflow;
