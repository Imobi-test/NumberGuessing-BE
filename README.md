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

### Cách 1: Sử dụng Gradle trực tiếp

```bash
# Build project
./gradlew clean build

# Chạy ứng dụng
./gradlew bootRun
```

### Cách 2: Sử dụng IDE

Mở project trong IDE hỗ trợ Gradle (IntelliJ IDEA, Eclipse) và chạy main class `com.example.Immobi.ImmobiApplication`

API sẽ khởi động tại: http://localhost:9002

### Cách 3: Chạy backend bằng Docker

#### Tạo Dockerfile

Tạo file `Dockerfile` trong thư mục gốc của project với nội dung sau:

```Dockerfile
FROM gradle:7.6.1-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 9002
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Build Docker image

```bash
docker build -t guessing-game-api:latest .
```

#### Chạy Backend từ Docker image

```bash
docker run -d --name guessing-game-api -p 9002:9002 \
  --network guessing-game-network \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/guessing_game \
  -e SPRING_REDIS_HOST=redis \
  guessing-game-api:latest
```

#### Chạy toàn bộ ứng dụng với Docker Compose

Cập nhật file `docker-compose.yml` để bao gồm cả backend:

```yaml
version: '3'

services:
  postgres:
    image: postgres:14-alpine
    container_name: guessing-game-db
    environment:
      POSTGRES_DB: guessing_game
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - guessing-game-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  pgadmin:
    image: dpage/pgadmin4:7.8
    container_name: guessing-game-pgadmin
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@admin.com
      PGADMIN_DEFAULT_PASSWORD: admin
    ports:
      - "5050:80"
    depends_on:
      - postgres
    volumes:
      - pgadmin_data:/var/lib/pgadmin
    networks:
      - guessing-game-network

  redis:
    image: redis:7-alpine
    container_name: guessing-game-redis
    command: redis-server --appendonly yes
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - guessing-game-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: guessing-game-redis-commander
    environment:
      - REDIS_HOSTS=local:redis:6379
    ports:
      - "8081:8081"
    depends_on:
      - redis
    networks:
      - guessing-game-network

  api:
    build: .
    container_name: guessing-game-api
    ports:
      - "9002:9002"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/guessing_game
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - guessing-game-network
    restart: unless-stopped

networks:
  guessing-game-network:
    driver: bridge

volumes:
  postgres_data:
  pgadmin_data:
  redis_data:
```

Chạy toàn bộ hệ thống bằng một lệnh:

```bash
docker-compose up -d
```

Để chỉ build lại và khởi động backend:

```bash
docker-compose up -d --build api
```

## Kiểm tra API

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
