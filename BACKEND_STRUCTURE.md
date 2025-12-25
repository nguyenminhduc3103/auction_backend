# 🏗️ BidSphere Backend - Cấu Trúc Dự Án

> **Updated:** 2025-12-23  
> **Tech Stack:** Spring Boot 3.5.6 | Java 21 | MySQL/PostgreSQL | JWT | Cloudinary

---

## 📁 Cây thư mục

```
auction-system-backend/
│
├── pom.xml                           # Maven dependencies
├── mvnw, mvnw.cmd                    # Maven wrapper
│
└── src/main/java/vn/team9/auction_system/
    │
    ├── AuctionSystemApplication.java    # Entry point
    │
    ├── auction/                         # 🔨 AUCTION MODULE
    │   ├── controller/
    │   │   ├── AuctionController.java   # CRUD auction, start/close
    │   │   ├── BidController.java       # Place bid, get bids
    │   │   └── UploadController.java    # Upload images
    │   ├── model/
    │   │   ├── Auction.java
    │   │   └── Bid.java
    │   ├── repository/
    │   │   ├── AuctionRepository.java
    │   │   ├── AuctionSpecification.java
    │   │   └── BidRepository.java
    │   ├── service/
    │   │   ├── AuctionServiceImpl.java
    │   │   ├── BidServiceImpl.java
    │   │   ├── AbstractBidService.java
    │   │   ├── AutoBidServiceImpl.java  # ⭐ Auto-bid logic
    │   │   ├── IAutoBidService.java
    │   │   └── AuctionScheduler.java
    │   ├── mapper/
    │   │   └── AuctionMapper.java
    │   └── event/                       # ⭐ Event-driven
    │       ├── AuctionEventPublisher.java
    │       ├── AuctionEventListener.java
    │       └── trigger/
    │
    ├── auth/                            # 🔐 AUTH MODULE
    │   ├── controller/
    │   │   ├── AuthController.java      # Login, Register, Verify
    │   │   ├── RoleController.java
    │   │   └── PermissionController.java
    │   ├── model/
    │   │   └── Permission.java
    │   ├── repository/
    │   │   ├── PermissionRepository.java
    │   │   └── RolePermissionRepository.java
    │   ├── service/
    │   │   ├── UserAuthService.java
    │   │   ├── JwtService.java
    │   │   ├── EmailService.java
    │   │   ├── RoleService.java
    │   │   ├── PermissionService.java
    │   │   └── OAuth2Service.java
    │   ├── mapper/
    │   └── specification/
    │
    ├── user/                            # 👤 USER MODULE
    │   ├── controller/
    │   │   ├── UserController.java      # Profile, password, avatar
    │   │   └── AdminUserController.java # Admin manage users
    │   ├── model/
    │   │   ├── User.java
    │   │   └── Role.java
    │   ├── repository/
    │   │   └── UserRepository.java
    │   ├── service/
    │   │   ├── UserService.java
    │   │   ├── AdminServiceImpl.java
    │   │   └── CustomUserDetailsService.java
    │   └── mapper/
    │
    ├── product/                         # 📦 PRODUCT MODULE
    │   ├── controller/
    │   │   └── ProductController.java
    │   ├── model/
    │   │   ├── Product.java
    │   │   └── Image.java
    │   ├── repository/
    │   ├── service/
    │   └── mapper/
    │
    ├── transaction/                     # 💳 TRANSACTION MODULE
    │   ├── controller/
    │   │   ├── TransactionAfterAuctionController.java
    │   │   └── AccountTransactionController.java
    │   ├── model/
    │   │   ├── TransactionAfterAuction.java
    │   │   └── AccountTransaction.java
    │   ├── repository/
    │   ├── service/
    │   └── mapper/
    │
    ├── feedback/                        # ⭐ FEEDBACK MODULE
    │   ├── controller/
    │   │   ├── FeedbackController.java
    │   │   ├── NotificationController.java
    │   │   └── UserWarningLogController.java
    │   ├── model/
    │   │   ├── Feedback.java
    │   │   ├── Notification.java
    │   │   └── UserWarningLog.java
    │   ├── repository/
    │   ├── service/
    │   └── mapper/
    │
    ├── user_report/                     # 📋 USER REPORT MODULE
    │   ├── controller/
    │   ├── model/
    │   ├── repository/
    │   ├── service/
    │   └── mapper/
    │
    ├── config/                          # ⚙️ CONFIGURATION
    │   ├── SecurityConfig.java
    │   ├── ApplicationConfig.java
    │   ├── WebConfig.java
    │   ├── CloudinaryConfig.java
    │   ├── PasswordConfig.java
    │   ├── PublicEndpoints.java
    │   └── jwt/
    │       └── JwtAuthenticationFilter.java
    │
    └── common/                          # 🔧 COMMON/SHARED
        ├── base/
        │   ├── BaseRequest.java
        │   ├── BaseResponse.java
        │   ├── BaseService.java
        │   └── AuditableEntity.java
        ├── dto/
        │   ├── account/
        │   ├── admin/
        │   ├── auction/
        │   ├── auth/
        │   ├── common/
        │   ├── feedback/
        │   ├── image/
        │   ├── notification/
        │   ├── pagination/
        │   ├── permission/
        │   ├── product/
        │   ├── role/
        │   ├── transaction/
        │   ├── user/
        │   └── user_report/
        ├── enums/
        ├── exception/
        │   ├── ApiException.java
        │   ├── BadRequestException.java
        │   └── NotFoundException.java
        ├── handler/
        │   └── GlobalExceptionHandler.java
        └── service/
            ├── IAuctionService.java
            ├── IBidService.java
            ├── IProductService.java
            └── ...
```

---

## 🗃️ Entity Models

**User** (`user` table)
- `userId`, `fullName`, `username`, `passwordHash`, `email`
- `phone`, `gender`, `balance`, `avatarUrl`
- `status`: PENDING | ACTIVE | BANNED
- `role` (FK), `verificationToken`, `bannedUntil`

**Role** (`role` table)
- `roleId`, `roleName`, `description`, `isActive`
- `permissions` (ManyToMany → Permission)

**Permission** (`permission` table)
- `permissionId`, `permissionName`, `apiPath`, `method`, `module`
- Format authority: `METHOD:/api/path` (VD: `GET:/api/users/me`)

**Auction** (`auction` table)
- `auctionId`, `product` (FK), `startTime`, `endTime`
- `status`: OPEN | PENDING | CLOSED | CANCELLED
- `highestCurrentPrice`, `bidStepAmount`, `winner` (FK)

**Bid** (`bid` table)
- `bidId`, `auction` (FK), `bidder` (FK), `bidAmount`, `createdAt`
- `maxAutobidAmount`, `stepAutoBidAmount`, `isAuto`, `isHighest`

**Product** (`product` table)
- `productId`, `name`, `description`, `category`
- `startPrice`, `estimatePrice`, `deposit`
- `seller` (FK), `imageUrl`, `status`: AVAILABLE | AUCTIONED | SOLD

**TransactionAfterAuction** (`transactionafterauction` table)
- `transactionId`, `auction` (FK), `buyer` (FK), `seller` (FK)
- `amount`, `status`: PENDING | PAID | SHIPPED | DONE | CANCELLED

---

## 🔐 Security & Authentication

**JWT Flow:**
```
POST /api/auth/login → JWT Token → Bearer Authorization Header
```

**Role-Based Access Control:**
- Annotation: `@PreAuthorize("hasAuthority('METHOD:/api/path')")`
- User authorities loaded từ Role → Permissions

**Public Endpoints (không cần token):**
- `/api/auth/**` - Login, Register, Verify
- `GET /api/auctions/**` - Browse auctions
- `GET /api/products/**` - Browse products (trừ /seller/me)

---

## 📡 API Endpoints

**Auth** `/api/auth`
- `POST /register` - Đăng ký
- `POST /login` - Đăng nhập
- `GET /verify?token=` - Xác thực email
- `POST /resend-verification` - Gửi lại email

**Users** `/api/users`
- `GET /me` - Thông tin user hiện tại
- `PUT /me` - Cập nhật profile
- `PUT /me/avatar` - Upload avatar
- `PATCH /change-password` - Đổi mật khẩu

**Auctions** `/api/auctions`
- `GET /` - Danh sách (filter, pagination)
- `GET /{id}` - Chi tiết
- `POST /` - Tạo mới
- `PUT /{id}` - Cập nhật
- `DELETE /{id}` - Xóa
- `POST /{id}/start` - Bắt đầu
- `POST /{id}/close` - Đóng

**Bids** `/api/bids`
- `POST /` - Đặt giá
- `POST /auto` - Auto-bid
- `GET /auction/{auctionId}` - Danh sách bids

**Products** `/api/products`
- `GET /` - Danh sách
- `GET /{id}` - Chi tiết
- `POST /` - Tạo mới
- `PUT /{id}` - Cập nhật
- `DELETE /{id}` - Xóa
- `PUT /{id}/approve` - Duyệt (admin)

---

## 📦 Dependencies

- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - Database ORM
- `spring-boot-starter-security` - Authentication
- `spring-boot-starter-validation` - Request validation
- `spring-boot-starter-mail` - Email service
- `jjwt-api/impl/jackson` - JWT handling
- `mysql-connector-j` - MySQL driver
- `postgresql` - PostgreSQL driver
- `cloudinary-http44` - Image upload
- `lombok` - Code generation
- `mapstruct` - DTO mapping

---

## 🚀 Chạy Backend

```bash
# Development
./mvnw spring-boot:run

# Build
./mvnw clean package
java -jar target/auction-system-0.0.1-SNAPSHOT.jar
```
