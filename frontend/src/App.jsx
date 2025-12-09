import React from 'react'
import "./index.css"
import Footer from './components/Footer'
import Navbar from './components/Navbar'
import Search from './components/Search'
import { Route, Routes } from 'react-router-dom'
import RoleDashboard from './auth/RoleDashboard'
import FarmerRegForm from './auth/form/FarmerRegForm'
import Login from './auth/Login'
import SetPassword from './components/SetPassword';
import "./App.css"
import FarmerLandingPage from './pages/LandingPage'
import ProductDetails from './pages/ProjuctDetails'
import PrivateRoute from './components/PrivateRoute';
import { ToastContainer } from 'react-toastify'
import 'react-toastify/dist/ReactToastify.css';
import ReviewPage from './components/ReviewPage'
import RetailerRegForm from './auth/form/retailerRegForm'
import Sidebar from './components/Sidebar'
import AddAdmin from './pages/admin/AddAdmin'
const App = () => {

  return (
    <div>


      <Routes>
        <Route path="/" element={ <> <Navbar /> <FarmerLandingPage /> </> } />


        <Route path="/search" element={<Search />} />
        <Route path="/login" element={ <> <Navbar /> <Login /> </> } />
        <Route path="/registerFarmer" element={<> <Navbar /> <FarmerRegForm /> </>} />
        <Route path="/registerRetailer" element={<> <Navbar /> <RetailerRegForm /> </>} />
        <Route path='/allreviews' element={<ReviewPage />} />

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

        <Route path="/productdetails/:id" element={<ProductDetails />} />
      </Routes>
      <ToastContainer theme="colored" position="top-right" autoClose={3000} />
      {/* <Footer /> */}
    </div>
  )
}

export default App
