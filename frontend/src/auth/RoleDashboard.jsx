import React from "react";
import { useAuth } from ".././context/AuthContext";
import AdminDashboard from ".././components/dashboard/AdminDashboard";
import FarmerDashboard from ".././components/dashboard/FarmerDashboard";
import RetailerDashboard from ".././components/dashboard/RetailerDashboard";

export default function RoleDashboard() {
  const { user } = useAuth();

  if (!user) return null;

  const roles = user.role; // array

  if (roles.includes("ADMIN")) return <AdminDashboard />;
  if (roles.includes("FARMER")) return <FarmerDashboard />;
  if (roles.includes("RETAILER")) return <RetailerDashboard />;

  return <h2>Unauthorized Role</h2>;
}
