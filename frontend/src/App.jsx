import React from 'react'
import "./index.css"
import Navbar from './components/Navbar'
import { Route, Routes } from 'react-router-dom'
import RoleDashboard from './auth/RoleDashboard'
import FarmerRegForm from './auth/form/FarmerRegForm'
import Login from './auth/Login'
import SetPassword from './components/SetPassword';
import "./App.css"
import FarmerLandingPage from './pages/LandingPage'
import PrivateRoute from './components/PrivateRoute';
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css';
import RetailerRegForm from './auth/form/RetailerRegForm'
import Sidebar from './components/Sidebar'
import AddAdmin from './pages/admin/AddAdmin'
import AddCrop from './pages/farmer/AddCrop'
import MyCrop from './pages/farmer/MyCrop'
import ForgotPassword from './auth/ForgotPassword'
import CropMarketplace from './pages/retailer/CropMarketplace'
import CropDetailPage from './pages/retailer/CropDetailPage'
import Notifications from './pages/farmer/Notifications'
import NotificationDetails from './pages/farmer/NotificationDetails'
import NotificationsRetailer from './pages/retailer/Notifications'
import NotificationDetailsRetailer from './pages/retailer/NotificationDetails'
import MyCropDetails from './pages/farmer/MyCropDetails'
import FarmerOrders from './pages/farmer/Orders'
import RetailerOrders from './pages/retailer/Orders'
import OrderDetailsFarmer from './pages/farmer/OrderDetails'
import OrderDetailsRetailer from './pages/retailer/OrderDetails'
const App = () => {

  return (
    <div>


      <Routes>
        <Route path="/" element={ <> <Navbar /> <FarmerLandingPage /> </> } />
        <Route path="/login" element={ <> <Navbar /> <Login /> </> } />
        <Route path="/forgot-password" element={ <> <Navbar /> <ForgotPassword /> </> } />
        <Route path="/registerFarmer" element={<> <Navbar /> <FarmerRegForm /> </>} />
        <Route path="/registerRetailer" element={<> <Navbar /> <RetailerRegForm /> </>} />
        <Route path="/set-password" element={<SetPassword />} />
        <Route
          path="/dashboard"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <RoleDashboard  />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/farmer/addCrops"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <AddCrop />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/farmer/myCrops"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <MyCrop />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/farmer/orders"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <FarmerOrders />
              </main>
            </PrivateRoute>
          }
        />
          <Route
            path="/farmer/notifications"
            element={
              <PrivateRoute>
                <Sidebar />
                <main className="app-content">
                  <Notifications />
                </main>
              </PrivateRoute>
            }
          />
          <Route
            path="/farmer/notifications/:id"
            element={
              <PrivateRoute>
                <Sidebar />
                <main className="app-content">
                  <NotificationDetails />
                </main>
              </PrivateRoute>
            }
          />
        <Route
          path="/newAdmin"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <AddAdmin  />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/cropMarketplace"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <CropMarketplace />
              </main>
            </PrivateRoute>
          }
        />

        <Route
          path="/crops/:id"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <CropDetailPage />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/retailer/notifications"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <NotificationsRetailer />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/retailer/orders"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <RetailerOrders />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/retailer/notifications/:id"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <NotificationDetailsRetailer />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/admin/addAdmin"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <AddAdmin />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/farmer/crops/:id"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <MyCropDetails />
              </main>
            </PrivateRoute>
          }
        />
         <Route
          path="/farmer/order/:id"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <OrderDetailsFarmer />
              </main>
            </PrivateRoute>
          }
        />
        <Route
          path="/retailer/order/:id"
          element={
            <PrivateRoute>
              <Sidebar />
              <main className="app-content">
                <OrderDetailsRetailer />
              </main>
            </PrivateRoute>
          }
        />
        
      </Routes>
      <ToastContainer theme="colored" position="top-right" autoClose={3000} />
      {/* <Footer /> */}
    </div>
  )
}

export default App
