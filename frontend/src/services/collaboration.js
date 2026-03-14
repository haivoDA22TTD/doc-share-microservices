import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

class CollaborationService {
  constructor() {
    this.client = null
    this.connected = false
    this.documentId = null
    this.subscriptions = []
  }

  connect(documentId, callbacks = {}) {
    if (this.connected && this.documentId === documentId) {
      console.log('Already connected to document:', documentId)
      return
    }

    this.disconnect()
    this.documentId = documentId

    console.log('Connecting to WebSocket for document:', documentId)

    // Create SockJS connection through API Gateway
    const socket = new SockJS('http://localhost:8080/ws')
    
    this.client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => {
        console.log('STOMP:', str)
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    })

    this.client.onConnect = async () => {
      console.log('✅ WebSocket connected for document:', documentId)
      this.connected = true

      console.log('📡 Setting up subscriptions...')

      // Subscribe to document changes
      const changeSub = this.client.subscribe(
        `/topic/document/${documentId}/changes`,
        (message) => {
          const change = JSON.parse(message.body)
          console.log('Received change:', change)
          if (callbacks.onDocumentChange) {
            callbacks.onDocumentChange(change)
          }
        }
      )
      this.subscriptions.push(changeSub)
      console.log('✅ Subscribed to /topic/document/' + documentId + '/changes')

      // Subscribe to cursor positions
      const cursorSub = this.client.subscribe(
        `/topic/document/${documentId}/cursors`,
        (message) => {
          const cursor = JSON.parse(message.body)
          console.log('📥 Received cursor update:', cursor)
          if (callbacks.onCursorMove) {
            callbacks.onCursorMove(cursor)
          } else {
            console.warn('⚠️ onCursorMove callback not defined!')
          }
        }
      )
      this.subscriptions.push(cursorSub)
      console.log('✅ Subscribed to /topic/document/' + documentId + '/cursors')

      // Subscribe to user presence
      const presenceSub = this.client.subscribe(
        `/topic/document/${documentId}/presence`,
        (message) => {
          const presence = JSON.parse(message.body)
          console.log('User presence:', presence)
          if (callbacks.onUserPresence) {
            callbacks.onUserPresence(presence)
          }
        }
      )
      this.subscriptions.push(presenceSub)
      console.log('✅ Subscribed to /topic/document/' + documentId + '/presence')

      // Subscribe to active users list
      const usersSub = this.client.subscribe(
        `/topic/document/${documentId}/users`,
        (message) => {
          const users = JSON.parse(message.body)
          console.log('Active users list:', users)
          if (callbacks.onUsersList) {
            callbacks.onUsersList(users)
          }
        }
      )
      this.subscriptions.push(usersSub)
      console.log('✅ Subscribed to /topic/document/' + documentId + '/users')

      // Get user info from API
      try {
        const token = localStorage.getItem('token')
        const response = await fetch('/api/users/me', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        if (response.ok) {
          const userInfo = await response.json()
          console.log('✅ Got user info from API:', userInfo)
          
          // Save to localStorage for future use
          localStorage.setItem('user', JSON.stringify({
            userId: userInfo.id,
            username: userInfo.username,
            email: userInfo.email
          }))
          
          // Send join with real user info
          this.sendJoin(userInfo.username, userInfo.id)
        } else {
          console.error('Failed to get user info, using fallback')
          this.sendJoin('Anonymous', null)
        }
      } catch (error) {
        console.error('Error getting user info:', error)
        this.sendJoin('Anonymous', null)
      }

      if (callbacks.onConnect) {
        callbacks.onConnect()
      }
    }

    this.client.onStompError = (frame) => {
      console.error('❌ STOMP error:', frame)
      this.connected = false
      if (callbacks.onError) {
        callbacks.onError(frame)
      }
    }

    this.client.onWebSocketClose = () => {
      console.log('WebSocket closed')
      this.connected = false
      if (callbacks.onDisconnect) {
        callbacks.onDisconnect()
      }
    }

    this.client.activate()
  }

  disconnect() {
    if (this.client && this.connected) {
      console.log('Disconnecting WebSocket...')
      
      // Send leave message
      const userStr = localStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          this.sendLeave(user.username, user.userId)
        } catch (e) {
          console.error('Error parsing user:', e)
        }
      }

      // Unsubscribe all
      this.subscriptions.forEach(sub => sub.unsubscribe())
      this.subscriptions = []

      this.client.deactivate()
      this.connected = false
      this.documentId = null
    }
  }

  getUserInfo() {
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        return JSON.parse(userStr)
      } catch (e) {
        console.error('Error parsing user:', e)
      }
    }
    return { userId: null, username: 'Anonymous' }
  }

  sendJoin(username, userId) {
    if (!this.connected || !this.client) {
      console.warn('Cannot send join: not connected')
      return
    }

    console.log('📤 Sending JOIN message:', { username, userId, documentId: this.documentId })

    this.client.publish({
      destination: `/app/document/${this.documentId}/join`,
      body: JSON.stringify({
        userId: userId || null,
        username: username || 'Anonymous',
        action: 'JOIN'
      })
    })
  }

  sendLeave(username, userId) {
    if (!this.connected || !this.client) {
      console.warn('Cannot send leave: not connected')
      return
    }

    console.log('📤 Sending LEAVE message:', { username, userId, documentId: this.documentId })

    this.client.publish({
      destination: `/app/document/${this.documentId}/leave`,
      body: JSON.stringify({
        userId: userId || null,
        username: username || 'Anonymous',
        action: 'LEAVE'
      })
    })
  }

  sendDocumentChange(username, userId, content) {
    if (!this.connected || !this.client) {
      console.warn('Cannot send change: not connected')
      return
    }

    console.log('Sending document change:', { username, userId, contentLength: content?.length })

    this.client.publish({
      destination: `/app/document/${this.documentId}/change`,
      body: JSON.stringify({
        userId,
        username,
        content,
        changeType: 'CONTENT_UPDATE'
      })
    })
  }

  sendCursorPosition(username, userId, index) {
    if (!this.connected || !this.client) {
      console.warn('⚠️ Cannot send cursor: not connected')
      return
    }

    console.log('📤 Publishing cursor position:', { username, userId, index, documentId: this.documentId })

    this.client.publish({
      destination: `/app/document/${this.documentId}/cursor`,
      body: JSON.stringify({
        userId,
        username,
        index
      })
    })
  }
}

export default new CollaborationService()
