import { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'

const OAuth2RedirectHandler = () => {
  const [searchParams] = useSearchParams()

  useEffect(() => {
    console.log('=== OAuth2RedirectHandler START ===')
    console.log('Full URL:', window.location.href)
    
    const token = searchParams.get('token')
    const error = searchParams.get('error')

    console.log('Token:', token)
    console.log('Error:', error)

    if (token) {
      console.log('Saving token to localStorage...')
      localStorage.setItem('token', token)
      
      console.log('Token saved, redirecting to dashboard...')
      // Use window.location for hard redirect
      window.location.href = '/dashboard'
    } else if (error) {
      console.error('OAuth2 error:', error)
      window.location.href = '/login?error=' + error
    } else {
      console.log('No token or error, redirecting to login')
      window.location.href = '/login'
    }
  }, [searchParams])

  return (
    <div className="oauth2-redirect">
      <div className="loading">
        <div className="spinner"></div>
        <p>Đang hoàn tất đăng nhập...</p>
      </div>
    </div>
  )
}

export default OAuth2RedirectHandler
