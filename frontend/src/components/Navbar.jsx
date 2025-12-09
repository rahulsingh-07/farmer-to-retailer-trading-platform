import React, { useState,useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FcHome } from "react-icons/fc";
import { useAuth } from '../context/AuthContext';
import '../css/Navbar.css';

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');
    const [isMenuOpen, setIsMenuOpen] = useState(false);

    const handleSearch = (e) => {
        if (e.key === 'Enter' && searchQuery.trim() !== '') {
            navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
        }
    };

    const handleLogout = () => {
        logout();
        navigate('/');
        setIsMenuOpen(false);
    };


    return (
        <>
        <header className="navbar-header">
            <div className="navbar-container">
                {/* Logo */}
                <Link to="/" className="navbar-logo">
                    <FcHome className="logo-icon" />
                    <span className="logo-text">FarmFresh Connect</span>
                </Link>

                {/* Desktop Search & Actions */}
                <div className="navbar-desktop">
                    <div className="search-container">
                        <input
                            type="text"
                            placeholder="Search crops, retailers..."
                            className="search-input"
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            onKeyDown={handleSearch}
                        />
                        <div className="search-icon">🔍</div>
                    </div>

                    <div className="navbar-actions">
                        {user ? (
                            <>
                                <Link to="/dashboard" className="nav-link dashboard-link">
                                    Dashboard
                                </Link>
                                {user.role?.includes("ROLE_ADMIN") && (
                                    <Link to="/admin" className="nav-link admin-link">
                                        Admin
                                    </Link>
                                )}
                                <button onClick={handleLogout} className="nav-btn logout-btn">
                                    Logout
                                </button>
                            </>
                        ) : (
                            
                            <Link to="/login" className="nav-link dashboard-link">
                                Login
                            </Link>
                        )}
                    </div>
                </div>
            </div>   
                
        </header>
        <div className="navbar-spacer"></div>
        </>
    );
};

export default Navbar;
