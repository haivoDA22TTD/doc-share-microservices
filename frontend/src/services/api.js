import axios from 'axios'

// Use API Gateway as single entry point
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      // Clear all auth data
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      // Redirect to login
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login?error=session_expired'
      }
    }
    return Promise.reject(error)
  }
)

export default api
