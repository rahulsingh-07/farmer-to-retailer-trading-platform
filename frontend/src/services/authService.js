import { publicApi } from "./apiClient";

/**
 * LOGIN
 * POST /auth/login
 */
export const login = (username, password) => {
  return publicApi.post("/auth/login", { username, password });
};

/**
 * FARMER REGISTER
 * POST /auth/register/farmer
 */
export const registerFarmer = (farmerData) => {
  return publicApi.post("/auth/register/farmer", { ...farmerData });
};

/**
 * RETAILER REGISTER
 * POST /auth/register/retailer
 */
export const registerRetailer = (formData) => {
  return publicApi.upload("/auth/register/retailer", formData);
};

export const landingPageStats = async () => {
  const response = await publicApi.get("/auth/landingPageStats");
  return response.data;
};

/**
 * FORGOT PASSWORD
 */
export const forgetPassword = (email) => {
  return publicApi.post("/auth/forgotPassword", { email });
};

/**
 * VALIDATE RESET TOKEN
 */
export const validateResetToken = (token) => {
  return publicApi.get(`/auth/validate-token?token=${token}`);
};

/**
 * SET NEW PASSWORD
 */
export const setNewPassword = (token, password) => {
  return publicApi.post(
    `/auth/set-password?token=${encodeURIComponent(token)}`,
    { password }
  );
};

