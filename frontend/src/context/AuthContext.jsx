import React, { createContext, useState, useEffect, useContext, useRef } from 'react';
import api from '../utils/api';
import { toast } from 'react-toastify';

const AuthContext = createContext();

function parseJwt(token) {
  if (!token) return null;
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null); // {username, roles:[]}
  const [token, setToken] = useState(null);
const [loading, setLoading] = useState(true);
  const logoutTimerRef = useRef(null);

  const clearLogoutTimer = () => {
    if (logoutTimerRef.current) {
      clearTimeout(logoutTimerRef.current);
      logoutTimerRef.current = null;
    }
  };

  const setLogoutTimer = (token) => {
    clearLogoutTimer();
    const payload = parseJwt(token);
    if (payload && payload.exp) {
      const expiryTimeMs = payload.exp * 1000;
      const currentTimeMs = Date.now();
      const timeLeft = expiryTimeMs - currentTimeMs;

      if (timeLeft <= 0) {
        logout();
      } else {
        logoutTimerRef.current = setTimeout(() => {
          logout();
          alert('Session expired. You have been logged out.');
        }, timeLeft);
      }
    }
  };

  useEffect(() => {
  const savedToken = localStorage.getItem('token');

  if (savedToken) {
    const payload = parseJwt(savedToken);

    if (payload && payload.sub) {
      const expiryTimeMs = payload.exp ? payload.exp * 1000 : null;

      if (expiryTimeMs && expiryTimeMs > Date.now()) {
        setUser({
          username: payload.sub,
          role: payload.role,
        });
        setToken(savedToken);
        setLogoutTimer(savedToken);
      } else {
        localStorage.removeItem('token');
      }
    } else {
      localStorage.removeItem('token');
    }
  }
  
  setLoading(false); 

  return () => {
    clearLogoutTimer();
  };
}, []); // ✅ Add dependency array


  const login = async (credentials) => {
    try {
      const data = await api.post('/auth/login', credentials);
      if (!data.token) {
        throw new Error('Invalid login response: Token missing');
      }
      const parsed = parseJwt(data.token);
      if (!parsed || !parsed.sub) {
        throw new Error('Invalid JWT token received');
      }
      localStorage.setItem('token', data.token);
      setToken(data.token);
      setUser({
  username: parsed.sub,
  role: Array.isArray(parsed.role) ? parsed.role : [parsed.role], // ✅ Always convert to array
});
toast.success("Log in successfully")
      setLogoutTimer(data.token);
    } catch (err) {
      throw new Error(err.message || 'Login failed');
    }
  };

  const registerFarmer = async (credentials) => {
    try {
      const data=await api.post('/auth/register/farmer', credentials);
      return data;
    } catch (err) {
      throw new Error(err.message || 'Registration failed');
    }
  };

  const registerRetailer = async (credentials) => {
    try {
      const data=await api.post('/auth/register/retailer', credentials);
      return data;
    } catch (err) {
      throw new Error(err.message || 'Registration failed');
    }
  };

  const updateStatus=async(credentials)=>{
    try {
      const data=await api.post('/admin/pendingUsers', credentials);
      return data;
    } catch (err) {
      throw new Error(err.message || 'Registration failed');
    }
  }

  const deleteAccount = async () => {
    if (!window.confirm("Are you sure you want to delete your account? This cannot be undone.")) {
      return;
    }
    try {
      const response = await api.delete('/user/deleteUser', token);
      const altmsg=response.message || "Deleted successfully"
      
      toast.warn(altmsg);
      logout(); // clear auth
    } catch (err) {
      const errmsg=err.message || 'Delete failed';
      toast.error(errmsg);
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
    clearLogoutTimer();
    toast.success("logout successfully");
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, registerFarmer,registerRetailer,deleteAccount ,loading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  return useContext(AuthContext);
};

