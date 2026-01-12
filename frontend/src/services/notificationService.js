import  { privateApi } from '../services/apiClient';
const BASE_URL = 'http://localhost:8081';


export const deleteMultipleNotifications = (notificationIds) => {
  const query = notificationIds.map(id => `ids=${encodeURIComponent(id)}`).join('&');
  return privateApi.delete(`/user/notifications/delete?${query}`);
};

export const readMultipleNotifications = (notificationIds) => {
  const query = notificationIds.map(id => `ids=${encodeURIComponent(id)}`).join('&');
  return privateApi.delete(`/user/notifications/read?${query}`);
};


export const getNotifications = async () => {
  const token = localStorage.getItem('token');
  try {
    const response = await fetch(`${BASE_URL}/user/notifications`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      }
    });

    if (!response.ok) {
      throw new Error('Failed to get Notification');
    }

    return await response.json();
  } catch (error) {
    console.error('Update crop error:', error);
    throw error;
  }
};

export const getNotification = (notificationId) => {
  return privateApi.get(`/user/notifications/${notificationId}`);
};