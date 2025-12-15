// Sidebar.jsx - FIXED: Role-based logic + syntax errors
import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import '../css/Sidebar.css';

export const adminLinks = [
  { id: 'home', icon: '🏠', label: 'Home', path: '/' },
  { id: 'users', icon: '👥', label: 'Users', path: '/admin/users' },
  { id: 'orders', icon: '🚛', label: 'Orders', path: '/admin/orders' },
  { id: 'pricing', icon: '💰', label: 'Price Caps & Bids', path: '/admin/pricing' },
  { id: 'analytics', icon: '📈', label: 'Analytics', path: '/admin/analytics' },
  { id: 'reports', icon: '📋', label: 'Sales Reports', path: '/admin/reports' },
  { id: 'settings', icon: '⚙️', label: 'Settings', path: '/newAdmin' },
  { id: 'notifications', icon: '🔔', label: 'Notifications', path: '/admin/notifications' },
];

export const farmerLinks = [
  { id: 'home', icon: '🏠', label: 'Home', path: '/' },
  { id: 'crops', icon: '🌾', label: 'My Crops', path: '/farmer/myCrops' },
  { id: 'add-crop', icon: '➕', label: 'Add Crop', path: '/farmer/addCrops' },
  { id: 'orders', icon: '🚛', label: 'Orders', path: '/farmer/orders' },
  { id: 'pricing', icon: '💰', label: 'Pricing', path: '/farmer/pricing' },
  { id: 'analytics', icon: '📈', label: 'Analytics', path: '/farmer/analytics' },
  { id: 'notifications', icon: '🔔', label: 'Notifications', path: '/farmer/notifications' },
  { id: 'marketplace', icon: '📦', label: 'Marketplace', path: '/cropMarketplace' },
  { id: 'settings', icon: '⚙️', label: 'Settings', path: '/farmer/settings' },
];

export const retailerLinks = [
  { id: 'home', icon: '🏠', label: 'Home', path: '/' },
  { id: 'inventory', icon: '📦', label: 'Inventory', path: '/cropMarketplace' },
  { id: 'orders', icon: '🚛', label: 'Orders', path: '/retailer/orders' },
  { id: 'pricing', icon: '💰', label: 'Pricing', path: '/retailer/pricing' },
  { id: 'notifications', icon: '🔔', label: 'Notifications', path: '/retailer/notifications' },
  { id: 'cart', icon: '🛒', label: 'Cart', path: '/retailer/cart' },
  { id: 'settings', icon: '⚙️', label: 'Settings', path: '/retailer/settings' },
];

const Sidebar = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [isDesktop, setIsDesktop] = useState(false);

  const isActive = (path) => location.pathname === path;

  // ✅ FIXED: Proper getRoleLinks FUNCTION (was array syntax error!)
  const getRoleLinks = () => {
    if (user?.role?.includes('ADMIN')) {
      return adminLinks;
    } else if (user?.role?.includes('FARMER')) {
      return farmerLinks;
    } else if (user?.role?.includes('RETAILER')) {
      return retailerLinks;
    }
    return farmerLinks; // Default fallback
  };

  // ✅ FIXED: Call the FUNCTION, don't assign it
  const links = getRoleLinks();

  useEffect(() => {
    const checkScreenSize = () => {
      setIsDesktop(window.innerWidth > 768);
      if (window.innerWidth > 768) setIsOpen(true);
    };
    checkScreenSize();
    window.addEventListener('resize', checkScreenSize);
    return () => window.removeEventListener('resize', checkScreenSize);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/');
    setIsOpen(false);
  };

  return (
    <>
      {!isDesktop && (
        <button className="sidebar-toggle" onClick={() => setIsOpen(!isOpen)}>
          <span></span><span></span><span></span>
        </button>
      )}

      <aside className={`sidebar ${isOpen ? 'open' : ''}`}>
        <div className="sidebar-logo">
          <div className="logo-icon">📊</div>
          <span className="logo-text">
            <Link to="/dashboard">FarmFresh</Link>
          </span>
        </div>

        <ul className="sidebar-menu">
          {links.map((item) => (
            <li key={item.id}>
              <Link
                to={item.path}
                className={`menu-item ${isActive(item.path) ? 'active' : ''}`}
                onClick={() => !isDesktop && setIsOpen(false)}
              >
                <span className="menu-icon">{item.icon}</span>
                <span className="menu-label">{item.label}</span>
              </Link>
            </li>
          ))}
        </ul>

        <div className="sidebar-user">
          {user ? (
            <>
              <button className="logout-btn" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <div className="user-info">
              <span className="user-name">Not logged in</span>
            </div>
          )}
        </div>
      </aside>

      {isOpen && !isDesktop && (
        <div className="sidebar-overlay" onClick={() => setIsOpen(false)} />
      )}
    </>
  );
};

export default Sidebar;
