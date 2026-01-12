import { Routes, Route } from "react-router-dom";
import PublicLayout from "../components/layout/PublicLayout";
import MainLayout from "../components/layout/MainLayout";
import ProtectedRoute from "./ProtectedRoute";

import LandingPage from "../components/layout/LandingPage";
import LoginPage from "../features/auth/login";
import RetailerRegister from "../features/auth/RetailerRegister";
import FarmerRegister from "../features/auth/FarmerRegister";
import ForgotPassword from "../features/auth/ForgotPassword";
import SetPassword from "../features/auth/SetPassword";

import CropList from "../features/retailer/components/CropList";
import FarmerCropDetails from "../features/farmer/pages/CropDetails";
import RetailerCropDetails from "../features/retailer/components/InventoryCardDetails";
import Unauthorized from "../components/common/Unauthorized";
import OrderList from "../features/common/OrderList";
import OrderDetails from "../features/common/OrderDetails";

import RetailerDashboard from "../features/retailer/pages/RetailerDashboard";
import AdminDashboard from "../features/admin/AdminDashboard";
import FarmerDashboard from "../features/farmer/pages/FarmerDashboard";
import AddCrop from "../features/farmer/pages/AddCrop";
import Notification from "../features/common/Notification";
import AddAdmin from "../features/admin/AddAdmin";
import MyBidsPage from "../features/retailer/pages/MyBidPage";
import PendingUsersTable from "../features/admin/PendingUsersTable";
const AppRoutes = () => {
  return (
    <Routes>

      {/* Public Pages */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/registerRetailer" element={<RetailerRegister />} />
        <Route path="/registerFarmer" element={<FarmerRegister />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/set-password" element={<SetPassword />} />
      </Route>

      {/* Protected Main Panel (Retailer example) */}
      <Route element={<ProtectedRoute allowedRoles={["RETAILER"]} />}>
        <Route element={<MainLayout />}>
          <Route path="/retailer/dashboard" element={<RetailerDashboard />} />
          <Route path="/retailer/inventory" element={<CropList />} />
          <Route path="/retailer/crop-details/:cropId" element={<RetailerCropDetails />} />
          <Route path="/retailer/my-bids" element={<MyBidsPage />} />
          <Route path="/retailer/orders" element={<OrderList />} />
          <Route path="/retailer/order-details/:orderId" element={<OrderDetails />} />
          <Route path="/retailer/notifications" element={<Notification />} />
        </Route>
      </Route>


      {/* Protected Main Panel (Admin example) */}
      <Route element={<ProtectedRoute allowedRoles={["ADMIN"]} />}>
        <Route element={<MainLayout />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/AddAdmin" element={<AddAdmin />} />
          <Route path="/admin/pendingUsers" element={<PendingUsersTable />} />
        </Route>
      </Route>

      {/* Farmer Protected */}
      <Route element={<ProtectedRoute allowedRoles={["FARMER"]} />}>
        <Route element={<MainLayout />}>
        <Route path="/farmer/dashboard" element={<FarmerDashboard />} />
        <Route path="/farmer/addCrop" element={<AddCrop />} />
        <Route path="/farmer/crops" element={<CropList />} />
        <Route path="/farmer/crop-details/:cropId" element={<FarmerCropDetails />} />
        <Route path="/farmer/orders" element={<OrderList />} />
        <Route path="/farmer/order-details/:orderId" element={<OrderDetails />} />
        <Route path="/farmer/notifications" element={<Notification />} />
        </Route>
      </Route>
      <Route path="/unauthorized" element={<Unauthorized />} />
    </Routes>
  );
};

export default AppRoutes;
