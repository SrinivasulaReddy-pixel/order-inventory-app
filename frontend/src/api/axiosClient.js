import axios from 'axios';

// All requests go through the API Gateway, not individual services directly.
const axiosClient = axios.create({
  baseURL: 'http://localhost:8080/api',
});

// Attach the JWT to every outgoing request. Read from localStorage (where authSlice
// persists it) rather than importing the Redux store directly here, to avoid a
// store <-> axiosClient <-> slice circular import.
axiosClient.interceptors.request.use((config) => {
  const persisted = JSON.parse(localStorage.getItem('auth') || 'null');
  if (persisted && persisted.token) {
    config.headers.Authorization = `Bearer ${persisted.token}`;
  }
  return config;
});

// If the gateway/a service rejects the token, bounce back to login.
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('auth');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
