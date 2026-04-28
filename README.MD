# HR Management API (Spring Boot 4)

REST API quan ly nhan su voi JWT auth, refresh token rotation, RBAC theo `apiPath + method`, upload file va dashboard tong quan.

## Tong quan

Backend gom cac feature:
- Auth: register/login/me/refresh/logout
- User, Company, Role, Permission CRUD
- Dashboard tong hop so lieu
- Upload file avatar/logo

Tat ca API business tra ve `ApiResponse<T>`.

## Trang thai hien tai

- Spring Boot `4.0.2`, Java `25`
- Security bang OAuth2 Resource Server + JWT
- RBAC dong tu DB (Role -> Permission)
- Refresh token luu DB (`refresh_tokens`), revoke khi logout, co cleanup scheduler moi ngay luc `02:00`
- User filter ho tro: `name`, `address`, `email`, `ageFrom`, `ageTo`, `gender`
- Pagination/sorting cho cac API list

## Cong nghe

- Spring MVC, Spring Security 7, Spring Data JPA, Hibernate
- MySQL (runtime), H2 (test)
- SpringDoc OpenAPI (Swagger UI)
- JUnit 5, Mockito, MockMvc

## Cau truc du an

```text
src/main/java/vn/luuhien/springrestwithai
|- config/          # security, jwt, openapi, scheduler, seeder, upload config
|- dto/             # ApiResponse, PagedResult
|- exception/       # base exception + global handler
|- feature/
|  |- auth/
|  |- user/
|  |- company/
|  |- role/
|  |- permission/
|  |- file/
|  |- dashboard/
|- security/        # CustomUserDetailsService, PermissionAuthorizationManager
```

## Yeu cau moi truong

- JDK 25
- Maven 3.9+
- MySQL 8+

## Cau hinh chinh

File: `src/main/resources/application.yml`

Cac muc can kiem tra truoc khi chay:
- `spring.datasource.*` (url/username/password)
- `jwt.secret`, `jwt.expiration`, `jwt.refresh-expiration`
- `app.upload.base-dir`
- `app.seed-data=true` (dev seed)

## Chay du an

```bash
mvn spring-boot:run
```

Windows (wrapper):

```powershell
.\mvnw.cmd spring-boot:run
```

Server mac dinh: `http://localhost:8080`

## Swagger / OpenAPI

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Endpoint chinh

- Auth: `/api/v1/auth/*`
- Users: `/api/v1/users`
- Companies: `/api/v1/companies`
- Roles: `/api/v1/roles`
- Permissions: `/api/v1/permissions`
- Dashboard: `/api/v1/dashboard`
- Files: `POST /api/v1/files`

Chi tiet day du: `docs/API_SPEC.md`

## Bao mat

- Public: login/register + swagger/resources whitelist
- Protected: cac endpoint con lai
- Authorization:
  - Match `HTTP method + request path`
  - Check permission theo role trong JWT
  - Ho tro role key ca dang `SUPER_ADMIN` va `ROLE_SUPER_ADMIN`

## Test

Chay toan bo:

```bash
mvn test
```

Neu moi truong Maven local repo bi loi quyen, dung:

```bash
mvn -Dmaven.repo.local=.m2/repository test
```

## Tai lieu lien quan

- `docs/PROJECT-STATUS.md`
- `docs/PROJECT-RULES.md`
- `docs/API_SPEC.md`
- `docs/DATABASE.md`
- `docs/ARCHITECTURE.md`
- `docs/decisions/*.md`

## License

Xem `LICENSE.MD` va `LICENSE_VI.md`.
