const API_BASE_URL = "http://localhost:8081"; // make sure this is defined

// Helper to handle response with tolerant parsing (handles empty or non-JSON bodies)
const checkResponse = async (response) => {
  const raw = await response.text();
  let parsed;

  if (raw) {
    try {
      parsed = JSON.parse(raw);
    } catch (e) {
      // Not valid JSON; keep the raw text
      parsed = raw;
    }
  }

  if (!response.ok) {
    const errorMessage = (parsed && parsed.message) || response.statusText || 'API Error';
    throw new Error(errorMessage);
  }

  // If JSON parsed to object/array, return it; otherwise return raw text
  return typeof parsed === 'undefined' ? {} : parsed;
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
