import axios from 'axios';
import Cookies from 'js-cookie';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add a request interceptor to include the token
apiClient.interceptors.request.use(
  (config) => {
    const token = Cookies.get('auth_token');
    if (token) {
      if (config.params) {
        config.params.token = token;
      } else {
        config.params = { token };
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default apiClient;
