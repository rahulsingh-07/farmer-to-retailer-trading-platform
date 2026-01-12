// src/services/farmerService.js
import  { privateApi } from '../../services/apiClient';

export const getPendingUsers = (page, limit) => {
  return privateApi.get(`/admin/pendingUsers?page=${page}&limit=${limit}`);
}

export const getAdminStats = () => {
  return privateApi.get('/admin/stats');
}

export const updateUserStatus = (userId, status) => {
  return privateApi.patch(`/admin/user/${userId}/status?status=${status}`);
}

export const createAdminUser = (adminData) => {
  return privateApi.post('/admin/newAdmin', adminData);
}