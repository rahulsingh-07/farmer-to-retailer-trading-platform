import {privateApi} from "../../services/apiClient";
const BASE_URL = 'http://localhost:8081';
import { toast } from "react-toastify";


export const getFarmerStats = async () => {
  const res = await privateApi.get("/farmer/totalCrops");
  console.log("Farmer stats response:", res);
  return res.data; // ApiResponse → data
};
// Crop operations  
export const addCrop = (formData) => {
    return privateApi.upload('/farmer/addCrop', formData);
  }


export const getMyCrops = async (filters = {}) => {
  const cleanedFilters = Object.fromEntries(
    Object.entries(filters).filter(
    ([_, v]) => v && v.toString().trim() !== ''
  )
  );
  const queryString = new URLSearchParams(cleanedFilters).toString();
  const response = await privateApi.get(`/farmer/crops?${queryString}`);
  return response.data ?? {}; 
};

export const getCropById = (cropId) => {
  return privateApi.get(`/farmer/crop/${cropId}`);
}

export const updateCrop = (cropId, updateData) => {
  return privateApi.patch(`/farmer/crop/${cropId}/update`, updateData);
};












// Order operations
export const getAllFarmerOrders = async (filters = {}) => {
  const cleanedFilters = Object.fromEntries(
    Object.entries(filters).filter(
    ([_, v]) => v && v.toString().trim() !== ''
  )
  );
  const queryString = new URLSearchParams(cleanedFilters).toString();
  const response = await privateApi.get(`/user/orders?${queryString}`);
  return response.data ?? {}; 
};



export const getFarmerOrderById = (orderId) => {
  return privateApi.get(`/user/orders/${orderId}`);
  
};

export const shipOrderApi = async (orderId) => {
  return privateApi.post(`/farmer/order/${orderId}/shipped`);
   
}

export const deliverOrderApi = async (orderId, {otp}) => {
  return privateApi.post(`/farmer/order/${orderId}/delivered`, { otp });
}
export const confirmOrderApi =async (orderId) => {
  const token = localStorage.getItem('token');
    const response = await fetch(`${BASE_URL}/farmer/order/${orderId}/confirmed`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
      }
    });
    const raw = await response.text();
  let parsed = raw ? JSON.parse(raw) : {};
  
  if (!response.ok) {
    const errorMessage = (parsed && parsed.message) || response.statusText || 'API Error';
    const successMsg = response?.data?.message || "Crop added successfully";
    toast.success(successMsg);
    throw new Error(errorMessage);
  }
  
  return parsed;
  }


