// Sidebar.jsx - FIXED: Role-based logic + syntax errors
import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  FaUsers,
  FaTruck,
  FaTag,
  FaChartBar,
  FaFileAlt,
  FaCog,
  FaBox,
  FaPlusSquare,
  FaLayerGroup,
  FaBell,
  FaHome,
} from 'react-icons/fa';
import { MdSpaceDashboard } from "react-icons/md";
import { useAuth } from '../context/AuthContext';
import '../css/Sidebar.css';

export const adminLinks = [
  { id: 'dashboad', icon: <MdSpaceDashboard />, label: 'Dashboard', path: '/dashboard' },
  { id: 'users', icon: <FaUsers />, label: 'Add Admin', path: '/admin/addAdmin' },
  { id: 'orders', icon: <FaTruck />, label: 'Orders', path: '/admin/orders' },
  { id: 'settings', icon: <FaCog />, label: 'Settings', path: '/newAdmin' },
];

export const farmerLinks = [
  { id: 'dashboad', icon: <MdSpaceDashboard />, label: 'Dashboard', path: '/dashboard' },
  { id: 'crops', icon: <FaLayerGroup />, label: 'My Crops', path: '/farmer/myCrops' },
  { id: 'add-crop', icon: <FaPlusSquare />, label: 'Add Crop', path: '/farmer/addCrops' },
  { id: 'orders', icon: <FaTruck />, label: 'Orders', path: '/farmer/orders' },,
  { id: 'notifications', icon: <FaBell />, label: 'Notifications', path: '/farmer/notifications' },
  { id: 'settings', icon: <FaCog />, label: 'Settings', path: '/farmer/settings' },
];

export const retailerLinks = [
  { id: 'dashboad', icon: <MdSpaceDashboard />, label: 'Dashboard', path: '/dashboard' },
  { id: 'inventory', icon: <FaBox />, label: 'Inventory', path: '/cropMarketplace' },
  { id: 'orders', icon: <FaTruck />, label: 'Orders', path: '/retailer/orders' },
  { id: 'notifications', icon: <FaBell />, label: 'Notifications', path: '/retailer/notifications' },
  { id: 'settings', icon: <FaCog />, label: 'Settings', path: '/retailer/settings' },
];

const Sidebar = () => {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [isDesktop, setIsDesktop] = useState(false);

  const isActive = (path) => location.pathname === path;

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
          <div className="logo-icon"><FaHome /></div>
          <span className="logo-text">
            <Link to="/">Home</Link>
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
            <button className="logout-btn" onClick={handleLogout}>
              Logout
            </button>
          ) : (
            <div className="user-info">
              <span className="user-name">Not logged in</span>
            </div>
          )}
        </div>
      </aside>

      {isOpen && !isDesktop && (
        <button
          type="button"
          className="sidebar-overlay"
          onClick={() => setIsOpen(false)}
          aria-label="Close sidebar"
        />
      )}
    </>
  );
};

export default Sidebar;
