# Number Guessing Game API

REST API cho trò chơi đoán số với bảng xếp hạng thời gian thực.

## Yêu cầu hệ thống

- JDK 17+
- Gradle 7.6+
- Docker & Docker Compose
- PostgreSQL (hoặc sử dụng Docker)
- Redis (hoặc sử dụng Docker)

## Cài đặt môi trường

### 1. Clone repository

```bash
git clone <repository-url>
cd NumberGuessing-BE
```

### 2. Khởi động các dịch vụ với Docker Compose

Dự án đã được cấu hình với Docker Compose để chạy các dịch vụ cần thiết:

```bash
docker-compose up -d
```

Lệnh này sẽ khởi động:
- PostgreSQL (cổng 5432)
- pgAdmin (cổng 5050) - Quản lý PostgreSQL qua giao diện web
- Redis (cổng 6379) - Cho bảng xếp hạng và caching
- Redis Commander (cổng 8081) - Quản lý Redis qua giao diện web

### 3. Kiểm tra các dịch vụ đã khởi động

Truy cập các dịch vụ quản lý:
- pgAdmin: http://localhost:5050 (Email: admin@admin.com, Password: admin)
- Redis Commander: http://localhost:8081

## Build và chạy project

Cập nhật file `docker-compose.yml` để bao gồm cả backend:

```bash
docker-compose up -d
```

### Swagger UI

Dự án tích hợp Swagger UI để dễ dàng kiểm tra API:

```
http://localhost:9002/swagger-ui.html
```

### Xác thực (Authentication)

API sử dụng JWT (JSON Web Token) để xác thực. Bạn cần lấy token trước khi gọi các API khác.

#### 1. Đăng ký tài khoản mới

```bash
curl -X POST http://localhost:9002/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","username":"testuser"}'
```

#### 2. Đăng nhập và lấy token

```bash
curl -X POST http://localhost:9002/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'
```

Response:
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "email": "user@example.com",
    "username": "testuser"
  },
  "message": "Login successful"
}
```

Sử dụng token nhận được cho các request tiếp theo:

```bash
curl -X GET http://localhost:9002/api/players/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

### Test các API chính

#### 1. Lấy thông tin người chơi hiện tại

```bash
curl -X GET http://localhost:9002/api/players/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 2. Đoán số

```bash
curl -X POST http://localhost:9002/api/game/guess \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"number": 3}'
```

#### 3. Mua thêm lượt chơi

```bash
curl -X POST http://localhost:9002/api/game/buy-turns \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 4. Xem bảng xếp hạng

```bash
curl -X GET http://localhost:9002/api/players/leaderboard \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Cấu hình

Các cấu hình chính của ứng dụng nằm trong file `src/main/resources/application.properties`.

### Database

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/guessing_game
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Redis

```properties
spring.redis.host=localhost
spring.redis.port=6379
```

### Server Port

```properties
server.port=9002
```

## Xử lý sự cố

### Kết nối database thất bại

Kiểm tra PostgreSQL đã chạy và có thể kết nối:

```bash
docker ps | grep postgres
docker logs guessing-game-db
```

### Redis không khả dụng

Kiểm tra Redis đã chạy và có thể kết nối:

```bash
docker ps | grep redis
docker logs guessing-game-redis
```

### Backend không khởi động

Kiểm tra logs của container:

```bash
docker logs guessing-game-api
```
