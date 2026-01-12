const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8081";

/* =======================
   Response Handler
   ======================= */
const checkResponse = async (response, { auth = true } = {}) => {
  // Some endpoints return 204/empty bodies; guard against JSON parsing errors
  let data = {};
  const text = await response.text();

  if (text) {
    try {
      data = JSON.parse(text) || {};
    } catch {
      data = {}; // default empty object instead of null
    }
  }

  if (auth && response.status === 401) {
    localStorage.clear();
    window.location.href = "/login";
    throw new Error("Session expired");
  }

  if (auth && response.status === 403) {
    window.location.href = "/unauthorized";
    throw new Error("Access denied");
  }

  if (!response.ok) {
    const error = new Error(data?.message || "Request failed");
    error.response = { data };
    error.status = response.status;
    throw error;
  }

  return data;
};

/* =======================
   Core Request  
   ======================= */
const request = async (url, options = {}, { auth = true } = {}) => {
  const token = localStorage.getItem("token");
  let finalUrl = `${BASE_URL}${url}`;

  if (options.params) {
    finalUrl += `?${new URLSearchParams(options.params).toString()}`;
  }
  const headers = {
    ...(options.body instanceof FormData ? {} : { "Content-Type": "application/json" }),
    ...(options.headers || {}),
    ...(auth && token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const response = await fetch(finalUrl, {
    ...options,
    headers,
  });

  return checkResponse(response, { auth });
};

/* =======================
   PUBLIC API (NO TOKEN)
   ======================= */
export const publicApi = {
  get: (url) =>
    request(url, { method: "GET" }, { auth: false }),

  post: (url, body) =>
    request(
      url,
      {
        method: "POST",
        body: JSON.stringify(body),
      },
      { auth: false }
    ),

  upload: (url, formData) =>
    request(
      url,
      {
        method: "POST",
        body: formData,
      },
      { auth: false }
    ),
};

/* =======================
   PRIVATE API (WITH TOKEN)
   ======================= */
export const privateApi = {
  get: (url) =>
    request(url, { method: "GET" }),

  post: (url, body) =>
    request(url, {
      method: "POST",
      body: JSON.stringify(body),
    }),

  put: (url, body) =>
    request(url, {
      method: "PUT",
      body: JSON.stringify(body),
    }),

  patch: (url, body) =>
    request(url, {
      method: "PATCH",
      ...(body ? { body: JSON.stringify(body) } : {}),
    }),

  delete: (url) =>
    request(url, { method: "DELETE" }),

  upload: (url, formData) =>
    request(
      url,
      {
        method: "POST",
        body: formData,
      }
    ),

};
