import { createContext, useState, useContext, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'

const AuthContext = createContext(null)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    console.log('AuthContext: Checking for existing token...')
    const token = localStorage.getItem('token')
    if (token) {
      console.log('AuthContext: Token found in localStorage')
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
      setUser({ token })
    } else {
      console.log('AuthContext: No token found')
    }
    setLoading(false)
  }, [])

  const login = async (credentials) => {
    try {
      console.log('=== LOGIN ATTEMPT ===')
      console.log('API URL:', api.defaults.baseURL)
      console.log('Credentials:', { username: credentials.username })
      
      const response = await api.post('/api/auth/login', credentials)
      console.log('✅ Login response received')
      console.log('Response status:', response.status)
      console.log('Response data:', response.data)
      
      const { token, userId, username, email } = response.data
      
      if (!token) {
        console.error('❌ NO TOKEN IN RESPONSE!')
        return { 
          success: false, 
          error: 'Không nhận được token từ server' 
        }
      }
      
      console.log('Token received, length:', token.length)
      console.log('Saving to localStorage...')
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify({ userId, username, email }))
      
      const savedToken = localStorage.getItem('token')
      console.log('Token saved successfully:', savedToken === token)
      
      setUser({ token, userId, username, email })
      console.log('✅ Login successful, navigating to dashboard...')
      navigate('/dashboard')
      
      return { success: true }
    } catch (error) {
      console.error('❌ LOGIN FAILED')
      console.error('Error:', error)
      console.error('Response:', error.response?.data)
      console.error('Status:', error.response?.status)
      return { 
        success: false, 
        error: error.response?.data?.message || error.message || 'Đăng nhập thất bại' 
      }
    }
  }

  const register = async (userData) => {
    try {
      const response = await api.post('/api/auth/register', userData)
      const { token, userId, username, email } = response.data
      
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify({ userId, username, email }))
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
      
      setUser({ token, userId, username, email })
      navigate('/dashboard')
      
      return { success: true }
    } catch (error) {
      return { 
        success: false, 
        error: error.response?.data?.message || 'Registration failed' 
      }
    }
  }

  const loginWithGoogle = (token) => {
    console.log('Google OAuth token received:', token ? 'Yes' : 'No')
    localStorage.setItem('token', token)
    // Note: User info will be fetched from /api/users/me
    api.defaults.headers.common['Authorization'] = `Bearer ${token}`
    setUser({ token })
    console.log('Redirecting to dashboard...')
    navigate('/dashboard')
  }

  const logout = async () => {
    try {
      await api.post('/api/auth/logout')
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      delete api.defaults.headers.common['Authorization']
      setUser(null)
      navigate('/login')
    }
  }

  const value = {
    user,
    loading,
    login,
    register,
    loginWithGoogle,
    logout,
    isAuthenticated: !!user
  }

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  )
}
