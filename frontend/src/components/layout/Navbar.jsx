import React from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import './Navbar.css'
import { useAuth } from '../../context/AuthContext.jsx';
const links = [
  { label: 'Home', id: 'hero' },
  { label: 'Statics', id: 'statics' },
  { label: 'Contact', id: 'contact' },
  { label: 'Workflow', id: 'workflow' },
]

export default function Navbar() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user } = useAuth(); 

  const handleClick = (sectionId) => {
    // agar already home page pe ho
    if (location.pathname === '/') {
      const el = document.getElementById(sectionId)
      el?.scrollIntoView({ behavior: 'smooth' })
    } 
    // agar login ya koi aur page pe ho
    else {
      navigate(`/#${sectionId}`)
    }
  }
   const getDashboardPath = () => {
    if (!user) return '/login';
    const role = user.role;
    switch (role) {
      case 'ADMIN':
        return '/admin/dashboard';
      case 'FARMER':
        return '/farmer/dashboard';
      case 'RETAILER':
        return '/retailer/dashboard';
      default:
        return '/unauthorized';
    }
  };
  return (
    <nav className="navbar">
      <div className="navbar__logo" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <img src="/logo2.svg" alt="Logo" style={{ height: '32px', width: '32px', }} />
        <span>Logo</span>
      </div>
      <ul className="navbar__links">
        {links.map(link => (
          <li key={link.id}>
            <button style={{ backgroundColor: "transparent", border: "none", cursor: "pointer" }}
              className="navbar__link"
              onClick={() => handleClick(link.id)}
            >
              {link.label}
            </button>
          </li>
        ))}

         {/* Dashboard link (only if logged in) */}
        {user && (
          <li>
            <button style={{ backgroundColor: "transparent", border: "none", cursor: "pointer" }}
              className="navbar__link"
              onClick={() => navigate(getDashboardPath())}
            >
              Dashboard
            </button>
          </li>
        )}
      </ul>
    </nav>
  )
}
