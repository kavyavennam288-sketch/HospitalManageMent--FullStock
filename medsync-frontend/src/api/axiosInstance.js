import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

// ── Request Interceptor ───────────────────────────────────────────────
// Runs before EVERY outgoing request.
// Reads the token from localStorage and attaches it to the Authorization header.
// This means every API call automatically sends: "Authorization: Bearer <token>"
// without any page needing to manually set it.
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response Interceptor ──────────────────────────────────────────────
// Runs after EVERY response comes back.
// If the server returns 401 (token expired or invalid),
// clear localStorage and redirect to login automatically.
// Without this, users would see confusing error messages instead of being
// sent back to the login page.
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;