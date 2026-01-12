import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import PropTypes from "prop-types";

const ProtectedRoute = ({ allowedRoles }) => {
  const { user, token, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!token || !user) {
    return <Navigate to="/login" replace />;
  }

  const userRole = user.role;
  const requiredRoles = (allowedRoles || []).map(r => r.toUpperCase());
 
  if (
    requiredRoles.length > 0 &&
    userRole &&
    !requiredRoles.includes(userRole)
  ) {
    return <Navigate to="/unauthorized" replace />;
  }

  // 5️⃣ Authorized
  return <Outlet />;
};

ProtectedRoute.propTypes = {
  allowedRoles: PropTypes.arrayOf(PropTypes.string),
};

export default ProtectedRoute;
