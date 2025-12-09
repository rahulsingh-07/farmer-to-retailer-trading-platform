const API_BASE_URL = "http://localhost:8081"; // make sure this is defined

// Helper to handle response
const checkResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const errorMessage = errorData.message || response.statusText || 'API Error';
    throw new Error(errorMessage);
  }
  return response.json();
};

const api = {
  get: async (path, token) => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: 'GET',
      headers: {
        'Authorization': token ? `Bearer ${token}` : undefined,  // better: don't send empty
        'Content-Type': 'application/json',
      },
    });
    return checkResponse(response);
  },
  post: async (path, data, token) => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: 'POST',
      headers: {
        'Authorization': token ? `Bearer ${token}` : undefined,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
    return checkResponse(response);
  },
  upload: async (path, formData, token) => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: 'POST',
      headers: {
        Authorization: token ? `Bearer ${token}` : undefined
        // ⚠️ DON'T set 'Content-Type', browser sets it for FormData
      },
      body: formData,
    });
    return checkResponse(response);
  },
  patch: async (path, data, token) => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: "PATCH",
      headers: {
        'Authorization': token ? `Bearer ${token}` : undefined,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });
    return checkResponse(response);
  },
  delete: async (path, token) => {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: 'DELETE',
      headers: {
        'Authorization': token ? `Bearer ${token}` : undefined,
        'Content-Type': 'application/json',
      },
    });
    return checkResponse(response);
  },
};

export default api;
