// Real-time Collaboration with WebSocket
class CollaborationManager {
    constructor(documentId, userId, username) {
        this.documentId = documentId;
        this.userId = userId;
        this.username = username;
        this.stompClient = null;
        this.connected = false;
        this.userColors = new Map();
        this.activeCursors = new Map();
        this.colorPalette = [
            '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', 
            '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E2'
        ];
        this.myColor = this.getRandomColor();
    }

    getRandomColor() {
        return this.colorPalette[Math.floor(Math.random() * this.colorPalette.length)];
    }

    getUserColor(userId) {
        if (!this.userColors.has(userId)) {
            this.userColors.set(userId, this.getRandomColor());
        }
        return this.userColors.get(userId);
    }

    connect() {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        // Disable debug logging
        this.stompClient.debug = null;

        this.stompClient.connect({}, (frame) => {
            console.log('Connected to WebSocket:', frame);
            this.connected = true;
            this.onConnected();
        }, (error) => {
            console.error('WebSocket connection error:', error);
            this.connected = false;
            // Retry connection after 5 seconds
            setTimeout(() => this.connect(), 5000);
        });
    }

    onConnected() {
        // Subscribe to document presence updates
        this.stompClient.subscribe(`/topic/document/${this.documentId}/presence`, (message) => {
            const presence = JSON.parse(message.body);
            this.handlePresence(presence);
        });

        // Subscribe to active users list
        this.stompClient.subscribe(`/topic/document/${this.documentId}/users`, (message) => {
            const users = JSON.parse(message.body);
            this.updateActiveUsers(users);
        });

        // Subscribe to cursor movements
        this.stompClient.subscribe(`/topic/document/${this.documentId}/cursor`, (message) => {
            const cursor = JSON.parse(message.body);
            console.log('📥 Received cursor update:', {
                username: cursor.username,
                index: cursor.index,
                color: cursor.color,
                isMyself: cursor.userId === this.userId
            });
            if (cursor.userId !== this.userId) {
                this.showRemoteCursor(cursor);
            } else {
                console.log('⏭️ Skipping own cursor');
            }
        });

        // Subscribe to document changes
        this.stompClient.subscribe(`/topic/document/${this.documentId}/changes`, (message) => {
            const change = JSON.parse(message.body);
            if (change.userId !== this.userId) {
                this.applyRemoteChange(change);
            }
        });

        // Announce presence
        this.sendPresence('JOIN');
        
        // Send cursor position periodically (every 2 seconds)
        this.cursorInterval = setInterval(() => {
            if (window.quill) {
                const selection = window.quill.getSelection();
                if (selection) {
                    this.sendCursor(selection.index, selection.length);
                }
            }
        }, 2000);
    }

    sendPresence(action) {
        if (this.connected && this.stompClient) {
            const presence = {
                userId: this.userId,
                username: this.username,
                documentId: this.documentId,
                action: action,
                color: this.myColor
            };
            
            this.stompClient.send(
                `/app/document/${this.documentId}/${action.toLowerCase()}`,
                {},
                JSON.stringify(presence)
            );
        }
    }

    sendCursor(index, length) {
        if (this.connected && this.stompClient) {
            const cursor = {
                userId: this.userId,
                username: this.username,
                documentId: this.documentId,
                index: index,
                length: length || 0,
                color: this.myColor
            };
            
            console.log('📤 Sending cursor position:', {
                username: cursor.username,
                index: cursor.index,
                color: cursor.color
            });
            
            this.stompClient.send(
                `/app/document/${this.documentId}/cursor`,
                {},
                JSON.stringify(cursor)
            );
        } else {
            console.log('❌ Cannot send cursor - not connected');
        }
    }

    sendChange(delta) {
        if (this.connected && this.stompClient) {
            const change = {
                userId: this.userId,
                username: this.username,
                documentId: this.documentId,
                delta: JSON.stringify(delta)
            };
            
            this.stompClient.send(
                `/app/document/${this.documentId}/change`,
                {},
                JSON.stringify(change)
            );
        }
    }

    handlePresence(presence) {
        const action = presence.action;
        const username = presence.username;
        
        // Get translation safely (check if t function exists)
        const getTranslation = (key) => {
            if (typeof t === 'function') {
                return t(key);
            }
            // Fallback translations
            const lang = localStorage.getItem('language') || 'en';
            const fallback = {
                'collab.joined': lang === 'vi' ? 'đã tham gia tài liệu' : 'joined the document',
                'collab.left': lang === 'vi' ? 'đã rời khỏi tài liệu' : 'left the document'
            };
            return fallback[key] || key;
        };
        
        if (action === 'JOIN') {
            this.showNotification(`${username} ${getTranslation('collab.joined')}`, 'info');
        } else if (action === 'LEAVE') {
            this.showNotification(`${username} ${getTranslation('collab.left')}`, 'info');
            this.removeCursor(presence.userId);
        }
    }

    updateActiveUsers(users) {
        const container = document.getElementById('active-users');
        if (!container) return;

        container.innerHTML = users.map(user => `
            <div class="flex items-center space-x-2 px-3 py-1.5 bg-gray-50 rounded-full border border-gray-200" 
                 title="${user.username}"
                 style="border-left: 3px solid ${user.color}">
                <div class="w-2.5 h-2.5 rounded-full animate-pulse" style="background-color: ${user.color}"></div>
                <span class="text-sm font-medium text-gray-700">${user.username}</span>
            </div>
        `).join('');
    }

    showRemoteCursor(cursor) {
        if (!window.quill) {
            console.log('❌ Quill not ready');
            return;
        }
        
        console.log('📍 Showing remote cursor:', cursor);
        
        const cursorId = `cursor-${cursor.userId}`;
        let cursorElement = document.getElementById(cursorId);

        if (!cursorElement) {
            console.log('✨ Creating new cursor element for user:', cursor.username);
            cursorElement = document.createElement('div');
            cursorElement.id = cursorId;
            cursorElement.className = 'remote-cursor';
            
            // Create cursor structure
            const caretDiv = document.createElement('div');
            caretDiv.className = 'cursor-caret';
            caretDiv.style.cssText = `
                width: 3px;
                height: 22px;
                background-color: ${cursor.color};
                box-shadow: 0 0 8px ${cursor.color};
                position: relative;
            `;
            
            const flagDiv = document.createElement('div');
            flagDiv.className = 'cursor-flag';
            flagDiv.textContent = cursor.username;
            flagDiv.style.cssText = `
                position: absolute;
                top: -28px;
                left: -4px;
                padding: 4px 10px;
                border-radius: 4px;
                font-size: 12px;
                color: white;
                background-color: ${cursor.color};
                white-space: nowrap;
                font-weight: 600;
                box-shadow: 0 2px 6px rgba(0,0,0,0.3);
                opacity: 1;
                transition: opacity 0.2s;
                pointer-events: none;
            `;
            
            cursorElement.appendChild(caretDiv);
            cursorElement.appendChild(flagDiv);
            
            // Add hover effect to show/hide flag
            cursorElement.addEventListener('mouseenter', () => {
                flagDiv.style.opacity = '1';
            });
            cursorElement.addEventListener('mouseleave', () => {
                flagDiv.style.opacity = '0';
            });
            
            // Append to editor
            const editor = document.querySelector('.ql-editor');
            if (editor) {
                editor.appendChild(cursorElement);
                console.log('✅ Cursor element created and appended to editor');
            } else {
                console.error('❌ .ql-editor not found!');
                return;
            }
            
            // Hide flag after 3 seconds
            setTimeout(() => {
                flagDiv.style.opacity = '0';
            }, 3000);
        }

        // Position cursor using Quill's getBounds
        try {
            const bounds = window.quill.getBounds(cursor.index);
            console.log('📐 Cursor bounds:', bounds);
            
            if (bounds && bounds.left !== undefined && bounds.top !== undefined) {
                // Get editor's scroll position
                const editor = document.querySelector('.ql-editor');
                const editorRect = editor.getBoundingClientRect();
                
                // Position relative to editor
                cursorElement.style.position = 'absolute';
                cursorElement.style.left = `${bounds.left}px`;
                cursorElement.style.top = `${bounds.top}px`;
                cursorElement.style.zIndex = '9999';
                cursorElement.style.pointerEvents = 'auto';
                cursorElement.style.display = 'block';
                cursorElement.style.visibility = 'visible';
                
                console.log('✅ Cursor positioned at:', {
                    left: bounds.left,
                    top: bounds.top,
                    editorRect: editorRect
                });
            } else {
                console.log('⚠️ Bounds is null/undefined or invalid');
                // Try positioning at start if bounds fail
                cursorElement.style.left = '0px';
                cursorElement.style.top = '0px';
                cursorElement.style.display = 'block';
            }
        } catch (e) {
            console.error('❌ Error positioning cursor:', e);
        }

        // Store cursor data
        this.activeCursors.set(cursor.userId, cursor);
        
        // Auto-remove cursor after 10 seconds of inactivity
        clearTimeout(this.activeCursors.get(`${cursor.userId}-timeout`));
        this.activeCursors.set(`${cursor.userId}-timeout`, setTimeout(() => {
            console.log('⏰ Removing inactive cursor for:', cursor.username);
            this.removeCursor(cursor.userId);
        }, 10000));
    }

    removeCursor(userId) {
        const cursorElement = document.getElementById(`cursor-${userId}`);
        if (cursorElement) {
            cursorElement.remove();
        }
        this.activeCursors.delete(userId);
    }

    applyRemoteChange(change) {
        if (window.quill && change.delta) {
            try {
                const delta = JSON.parse(change.delta);
                // Disable local change tracking temporarily
                window.isRemoteChange = true;
                window.quill.updateContents(delta, 'api');
                window.isRemoteChange = false;
            } catch (e) {
                console.error('Error applying remote change:', e);
                window.isRemoteChange = false;
            }
        }
    }

    showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `fixed top-20 right-4 px-4 py-2 rounded shadow-lg text-white ${
            type === 'info' ? 'bg-blue-500' : 'bg-green-500'
        } animate-fade-in`;
        notification.textContent = message;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.classList.add('animate-fade-out');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }

    disconnect() {
        if (this.cursorInterval) {
            clearInterval(this.cursorInterval);
        }
        if (this.connected && this.stompClient) {
            this.sendPresence('LEAVE');
            this.stompClient.disconnect();
            this.connected = false;
        }
    }
}

// CSS for remote cursors and animations
const style = document.createElement('style');
style.textContent = `
    .animate-fade-in {
        animation: fadeIn 0.3s ease-in;
    }
    
    .animate-fade-out {
        animation: fadeOut 0.3s ease-out;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }
    
    @keyframes fadeOut {
        from { opacity: 1; transform: translateY(0); }
        to { opacity: 0; transform: translateY(-10px); }
    }
`;
document.head.appendChild(style);
