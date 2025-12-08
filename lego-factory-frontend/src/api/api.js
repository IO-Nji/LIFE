import axios from 'axios';

// Use relative path for production deployment via Nginx proxy
// In development, Vite proxy will handle the /api routing
const API_GATEWAY_URL = import.meta.env.VITE_API_GATEWAY_URL ?? '/api';

const api = axios.create({
  baseURL: API_GATEWAY_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add response interceptor to handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid, clear session and redirect to login
      localStorage.removeItem('authToken');
      localStorage.removeItem('authSession');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
