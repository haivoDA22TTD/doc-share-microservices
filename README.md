# 📝 DocShare - Hệ thống Soạn thảo Tài liệu Cộng tác Thời gian Thực

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=for-the-badge&logo=spring)
![React](https://img.shields.io/badge/React-18.2.0-blue?style=for-the-badge&logo=react)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)
![Microservices](https://img.shields.io/badge/Kiến%20trúc-Microservices-blueviolet?style=for-the-badge)

**Nền tảng cộng tác tài liệu hiện đại, có khả năng mở rộng cao với kiến trúc microservices**

[Tính năng](#-tính-năng) • [Kiến trúc](#-kiến-trúc) • [Bắt đầu nhanh](#-bắt-đầu-nhanh) • [Tài liệu](#-tài-liệu)

</div>

---

## 🌟 Tính năng

### 📄 Quản lý Tài liệu
- ✅ Tạo, Đọc, Cập nhật, Xóa tài liệu
- ✅ Soạn thảo văn bản phong phú với Quill.js
- ✅ Chia sẻ tài liệu với phân quyền (Xem/Chỉnh sửa)
- ✅ Tài liệu công khai/riêng tư
- ✅ Tự động lưu theo thời gian thực

### 👥 Cộng tác Thời gian Thực
- ✅ Nhiều người dùng chỉnh sửa đồng thời
- ✅ Theo dõi con trỏ trực tiếp với tên người dùng
- ✅ Đồng bộ nội dung tức thì
- ✅ Hiển thị người dùng đang online
- ✅ Giao tiếp dựa trên WebSocket

### 🔐 Xác thực & Phân quyền
- ✅ Xác thực bằng Username/Password
- ✅ Tích hợp Google OAuth2
- ✅ Bảo mật dựa trên JWT token
- ✅ Blacklist token khi đăng xuất
- ✅ Kiểm soát truy cập dựa trên vai trò

### 🌐 Đa ngôn ngữ
- ✅ Hỗ trợ đa ngôn ngữ (Tiếng Anh/Tiếng Việt)
- ✅ Chuyển đổi ngôn ngữ động
- ✅ Các thành phần UI được bản địa hóa

### 📊 Giám sát & Quan sát
- ✅ Thu thập metrics với Prometheus
- ✅ Dashboard Grafana
- ✅ Kiểm tra sức khỏe dịch vụ
- ✅ Giám sát hiệu suất thời gian thực
- ✅ Metrics nghiệp vụ tùy chỉnh

---

## 🏗️ Kiến trúc

### Kiến trúc Microservices

```
┌─────────────────────────────────────────────────────────────────┐
│                     Frontend (React)                             │
│                        Cổng: 4200                                │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   API Gateway (Spring Cloud)                     │
│                        Cổng: 8080                                │
│  • Định tuyến Request   • Cân bằng tải                           │
│  • Xử lý CORS           • Hỗ trợ WebSocket                       │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              Eureka Server (Service Discovery)                   │
│                        Cổng: 8761                                │
│  • Đăng ký dịch vụ      • Giám sát sức khỏe                     │
│  • Khám phá động        • Cân bằng tải                           │
└────────────────────────────┬────────────────────────────────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                             ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│    Auth Service          │  │   Document Service       │
│      Cổng: 8081          │  │      Cổng: 8082          │
│                          │  │                          │
│ • Xác thực người dùng    │  │ • CRUD Tài liệu          │
│ • Tích hợp OAuth2        │  │ • Phân quyền             │
│ • Tạo JWT                │  │ • Cộng tác thời gian thực│
│ • Quản lý người dùng     │  │ • Xử lý WebSocket        │
│ • Blacklist Token        │  │ • Thu thập Metrics       │
└──────────────────────────┘  └──────────────────────────┘
              │                             │
              └──────────────┬──────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Hạ tầng Chia sẻ                               │
│                                                                  │
│  PostgreSQL (5432)  │  Redis (6379)  │  RabbitMQ (5672/15672)  │
│  Prometheus (9090)  │  Grafana (3000) │  Redis Insight (5540)  │
└─────────────────────────────────────────────────────────────────┘
```

### Công nghệ Sử dụng

#### Backend
- **Framework**: Spring Boot 3.2.0
- **Ngôn ngữ**: Java 21
- **Bảo mật**: Spring Security + JWT
- **Cơ sở dữ liệu**: PostgreSQL 15
- **Cache**: Redis 7
- **Message Queue**: RabbitMQ 3.13
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Giám sát**: Prometheus + Grafana
- **WebSocket**: STOMP over SockJS

#### Frontend
- **Framework**: React 18.2.0
- **Build Tool**: Vite
- **Styling**: SCSS
- **Trình soạn thảo**: Quill.js
- **HTTP Client**: Axios
- **WebSocket**: SockJS + STOMP

#### DevOps
- **Container hóa**: Docker + Docker Compose
- **Reverse Proxy**: Nginx
- **CI/CD**: Multi-stage Docker builds

---

## 🚀 Bắt đầu nhanh

### Yêu cầu hệ thống

- 🐳 Docker & Docker Compose
- 🌐 Google OAuth2 Credentials (tùy chọn)
- 💻 RAM tối thiểu 8GB
- 📦 Dung lượng trống 20GB

### Cài đặt

#### 1️⃣ Clone Repository

```bash
git clone https://github.com/yourusername/docshare.git
cd docshare
```

#### 2️⃣ Cấu hình môi trường

Tạo hoặc cập nhật file `.env`:

```env
# Cấu hình Google OAuth2
GOOGLE_CLIENT_ID=your-client-id-here
GOOGLE_CLIENT_SECRET=your-client-secret-here
GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google
```

> 📌 **Lưu ý**: Lấy OAuth2 credentials từ [Google Cloud Console](https://console.cloud.google.com/apis/credentials)

#### 3️⃣ Cập nhật OAuth2 Redirect URI

Trong Google Cloud Console:
- Vào: **APIs & Services** → **Credentials**
- Chọn OAuth 2.0 Client ID của bạn
- Thêm Authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`

#### 4️⃣ Khởi động Services

```bash
# Build và start tất cả services
docker-compose -f docker-compose.microservices.yml up -d --build

# Xem logs
docker-compose -f docker-compose.microservices.yml logs -f
```

#### 5️⃣ Kiểm tra Deployment

Đợi 1-2 phút để tất cả services khởi động, sau đó kiểm tra:

| Dịch vụ | URL | Trạng thái mong đợi |
|---------|-----|---------------------|
| 🌐 Frontend | http://localhost:4200 | Trang đăng nhập hiển thị |
| 🚪 API Gateway | http://localhost:8080/actuator/health | `{"status":"UP"}` |
| 🔍 Eureka Dashboard | http://localhost:8761 | 2 services đã đăng ký |
| 📊 Grafana | http://localhost:3000 | Dashboard (admin/admin) |
| 📈 Prometheus | http://localhost:9090 | Metrics khả dụng |
| 🐰 RabbitMQ | http://localhost:15672 | Management UI (admin/admin) |

---

## 📖 Tài liệu

### 📚 Các hướng dẫn có sẵn

- 📘 [**Cấu hình Ports**](PORTS_CONFIGURATION.md) - Tham chiếu đầy đủ về ports
- 🚀 [**Khởi động Microservices**](START_MICROSERVICES.md) - Hướng dẫn khởi động chi tiết
- 🎯 [**Sẵn sàng chạy**](READY_TO_RUN.md) - Checklist khởi động nhanh
- 🏗️ [**Hướng dẫn Microservices**](FINAL_MICROSERVICES_GUIDE.md) - Tìm hiểu sâu về kiến trúc

### 🔌 API Endpoints

Tất cả requests đi qua API Gateway tại `http://localhost:8080`

#### Xác thực

```bash
# Đăng ký
POST /api/auth/register
Content-Type: application/json
{
  "username": "john",
  "email": "john@example.com",
  "password": "password123",
  "fullName": "John Doe"
}

# Đăng nhập
POST /api/auth/login
Content-Type: application/json
{
  "username": "john",
  "password": "password123"
}

# Đăng xuất
POST /api/auth/logout
Authorization: Bearer {token}

# Lấy thông tin người dùng hiện tại
GET /api/users/me
Authorization: Bearer {token}

# Đăng nhập Google OAuth2
GET /oauth2/authorization/google
```

#### Tài liệu

```bash
# Danh sách tài liệu
GET /api/documents
Authorization: Bearer {token}

# Lấy tài liệu
GET /api/documents/{id}
Authorization: Bearer {token}

# Tạo tài liệu
POST /api/documents
Authorization: Bearer {token}
Content-Type: application/json
{
  "title": "Tài liệu của tôi",
  "content": "<p>Xin chào</p>",
  "isPublic": false
}

# Cập nhật tài liệu
PUT /api/documents/{id}
Authorization: Bearer {token}
Content-Type: application/json
{
  "title": "Tiêu đề đã cập nhật",
  "content": "<p>Nội dung đã cập nhật</p>",
  "isPublic": true
}

# Xóa tài liệu
DELETE /api/documents/{id}
Authorization: Bearer {token}

# Chia sẻ tài liệu
POST /api/documents/{id}/share
Authorization: Bearer {token}
Content-Type: application/json
{
  "username": "jane",
  "permission": "EDIT"  // hoặc "VIEW"
}

# Lấy quyền truy cập tài liệu
GET /api/documents/{id}/permissions
Authorization: Bearer {token}
```

---

## 🔧 Cấu hình

### Cổng dịch vụ

| Dịch vụ | Cổng | Truy cập |
|---------|------|----------|
| 🌐 Frontend | 4200 | Bên ngoài |
| 🚪 API Gateway | 8080 | Bên ngoài |
| 🔐 Auth Service | 8081 | Nội bộ |
| 📄 Document Service | 8082 | Nội bộ |
| 🔍 Eureka Server | 8761 | Bên ngoài |
| 🗄️ PostgreSQL | 5432 | Nội bộ |
| 💾 Redis | 6379 | Nội bộ |
| 🔴 Redis Insight | 5540 | Bên ngoài |
| 🐰 RabbitMQ AMQP | 5672 | Nội bộ |
| 🐰 RabbitMQ Management | 15672 | Bên ngoài |
| 📊 Prometheus | 9090 | Bên ngoài |
| 📈 Grafana | 3000 | Bên ngoài |

---

## 🐛 Xử lý sự cố

### Services không khởi động

```bash
# Kiểm tra logs
docker-compose -f docker-compose.microservices.yml logs

# Restart service cụ thể
docker-compose -f docker-compose.microservices.yml restart auth-service

# Rebuild service
docker-compose -f docker-compose.microservices.yml up -d --build auth-service
```

### Services không đăng ký với Eureka

- Đợi 30-60 giây để đăng ký
- Kiểm tra Eureka dashboard: http://localhost:8761
- Xác minh kết nối mạng giữa các services
- Kiểm tra logs service để tìm lỗi kết nối Eureka

### Đăng nhập OAuth2 thất bại

- Xác minh redirect URI trong Google Cloud Console khớp với: `http://localhost:8080/login/oauth2/code/google`
- Kiểm tra file `.env` có credentials đúng
- Đảm bảo API Gateway đang chạy trên cổng 8080

---

## 📞 Hỗ trợ

Để được hỗ trợ, email support@docshare.com hoặc mở issue trên GitHub.

---

<div align="center">

**⭐ Star repository này nếu bạn thấy hữu ích!**

Được tạo với ❤️ bởi DocShare Team

</div>
