import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useLanguage } from '../contexts/LanguageContext'
import api from '../services/api'

const Dashboard = () => {
  const [documents, setDocuments] = useState([])
  const [loading, setLoading] = useState(true)
  const [userDetails, setUserDetails] = useState(null)
  const { user, logout } = useAuth()
  const { t, language, toggleLanguage } = useLanguage()
  const navigate = useNavigate()

  const fetchDocuments = async () => {
    try {
      const response = await api.get('/api/documents/my-documents')
      setDocuments(response.data)
    } catch (error) {
      console.error('Error fetching documents:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchUserDetails = async () => {
    try {
      const response = await api.get('/api/users/me')
      setUserDetails(response.data)
    } catch (error) {
      console.error('Error fetching user details:', error)
    }
  }

  useEffect(() => {
    const loadData = async () => {
      await fetchUserDetails()
      // Add small delay to avoid rate limit
      await new Promise(resolve => setTimeout(resolve, 100))
      await fetchDocuments()
    }
    loadData()
  }, [])

  const handleOpenDocument = (docId) => {
    navigate(`/document/${docId}`)
  }

  return (
    <div className="dashboard">
      {/* Navigation */}
      <nav className="dashboard-nav">
        <div className="nav-container">
          <h1 className="nav-logo">DocShare</h1>
          <div className="nav-actions">
            <button onClick={toggleLanguage} className="language-toggle">
              {language === 'en' ? '🇻🇳 VI' : '🇬🇧 EN'}
            </button>
            <span className="user-info">
              {t('nav.hello')}, {userDetails?.username || user?.username}
            </span>
            <button onClick={logout} className="btn btn-danger">
              {t('nav.logout')}
            </button>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="dashboard-content">
        {/* Welcome Section */}
        <div className="welcome-section">
          <h2>{t('dashboard.welcome')}</h2>
          <p className="subtitle">{t('dashboard.subtitle')}</p>
          
          {userDetails && (
            <div className="user-details">
              <h3>{t('dashboard.userInfo')}</h3>
              <div className="user-data">
                <p><strong>{t('dashboard.username')}:</strong> {userDetails.username}</p>
                <p><strong>{t('dashboard.email')}:</strong> {userDetails.email}</p>
                <p><strong>{t('dashboard.fullName')}:</strong> {userDetails.fullName}</p>
                <p><strong>{t('dashboard.roles')}:</strong> {userDetails.roles?.join(', ')}</p>
                <p><strong>{t('dashboard.accountCreated')}:</strong> {new Date(userDetails.createdAt).toLocaleDateString()}</p>
              </div>
            </div>
          )}
        </div>

        {/* Quick Actions */}
        <div className="quick-actions">
          <h3>{t('dashboard.quickActions')}</h3>
          <div className="actions-grid">
            <button 
              onClick={() => navigate('/document/new')}
              className="action-btn action-btn-primary"
            >
              {t('dashboard.createNew')}
            </button>
            <button 
              onClick={() => navigate('/documents')}
              className="action-btn action-btn-success"
            >
              {t('dashboard.myDocuments')}
            </button>
            <button 
              onClick={() => navigate('/documents')}
              className="action-btn action-btn-purple"
            >
              {t('dashboard.sharedWithMe')}
            </button>
          </div>
        </div>

        {/* Documents List */}
        <div className="documents-section">
          <h3>{t('dashboard.myDocuments')}</h3>
          
          {loading ? (
            <div className="loading">{t('common.loading')}</div>
          ) : documents.length === 0 ? (
            <div className="empty-state">
              <p>{t('dashboard.noDocuments')}</p>
              <p className="empty-hint">{t('dashboard.createFirst')}</p>
            </div>
          ) : (
            <div className="documents-grid">
              {documents.map((doc) => (
                <div 
                  key={doc.id} 
                  onClick={() => handleOpenDocument(doc.id)}
                  className="document-card"
                >
                  <h4>{doc.title}</h4>
                  <p className="doc-meta">
                    {t('dashboard.lastEdited')}: {new Date(doc.lastEditedAt).toLocaleDateString()}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Dashboard
