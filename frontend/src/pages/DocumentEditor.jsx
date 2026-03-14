import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useLanguage } from '../contexts/LanguageContext'
import api from '../services/api'
import collaborationService from '../services/collaboration'

const DocumentEditor = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const { logout, user } = useAuth()
  const { t, language, toggleLanguage } = useLanguage()
  
  const [title, setTitle] = useState('')
  const [isPublic, setIsPublic] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [saveStatus, setSaveStatus] = useState('')
  const [isReadOnly, setIsReadOnly] = useState(false)
  const [wordCount, setWordCount] = useState(0)
  const [charCount, setCharCount] = useState(0)
  const [onlineUsers, setOnlineUsers] = useState([])
  const [remoteCursors, setRemoteCursors] = useState({})
  
  const editorRef = useRef(null)
  const quillRef = useRef(null)
  const isNewDocument = !id || id === 'new'
  const isUpdatingFromRemote = useRef(false)

  console.log('DocumentEditor - id:', id, 'isNewDocument:', isNewDocument)

  // Initialize Quill
  useEffect(() => {
    let mounted = true
    
    const initQuill = () => {
      if (!mounted) return
      if (!window.Quill) {
        console.error('Quill not loaded')
        return
      }
      if (!editorRef.current) {
        console.error('Editor ref not ready')
        return
      }
      if (quillRef.current) {
        console.log('Quill already initialized')
        return
      }

      try {
        console.log('Initializing Quill...')
        
        quillRef.current = new window.Quill(editorRef.current, {
          theme: 'snow',
          placeholder: 'Bắt đầu nhập nội dung...',
          modules: {
            toolbar: [
              [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
              [{ 'font': [] }],
              [{ 'size': ['small', false, 'large', 'huge'] }],
              ['bold', 'italic', 'underline', 'strike'],
              [{ 'color': [] }, { 'background': [] }],
              [{ 'script': 'sub'}, { 'script': 'super' }],
              [{ 'list': 'ordered'}, { 'list': 'bullet' }],
              [{ 'indent': '-1'}, { 'indent': '+1' }],
              [{ 'align': [] }],
              ['blockquote', 'code-block'],
              ['link', 'image', 'video'],
              ['clean']
            ]
          }
        })

        console.log('Quill initialized!')

        // Track text changes with minimal debounce
        let textChangeTimeout = null
        let lastSentContent = ''
        
        quillRef.current.on('text-change', (delta, oldDelta, source) => {
          if (!quillRef.current) return
          
          const text = quillRef.current.getText().trim()
          const words = text ? text.split(/\s+/).length : 0
          setWordCount(words)
          setCharCount(text.length)
          
          // Only send changes if user is typing (not from remote)
          if (source === 'user' && !isUpdatingFromRemote.current && !isNewDocument) {
            // Clear previous timeout
            if (textChangeTimeout) {
              clearTimeout(textChangeTimeout)
            }
            
            // Minimal debounce: 100ms for fast sync
            textChangeTimeout = setTimeout(() => {
              if (!quillRef.current) return
              const content = quillRef.current.root.innerHTML
              
              // Only send if content actually changed
              if (content !== lastSentContent) {
                lastSentContent = content
                const user = collaborationService.getUserInfo()
                console.log('📤 Sending content update, length:', content.length)
                collaborationService.sendDocumentChange(
                  user.username,
                  user.userId,
                  content
                )
              }
            }, 100)
          }
        })

        // Track cursor position (minimal debounce)
        let cursorTimeout = null
        quillRef.current.on('selection-change', (range) => {
          if (range && !isNewDocument && !isUpdatingFromRemote.current) {
            if (cursorTimeout) {
              clearTimeout(cursorTimeout)
            }
            
            cursorTimeout = setTimeout(() => {
              const user = collaborationService.getUserInfo()
              console.log('📍 Sending cursor position:', range.index, 'user:', user.username)
              collaborationService.sendCursorPosition(
                user.username,
                user.userId,
                range.index
              )
            }, 50)
          }
        })

      } catch (error) {
        console.error('Error initializing Quill:', error)
      }
    }

    if (window.Quill) {
      setTimeout(initQuill, 100)
    } else {
      let attempts = 0
      const checkInterval = setInterval(() => {
        attempts++
        if (window.Quill) {
          clearInterval(checkInterval)
          setTimeout(initQuill, 100)
        } else if (attempts > 50) {
          clearInterval(checkInterval)
          console.error('Quill failed to load')
        }
      }, 100)

      return () => {
        mounted = false
        clearInterval(checkInterval)
      }
    }

    return () => {
      mounted = false
    }
  }, [])

  // Load document
  useEffect(() => {
    if (isNewDocument) {
      console.log('New document mode')
      setLoading(false)
      return
    }

    const loadDocument = async () => {
      try {
        console.log('Loading document with ID:', id)
        const response = await api.get(`/api/documents/${id}`)
        const doc = response.data
        
        console.log('Document loaded:', doc)
        setTitle(doc.title)
        setIsPublic(doc.isPublic)
        setIsReadOnly(doc.userPermission === 'VIEW')

        if (quillRef.current && doc.content) {
          quillRef.current.root.innerHTML = doc.content
        }

      } catch (error) {
        console.error('Error loading document:', error)
        if (error.response?.status === 403) {
          alert('Bạn không có quyền truy cập tài liệu này')
          navigate('/documents')
        } else if (error.response?.status === 404) {
          alert('Tài liệu không tồn tại')
          navigate('/documents')
        } else {
          alert('Lỗi khi tải tài liệu: ' + (error.response?.data?.message || error.message))
          navigate('/documents')
        }
      } finally {
        setLoading(false)
      }
    }

    loadDocument()
  }, [id, isNewDocument, navigate])

  // WebSocket connection for collaboration
  useEffect(() => {
    if (isNewDocument || !id) return

    console.log('Setting up WebSocket for document:', id)

    // Add current user to online list immediately
    const user = collaborationService.getUserInfo()
    if (user.userId) {
      setOnlineUsers([{
        userId: user.userId,
        username: user.username,
        color: getUserColor(user.userId)
      }])
    }

    collaborationService.connect(id, {
      onConnect: () => {
        console.log('✅ Connected to collaboration service')
      },
      onDocumentChange: (change) => {
        console.log('📝 Document changed by:', change.username, 'userId:', change.userId)
        const user = collaborationService.getUserInfo()
        
        // Don't update if it's from current user
        if (change.userId === user.userId) {
          console.log('Ignoring own change')
          return
        }
        
        // Don't update if user is currently typing
        if (quillRef.current && quillRef.current.hasFocus()) {
          console.log('⚠️ User is typing, skipping remote update')
          return
        }
        
        // Update editor content
        if (quillRef.current && change.content) {
          const currentContent = quillRef.current.root.innerHTML
          
          // Only update if content is actually different
          if (currentContent === change.content) {
            console.log('Content is same, skipping update')
            return
          }
          
          console.log('📥 Updating content from remote user, length:', change.content.length)
          
          isUpdatingFromRemote.current = true
          
          try {
            // Save current selection position
            const selection = quillRef.current.getSelection()
            const cursorPosition = selection ? selection.index : 0
            
            // Update content using clipboard to preserve formatting
            const delta = quillRef.current.clipboard.convert(change.content)
            quillRef.current.setContents(delta, 'silent')
            
            // Restore cursor position if user had selection
            if (selection && cursorPosition > 0) {
              setTimeout(() => {
                try {
                  const newLength = quillRef.current.getLength()
                  const safePosition = Math.min(cursorPosition, newLength - 1)
                  quillRef.current.setSelection(safePosition, 0)
                } catch (e) {
                  console.error('Error restoring cursor:', e)
                }
              }, 10)
            }
          } catch (error) {
            console.error('Error updating content:', error)
          } finally {
            setTimeout(() => {
              isUpdatingFromRemote.current = false
            }, 50)
          }
        }
      },
      onCursorMove: (cursor) => {
        const user = collaborationService.getUserInfo()
        if (cursor.userId === user.userId) return
        
        console.log('🖱️ Cursor moved:', cursor.username, 'index:', cursor.index)
        
        setRemoteCursors(prev => ({
          ...prev,
          [cursor.userId]: {
            username: cursor.username,
            index: cursor.index || 0,
            color: getUserColor(cursor.userId)
          }
        }))
      },
      onUsersList: (users) => {
        console.log('📋 Received active users list:', users)
        const user = collaborationService.getUserInfo()
        
        // Convert Set to Array and add current user if not in list
        const usersList = Array.isArray(users) ? users : Array.from(users)
        const allUsers = usersList.map(u => ({
          userId: u.userId,
          username: u.username,
          color: getUserColor(u.userId)
        }))
        
        // Add current user if not already in list
        if (!allUsers.find(u => u.userId === user.userId)) {
          allUsers.unshift({
            userId: user.userId,
            username: user.username,
            color: getUserColor(user.userId)
          })
        }
        
        console.log('Setting online users:', allUsers)
        setOnlineUsers(allUsers)
      },
      onUserPresence: (presence) => {
        console.log('👤 User presence:', presence.action, presence.username, 'userId:', presence.userId)
        const user = collaborationService.getUserInfo()
        
        // Don't add self again
        if (presence.userId === user.userId) {
          console.log('Ignoring own presence')
          return
        }
        
        if (presence.action === 'JOIN') {
          setOnlineUsers(prev => {
            if (!prev.find(u => u.userId === presence.userId)) {
              console.log('Adding user to online list:', presence.username)
              return [...prev, {
                userId: presence.userId,
                username: presence.username,
                color: getUserColor(presence.userId)
              }]
            }
            return prev
          })
        } else if (presence.action === 'LEAVE') {
          console.log('Removing user from online list:', presence.username)
          setOnlineUsers(prev => prev.filter(u => u.userId !== presence.userId))
          setRemoteCursors(prev => {
            const newCursors = { ...prev }
            delete newCursors[presence.userId]
            return newCursors
          })
        }
      },
      onError: (error) => {
        console.error('❌ WebSocket error:', error)
      }
    })

    return () => {
      console.log('Disconnecting WebSocket...')
      collaborationService.disconnect()
      setOnlineUsers([])
      setRemoteCursors({})
    }
  }, [id, isNewDocument])

  // Render remote cursors
  useEffect(() => {
    if (!quillRef.current || !editorRef.current) return

    // Remove old cursors
    const oldCursors = editorRef.current.querySelectorAll('.remote-cursor')
    oldCursors.forEach(cursor => cursor.remove())

    // Render new cursors
    Object.entries(remoteCursors).forEach(([userId, cursor]) => {
      try {
        const bounds = quillRef.current.getBounds(cursor.index)
        if (!bounds) return

        const cursorEl = document.createElement('div')
        cursorEl.className = 'remote-cursor'
        cursorEl.style.backgroundColor = cursor.color
        cursorEl.style.left = `${bounds.left}px`
        cursorEl.style.top = `${bounds.top}px`
        cursorEl.style.height = `${bounds.height}px`

        const label = document.createElement('div')
        label.className = 'cursor-label'
        label.textContent = cursor.username
        cursorEl.appendChild(label)

        const editor = editorRef.current.querySelector('.ql-editor')
        if (editor) {
          editor.style.position = 'relative'
          editor.appendChild(cursorEl)
        }
      } catch (error) {
        console.error('Error rendering cursor:', error)
      }
    })
  }, [remoteCursors])

  // Generate consistent color for each user
  const getUserColor = (userId) => {
    const colors = [
      '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', 
      '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2'
    ]
    return colors[userId % colors.length]
  }

  // Save document
  const handleSave = async () => {
    if (!quillRef.current) {
      alert('Trình soạn thảo chưa sẵn sàng')
      return
    }

    const docTitle = title.trim() || 'Tài liệu chưa đặt tên'
    const content = quillRef.current.root.innerHTML

    console.log('=== SAVING DOCUMENT ===')
    console.log('Title:', docTitle)
    console.log('Content length:', content.length)
    console.log('Content preview:', content.substring(0, 100))
    console.log('Is Public:', isPublic)
    console.log('Is New Document:', isNewDocument)
    console.log('Document ID:', id)

    setSaving(true)
    setSaveStatus('Đang lưu...')

    try {
      const payload = { 
        title: docTitle, 
        content: content, 
        isPublic: isPublic 
      }

      console.log('Payload:', JSON.stringify(payload, null, 2))

      let response
      if (isNewDocument) {
        console.log('POST /api/documents')
        response = await api.post('/api/documents', payload)
        console.log('✅ Document created:', response.data)
        setSaveStatus('Đã lưu ✓')
        setTimeout(() => {
          navigate(`/document/${response.data.id}`, { replace: true })
        }, 500)
      } else {
        console.log(`PUT /api/documents/${id}`)
        response = await api.put(`/api/documents/${id}`, payload)
        console.log('✅ Document updated:', response.data)
        setSaveStatus('Đã lưu ✓')
        setTimeout(() => setSaveStatus(''), 2000)
      }
    } catch (error) {
      console.error('❌ SAVE FAILED')
      console.error('Error:', error)
      console.error('Error response:', error.response)
      console.error('Error data:', error.response?.data)
      console.error('Error status:', error.response?.status)
      console.error('Error headers:', error.response?.headers)
      
      const errorMsg = error.response?.data?.message || error.message || 'Lỗi không xác định'
      setSaveStatus('Lưu thất bại')
      alert(`Lưu thất bại: ${errorMsg}`)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="text-gray-600">Đang tải...</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <nav className="bg-white shadow-lg">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex justify-between items-center py-4">
            <div className="flex items-center space-x-6">
              <h1 className="text-2xl font-bold text-blue-600">DocShare</h1>
              <Link to="/dashboard" className="text-gray-600 hover:text-blue-600">
                Trang chủ
              </Link>
              <Link to="/documents" className="text-gray-600 hover:text-blue-600">
                Tài liệu
              </Link>
            </div>
            <div className="flex items-center space-x-4">
              {/* Online Users */}
              {onlineUsers.length > 0 && (
                <div className="flex items-center space-x-2">
                  <span className="text-sm text-gray-600">👥 Online:</span>
                  <div className="flex -space-x-2">
                    {onlineUsers.map(user => (
                      <div
                        key={user.userId}
                        className="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold border-2 border-white"
                        style={{ backgroundColor: user.color }}
                        title={user.username}
                      >
                        {user.username.charAt(0).toUpperCase()}
                      </div>
                    ))}
                  </div>
                </div>
              )}
              
              <span className="text-sm text-gray-500">{saveStatus}</span>
              <button 
                onClick={toggleLanguage} 
                className="bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-2 px-4 rounded"
              >
                {language === 'en' ? '🇻🇳 VI' : '🇬🇧 EN'}
              </button>
              <button 
                onClick={handleSave} 
                className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:opacity-50"
                disabled={saving || isReadOnly}
              >
                Lưu
              </button>
              <button 
                onClick={logout} 
                className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
              >
                Đăng xuất
              </button>
            </div>
          </div>
        </div>
      </nav>

      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="bg-white rounded-lg shadow-md p-8">
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Tài liệu chưa đặt tên"
            className="text-3xl font-bold w-full mb-6 border-none focus:outline-none focus:ring-0"
            disabled={isReadOnly}
          />

          <div ref={editorRef} id="editor-container" style={{ height: '500px' }}></div>

          <div className="mt-6 flex items-center justify-between">
            <label className="flex items-center space-x-2">
              <input 
                type="checkbox" 
                checked={isPublic}
                onChange={(e) => setIsPublic(e.target.checked)}
                disabled={isReadOnly}
                className="rounded"
              />
              <span className="text-sm text-gray-700">Công khai tài liệu này</span>
            </label>
            <div className="text-sm text-gray-500">
              <span>{wordCount} từ</span> | <span>{charCount} ký tự</span>
            </div>
          </div>

          {isReadOnly && (
            <div className="mt-4 bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded">
              Chỉ đọc
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default DocumentEditor
