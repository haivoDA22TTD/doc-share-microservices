import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useLanguage } from '../contexts/LanguageContext'
import api from '../services/api'

const Documents = () => {
  const [myDocuments, setMyDocuments] = useState([])
  const [sharedDocuments, setSharedDocuments] = useState([])
  const [activeTab, setActiveTab] = useState('my')
  const [loading, setLoading] = useState(true)
  const [userDetails, setUserDetails] = useState(null)
  const [shareModalOpen, setShareModalOpen] = useState(false)
  const [selectedDocId, setSelectedDocId] = useState(null)
  const [shareUsername, setShareUsername] = useState('')
  const [sharePermission, setSharePermission] = useState('VIEW')
  const { user, logout } = useAuth()
  const { t, language, toggleLanguage } = useLanguage()
  const navigate = useNavigate()

  useEffect(() => {
    fetchUserDetails()
    loadMyDocuments()
  }, [])

  const fetchUserDetails = async () => {
    try {
      const response = await api.get('/api/users/me')
      setUserDetails(response.data)
    } catch (error) {
      console.error('Error fetching user details:', error)
    }
  }

  const loadMyDocuments = async () => {
    try {
      const response = await api.get('/api/documents/my-documents')
      setMyDocuments(response.data)
    } catch (error) {
      console.error('Error loading documents:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadSharedDocuments = async () => {
    try {
      const response = await api.get('/api/documents/shared-with-me')
      setSharedDocuments(response.data)
    } catch (error) {
      console.error('Error loading shared documents:', error)
    }
  }

  const handleTabChange = (tab) => {
    setActiveTab(tab)
    if (tab === 'shared' && sharedDocuments.length === 0) {
      loadSharedDocuments()
    }
  }

  const handleDeleteDocument = async (id) => {
    if (!confirm(t('docs.deleteConfirm'))) return

    try {
      await api.delete(`/api/documents/${id}`)
      setMyDocuments(myDocuments.filter(doc => doc.id !== id))
    } catch (error) {
      console.error('Error deleting document:', error)
      alert(t('docs.deleteFailed'))
    }
  }

  const handleOpenDocument = (id) => {
    navigate(`/document/${id}`)
  }

  const handleShareDocument = (docId) => {
    setSelectedDocId(docId)
    setShareModalOpen(true)
    setShareUsername('')
    setSharePermission('VIEW')
  }

  const handleShareSubmit = async (e) => {
    e.preventDefault()
    
    if (!shareUsername.trim()) {
      alert('Vui lòng nhập username')
      return
    }

    try {
      await api.post(`/api/documents/${selectedDocId}/share`, {
        username: shareUsername,
        permission: sharePermission
      })
      
      alert('Chia sẻ tài liệu thành công!')
      setShareModalOpen(false)
      setShareUsername('')
    } catch (error) {
      console.error('Error sharing document:', error)
      alert(error.response?.data?.message || 'Lỗi khi chia sẻ tài liệu')
    }
  }

  const documents = activeTab === 'my' ? myDocuments : sharedDocuments

  return (
    <div className="documents-page">
      {/* Navigation */}
      <nav className="documents-nav">
        <div className="nav-container">
          <div className="nav-left">
            <h1 className="nav-logo">DocShare</h1>
            <Link to="/dashboard" className="nav-link">{t('nav.dashboard')}</Link>
            <Link to="/documents" className="nav-link active">{t('nav.documents')}</Link>
          </div>
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
      <div className="documents-content">
        <div className="documents-header">
          <h2>{t('docs.title')}</h2>
          <Link to="/document/new" className="btn btn-primary">
            {t('docs.newDoc')}
          </Link>
        </div>

        {/* Tabs */}
        <div className="tabs-container">
          <div className="tabs">
            <button
              className={`tab ${activeTab === 'my' ? 'tab-active' : ''}`}
              onClick={() => handleTabChange('my')}
            >
              {t('docs.myDocs')}
            </button>
            <button
              className={`tab ${activeTab === 'shared' ? 'tab-active' : ''}`}
              onClick={() => handleTabChange('shared')}
            >
              {t('docs.sharedWithMe')}
            </button>
          </div>
        </div>

        {/* Documents List */}
        <div className="documents-list">
          {loading ? (
            <div className="loading">{t('common.loading')}</div>
          ) : documents.length === 0 ? (
            <div className="empty-state">
              <p>{t('docs.noDocuments')}</p>
            </div>
          ) : (
            documents.map((doc) => (
              <div key={doc.id} className="document-item">
                <div className="document-info">
                  <h3 onClick={() => handleOpenDocument(doc.id)} className="document-title">
                    {doc.title}
                  </h3>
                  <p className="document-meta">
                    {t('docs.owner')}: {doc.ownerUsername} | {t('docs.permission')}: {doc.userPermission}
                  </p>
                  <p className="document-date">
                    {t('docs.lastEdited')}: {new Date(doc.lastEditedAt).toLocaleString()}
                  </p>
                </div>
                {doc.userPermission === 'OWNER' && (
                  <div className="document-actions">
                    <button 
                      onClick={() => handleShareDocument(doc.id)}
                      className="btn-action btn-action-share"
                    >
                      {t('docs.share')}
                    </button>
                    <button 
                      onClick={() => handleDeleteDocument(doc.id)}
                      className="btn-action btn-action-delete"
                    >
                      {t('docs.delete')}
                    </button>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      </div>

      {/* Share Modal */}
      {shareModalOpen && (
        <div className="modal-overlay" onClick={() => setShareModalOpen(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h3>Chia sẻ tài liệu</h3>
            <form onSubmit={handleShareSubmit}>
              <div className="form-group">
                <label htmlFor="shareUsername">Username người dùng:</label>
                <input
                  type="text"
                  id="shareUsername"
                  value={shareUsername}
                  onChange={(e) => setShareUsername(e.target.value)}
                  placeholder="username"
                  required
                />
              </div>
              
              <div className="form-group">
                <label htmlFor="sharePermission">Quyền:</label>
                <select
                  id="sharePermission"
                  value={sharePermission}
                  onChange={(e) => setSharePermission(e.target.value)}
                >
                  <option value="VIEW">Xem</option>
                  <option value="EDIT">Chỉnh sửa</option>
                </select>
              </div>

              <div className="modal-actions">
                <button type="submit" className="btn btn-primary">
                  Chia sẻ
                </button>
                <button 
                  type="button" 
                  onClick={() => setShareModalOpen(false)}
                  className="btn btn-secondary"
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default Documents
