import { a, s } from 'framer-motion/client';
import {publicApi,privateApi} from '../../services/apiClient';
const BASE_URL = 'http://localhost:8081';

export const getFilteredCrops = async (filters = {}) => {
  const cleanedFilters = Object.fromEntries(
  Object.entries(filters).filter(
    ([_, v]) => v && v.toString().trim() !== ''
  )
);
  const queryString = new URLSearchParams(cleanedFilters).toString();
  const response = await publicApi.get(`/public/crops?${queryString}`);
  return response.data ?? {};
};


export const getCropById = (cropId) => {
  const res=privateApi.get(`/public/crop/${cropId}`);
  return res;
}

export const placeBid = async (auctionId, bidData) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${BASE_URL }/public/auctions/${auctionId}/bid`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(bidData)
  });
  
  const data = await response.json();
  if (!response.ok) throw new Error(data.message || 'Bid failed');
  return data;
};

export const getMyBids = async () => {
  return privateApi.get('/public/retailer/bids');
}



// Order operations (Your backend endpoints)
export const getAllRetailerOrders  = async (filters = {}) => {
  const cleanedFilters = Object.fromEntries(
    Object.entries(filters).filter(
    ([_, v]) => v && v.toString().trim() !== ''
  )
  );
  const queryString = new URLSearchParams(cleanedFilters).toString();
  const response = await privateApi.get(`/user/orders?${queryString}`);
  return response.data ?? {}; 
};

export const getRetailerOrderById = (orderId) => {
  return privateApi.get(`/user/orders/${orderId}`);
};

export const buyNow=(cropId)=>{
  return privateApi.post(`/public/crop/${cropId}/buy-now`)
}

export const addReview = async (orderId, reviewData) => {
  const res = await privateApi.post(`/user/orders/${orderId}/review`, reviewData);
  return res.data;  // directly the API response
};