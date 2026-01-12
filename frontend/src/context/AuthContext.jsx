import { createContext, useContext, useEffect, useState, useMemo, useRef } from "react";
import PropTypes from "prop-types";
import jwtDecode from "jwt-decode";
import { toast } from "react-toastify";
import { login as loginApi } from "../services/authService";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const [loading, setLoading] = useState(true);
    const logoutTimerRef = useRef(null);

    const clearLogoutTimer = () => {
        if (logoutTimerRef.current) {
            clearTimeout(logoutTimerRef.current);
            logoutTimerRef.current = null;
        }
    };

    const scheduleLogout = (jwt) => {
        clearLogoutTimer();
        const { exp } = jwtDecode(jwt);
        if (!exp) return;

        const timeout = exp * 1000 - Date.now();
        if (timeout <= 0) {
            logout();
        } else {
            logoutTimerRef.current = setTimeout(logout, timeout);
        }
    };

    useEffect(() => {
        const storedToken = localStorage.getItem("token");
        if (!storedToken) {
            setLoading(false);
            return;
        }

        try {
            const decoded = jwtDecode(storedToken);
            if (decoded.exp * 1000 > Date.now()) {
                setToken(storedToken);
                setUser({
                    username: decoded.sub,
                    role: decoded.role,
                });
                scheduleLogout(storedToken);
            } else {
                localStorage.removeItem("token");
            }
        } catch {
            localStorage.removeItem("token");
        } finally {
            setLoading(false);
        }

        return clearLogoutTimer;
    }, []);

    const login = async (credentials) => {
        const response = await loginApi(credentials.username, credentials.password);
        console.log("Login API response:", response); // Debug log
        const jwt = response?.data?.token?.trim();
        if (!jwt) {
            throw new Error("No token returned from server");
        }
        
        localStorage.setItem("token", jwt);

        const decoded = jwtDecode(jwt);
        const user = {
            username: decoded.sub,
            role:decoded.role,
        };

        setToken(jwt);
        setUser(user);

        scheduleLogout(jwt);
        toast.success(response.message || "Login successful");

        return user;
    };


    const logout = () => {
        localStorage.removeItem("token");
        setToken(null);
        setUser(null);
        clearLogoutTimer();
    };

    const value = useMemo(
        () => ({ user, token, loading, login, logout }),
        [user, token, loading]
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

AuthProvider.propTypes = {
    children: PropTypes.node.isRequired,
};

export const useAuth = () => {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
};
