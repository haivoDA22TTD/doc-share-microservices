# DocShare Frontend

React frontend cho DocShare - Real-time collaborative document editing platform.

## Quick Start

### Windows
```bash
# Double-click hoặc chạy trong terminal
start-dev.bat
```

### Linux/Mac
```bash
npm install
npm run dev
```

Frontend sẽ chạy tại: **http://localhost:4200**

## Requirements

- Node.js 18+
- npm 9+
- Backend running at http://localhost:8080

## Development

```bash
# Install dependencies
npm install

# Start dev server (with hot reload)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint
```

## Environment Variables

File `.env` đã được cấu hình:

```env
VITE_API_URL=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
VITE_GOOGLE_CLIENT_ID=968228159936-ppjgdnndoae9mll430ta3gtl0at2nn8v.apps.googleusercontent.com
```

## Project Structure

```
src/
├── components/          # Reusable components
│   ├── PrivateRoute.jsx
│   └── OAuth2RedirectHandler.jsx
├── contexts/            # React contexts
│   └── AuthContext.jsx
├── pages/               # Page components
│   ├── Login.jsx
│   ├── Register.jsx
│   ├── Dashboard.jsx
│   └── DocumentEditor.jsx
├── services/            # API services
│   └── api.js
├── styles/              # SCSS styles
│   └── main.scss
├── App.jsx
└── main.jsx
```

## Features

- ⚛️ React 18 với Vite
- 🎨 SCSS styling system
- 🔐 Google OAuth2 login
- 🔑 JWT authentication
- 📝 Document CRUD operations
- 🛡️ Protected routes
- 🚀 Hot reload development
- 📱 Responsive design

## Available Routes

- `/login` - Login page (Google OAuth + username/password)
- `/register` - Registration page
- `/dashboard` - Document dashboard (protected)
- `/document/:id` - Document editor (protected)
- `/oauth2/redirect` - OAuth callback handler

## API Integration

All API calls through `src/services/api.js`:
- Automatic JWT token injection
- Request/response interceptors
- Error handling
- Base URL from environment

## Authentication Flow

### Username/Password Login
1. User enters credentials
2. POST `/api/auth/login`
3. Receive JWT token
4. Save to localStorage
5. Redirect to dashboard

### Google OAuth Login
1. User clicks "Continue with Google"
2. Redirect to `http://localhost:8080/oauth2/authorization/google`
3. Google OAuth consent screen
4. Backend processes OAuth callback
5. Backend redirects to `http://localhost:4200/oauth2/redirect?token=<JWT>`
6. Frontend saves token and redirects to dashboard

## Styling

SCSS structure in `src/styles/main.scss`:
- CSS variables for theming
- Responsive breakpoints
- Component styles
- Utility classes

## Troubleshooting

### Cannot connect to backend
```bash
# Check backend health
curl http://localhost:8080/actuator/health

# Restart backend
docker-compose restart app
```

### Google OAuth not working
- Verify `VITE_GOOGLE_CLIENT_ID` in `.env`
- Check Google Console redirect URI: `http://localhost:8080/login/oauth2/code/google`
- Check backend logs: `docker logs docshare-app -f`

### CORS errors
- Backend must allow `http://localhost:4200`
- Check `SecurityConfig.java` CORS configuration

### npm install fails
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

## Documentation

Xem thêm trong thư mục gốc:
- `QUICK_START_FRONTEND.md` - Hướng dẫn nhanh
- `GOOGLE_OAUTH_SETUP.md` - Setup Google OAuth
- `FRONTEND_IMPLEMENTATION.md` - Chi tiết implementation

## Tech Stack

- React 18.2
- React Router 6.22
- Axios 1.6
- SCSS/Sass 1.70
- Vite 5.1
- @react-oauth/google 0.12

## Next Steps

- [ ] WebSocket integration cho real-time collaboration
- [ ] Quill.js rich text editor
- [ ] User presence indicators
- [ ] Document sharing với permissions
- [ ] Comments & mentions
- [ ] Version history

## License

MIT
