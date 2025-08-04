# NumberGuessing-BE

##Ứng dụng backend cho trò chơi đoán số với Spring Boot, PostgreSQL, Redis và JWT Authentication.

## Tôi đã cài đặt giao diện cho game, vui lòng clone NumberGuessing-FE repo và làm theo hướng dãn trong file readme.md


## 📋 Mục lục

- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt môi trường](#cài-đặt-môi-trường)
- [Chạy ứng dụng](#chạy-ứng-dụng)
- [API Documentation](#api-documentation)
- [Authentication](#authentication)
- [Test API](#test-api)
- [Cấu trúc project](#cấu-trúc-project)

## 🖥️ Yêu cầu hệ thống

- Docker và Docker Compose
- Java 17 hoặc cao hơn
- Gradle 7.6.1

## 🚀 Cài đặt môi trường

### 1. Clone repository

```bash
git clone <repository-url>
cd NumberGuessing-BE
```

### 2. Kiểm tra Docker

```bash
docker network create guessing-game-network
## Nếu Đã tạo ra network thì không tạo lại 
docker-compose up -d
```

Lệnh này sẽ khởi động:
- PostgreSQL database (port 5432)
- Redis cache (port 6379)
- PgAdmin (port 5050)
- Redis Commander (port 8081)
- Spring Boot API (port 9002)


## 📚 API Documentation

### Swagger UI

Sau khi khởi động ứng dụng, truy cập Swagger UI tại:

```
http://localhost:9002/swagger-ui.html
```

### API Endpoints

#### Authentication
- `POST /api/auth/register` - Đăng ký tài khoản mới
- `POST /api/auth/login` - Đăng nhập

#### Game
- `POST /api/game/guess` - Đoán số
- `POST /api/game/buy-turns` - Mua thêm lượt chơi
- `POST /api/game/reset` - Reset lượt chơi

#### Player
- `GET /api/players/leaderboard` - Xem bảng xếp hạng
- `GET /api/players/me` - Xem thông tin profile
- `POST /api/players/refresh-profile` - Refresh cache profile

## 🔐 Authentication

### 1. Đăng ký tài khoản

```bash
curl -X POST http://localhost:9002/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com"
  }'
```

Response:
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "testuser"
  }
}
```

### 2. Đăng nhập

```bash
curl -X POST http://localhost:9002/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

### 3. Sử dụng token

Sau khi đăng nhập/đăng ký, sử dụng token trong header `Authorization`:

```bash
curl -X GET http://localhost:9002/api/players/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## 🧪 Test API

### 1. Test nhanh với Swagger

1. Mở trình duyệt và truy cập: `http://localhost:9002/swagger-ui.html`
2. Chọn endpoint muốn test
3. Click "Try it out"
4. Nhập thông tin và click "Execute"

### 2. Test với cURL

#### Đăng ký và đăng nhập:
```bash
# Đăng ký
curl -X POST http://localhost:9002/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "player1", "password": "123456", "email": "player1@test.com"}'

# Đăng nhập
curl -X POST http://localhost:9002/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "player1", "password": "123456"}'
```

#### Chơi game (sử dụng token từ đăng nhập):
```bash
# Đoán số
curl -X POST http://localhost:9002/api/game/guess \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"guessNumber": 50}'

# Xem profile
curl -X GET http://localhost:9002/api/players/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Xem leaderboard
curl -X GET http://localhost:9002/api/players/leaderboard
```

### 3. Test với Postman

1. Import collection từ Swagger UI
2. Set up environment variables cho token
3. Test các endpoints

## 🗄️ Database Management

### PgAdmin (PostgreSQL GUI)

Truy cập PgAdmin tại: `http://localhost:5050`

- **Email:** admin@admin.com
- **Password:** admin

### Redis Commander

Truy cập Redis Commander tại: `http://localhost:8081`

## 📁 Cấu trúc project

```
NumberGuessing-BE/
├── src/main/java/com/example/Immobi/
│   ├── Controller/          # REST Controllers
│   ├── Service/            # Business Logic
│   ├── Repository/         # Data Access Layer
│   ├── Entity/             # JPA Entities
│   ├── Dto/                # Data Transfer Objects
│   ├── Core/               # Core configurations
│   └── ImmobiApplication.java
├── src/main/resources/
│   ├── application.properties
│   └── static/
├── docker-compose.yml      # Docker services
├── Dockerfile             # API container
└── build.gradle           # Dependencies
```

## 📝 Notes

- JWT token có thời hạn 24 giờ
- Redis được sử dụng để cache leaderboard và player profiles
- Database sẽ tự động tạo tables khi khởi động lần đầu
- Tất cả API endpoints (trừ auth) yêu cầu JWT token

## 🤝 Contributing

1. Fork project
2. Tạo feature branch
3. Commit changes
4. Push to branch
5. Tạo Pull Request
