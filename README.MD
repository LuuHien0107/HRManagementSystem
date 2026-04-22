# HR Management API (Spring Boot 4)

REST API quản lý nhân sự với JWT authentication, refresh token, upload file, và phân quyền theo role/permission.

## Tổng quan

Dự án cung cấp backend API cho các nghiệp vụ:
- Quản lý người dùng (Users)
- Quản lý công ty (Companies)
- Quản lý vai trò (Roles)
- Quản lý quyền (Permissions)
- Xác thực và vòng đời token
- Upload file (avatar/logo)

Tất cả endpoint đều trả về chuẩn `ApiResponse<T>`.

## Chức năng chính

- Xác thực JWT bằng Spring Security OAuth2 Resource Server
- Luồng access token + refresh token
- Đăng ký, đăng nhập, refresh token, logout, lấy thông tin user hiện tại (`/auth/me`)
- CRUD cho User, Company, Role, Permission
- Phân quyền RBAC theo `apiPath + method` thông qua Role -> Permission
- Upload file có validate (đuôi file, kích thước, thư mục)
- Phân trang + sắp xếp cho các endpoint danh sách
- Global exception handling, response JSON thống nhất
- Seed dữ liệu mẫu (permissions, roles, companies, users)

## Công nghệ sử dụng

- Java 25
- Spring Boot 4.0.2
- Spring MVC
- Spring Security 7 + OAuth2 Resource Server
- Spring Data JPA + Hibernate
- MySQL (runtime)
- H2 (test profile)
- Maven
- SpringDoc OpenAPI (Swagger UI)
- JUnit + Mockito + MockMvc

## Cấu trúc dự án

```text
src/main/java/vn/luuhien/springrestwithai
|- config/            # security, jwt, openapi, upload config, seeder
|- dto/               # ApiResponse dùng chung
|- exception/         # custom exceptions + global handler
|- feature/
|  |- auth/
|  |- user/
|  |- company/
|  |- role/
|  |- permission/
|  |- file/
|- security/          # CustomUserDetailsService, PermissionAuthorizationManager
```

## Yêu cầu môi trường

- JDK 25
- Maven 3.9+
- MySQL 8+

## Cấu hình

File cấu hình chính: `src/main/resources/application.yml`

Thiết lập mặc định hiện tại:
- DB: `jdbc:mysql://localhost:3306/hr_management`
- JWT secret + thời gian hết hạn token
- Thư mục upload: `uploads`
- Seed dữ liệu bật sẵn: `app.seed-data=true`

Bạn nên chỉnh lại username/password MySQL theo máy local trước khi chạy.

## Chạy dự án

### Dùng Maven

```bash
mvn spring-boot:run
```

### Dùng Maven Wrapper

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Server mặc định: `http://localhost:8080`

## Tài liệu API

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Chi tiết đầy đủ: `docs/API_SPEC.md`

## Các endpoint chính

### Auth
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

### Các module nghiệp vụ
- Users: `/api/v1/users`
- Companies: `/api/v1/companies`
- Roles: `/api/v1/roles`
- Permissions: `/api/v1/permissions`
- Files: `POST /api/v1/files`

## Mô hình bảo mật

- Public: login, register, swagger, static uploads
- Protected: toàn bộ endpoint còn lại
- Rule phân quyền động từ DB:
  - So khớp request `path + method`
  - Đối chiếu danh sách permission của các role user đang có

## Kiểm thử

Chạy toàn bộ test:

```bash
mvn test
```

Cấu hình test profile: `src/main/resources/application-test.yml`

## Tài liệu liên quan

- `docs/PROJECT-RULES.md`
- `docs/PROJECT-STATUS.md`
- `docs/API_SPEC.md`
- `docs/ARCHITECTURE.md`
- `docs/DATABASE.md`

## Giấy phép

Xem `LICENSE.MD` và `LICENSE_VI.md`.
