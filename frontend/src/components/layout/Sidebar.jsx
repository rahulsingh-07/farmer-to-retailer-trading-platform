import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext.jsx';
import './Sidebar.css';
import {
  LayoutDashboard, ShoppingCart, Users, Package,
  Wheat, CopyPlus, Wallet, History, UserPlus,
  ChevronLeft, ChevronRight, LogOut, Bell
} from 'lucide-react';
import Swal from "sweetalert2";

const demoUser = { name: 'Demo User', role: 'FARMER' };

export default function Sidebar({ isOpen = true, onClose = () => { } }) {
  const { user, logout } = useAuth();
  const location = useLocation();
  const [isDesktop, setIsDesktop] = useState(window.innerWidth > 1024);
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    const handleResize = () => setIsDesktop(window.innerWidth > 1024);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const getRoleLinks = () => {
    if (!user) return [];

    const role = user.role;
    const roleKey = role?.toLowerCase() || 'user';

    const commonLinks = [
      { icon: LayoutDashboard, label: 'Dashboard', path: `/${roleKey}/dashboard` },
    ];

    const roleLinks = {
      ADMIN: [
        ...commonLinks,
        { icon: UserPlus, label: 'Add Admin', path: '/admin/AddAdmin' },
        { icon: Users, label: 'Pending Users', path: '/admin/pendingUsers' },
      ],
      FARMER: [
        ...commonLinks,
        { icon: Wheat, label: 'My Crops', path: '/farmer/crops' },
        { icon: CopyPlus, label: 'Add Crop', path: '/farmer/addCrop' },
        { icon: Package, label: 'Orders', path: '/farmer/orders' },
        { icon: Bell, label: 'Notifications', path: '/farmer/notifications' },
      ],
      RETAILER: [
        ...commonLinks,
        { icon: ShoppingCart, label: 'Browse Crops', path: '/retailer/inventory' },
        { icon: Package, label: 'My Orders', path: '/retailer/orders' },
        { icon: Bell, label: 'Notifications', path: '/retailer/notifications' },
        { icon: CopyPlus, label: 'My Bids', path: '/retailer/my-bids' },
      ],
    };

    return roleLinks[user.role] || [];
  };

  const links = getRoleLinks();
  const isActive = (path) => location.pathname === path;
  const handleLinkClick = () => !isDesktop && onClose();

  const handleLogout = async () => {
    const result = await Swal.fire({
      title: "Logout?",
      text: "Are you sure you want to logout?",
      icon: "warning",
      showCancelButton: true,
      confirmButtonText: "Yes, logout",
      cancelButtonText: "Cancel",
      confirmButtonColor: "#ef4444",
      cancelButtonColor: "#64748b",
    });

    if (result.isConfirmed) {
      logout();
      onClose();
    }
  };


  return (
    <>
      {isOpen && !isDesktop && <div className="sidebar-overlay" onClick={onClose} />}

      <aside className={`sidebar ${isOpen ? 'sidebar-open' : ''} ${collapsed ? 'sidebar-collapsed' : ''}`}>
        <div className="sidebar-header">
          <div className="logo">
            <span className="logo-icon"><Link to="/">🌾</Link></span>
            {!collapsed && <span className="logo-text">AgriTrade</span>}
          </div>
          {isDesktop && (
            <button className="sidebar-toggle" onClick={() => setCollapsed(!collapsed)}>
              {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
            </button>
          )}
        </div>

        <nav className="sidebar-nav">
          <ul className="nav-list">
            {links.map(({ icon: Icon, label, path }) => (
              <li key={path}>
                <Link to={path} className={`nav-link ${isActive(path) ? 'active' : ''}`} onClick={handleLinkClick}>
                  <Icon className="nav-icon" size={20} />
                  <span className="nav-label">{label}</span>
                  {collapsed && <span className="nav-tooltip">{label}</span>}
                </Link>
              </li>
            ))}
          </ul>
        </nav>

        {/* Footer - Works perfectly in both modes */}
        {!collapsed && (
          <div className="sidebar-footer">
            <div className="user-info" >
              <div className="user-avatar">{user.username?.[0]?.toUpperCase() || 'U'}</div>
              <div className="user-details">
                <span className="user-name">{user.username || 'User'}</span>
                <span className="user-role">{user.role}</span>
              </div>
            </div>
            <button className="logout-btn" onClick={handleLogout}>
              <LogOut size={16} /> Logout
            </button>

          </div>
        )}

        {/* Collapsed mode footer - Icons only */}
        {collapsed && user && (
          <div className="sidebar-footer">
            <div className="user-info" >
              <div className="user-avatar">{user.username?.[0]?.toUpperCase() || 'U'}</div>
            </div>
            <button className="logout-btn" onClick={handleLogout}>
              <LogOut size={16} />
            </button>

          </div>
        )}

      </aside>
    </>
  );
}
