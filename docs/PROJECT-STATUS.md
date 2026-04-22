# Project Status

> Last updated: 2026-04-22 | By: @codex | Session: #2
>
> AI: update this file at the end of every session when asked.
> Follow this exact format. Keep it concise — under 80 lines.

---

## Completed
- ✅ Project skeleton (Spring Boot 4, Maven, application.yml)
- ✅ Documentation setup (CLAUDE.md, PROJECT-RULES, ARCHITECTURE, DATABASE, API_SPEC)
- ✅ ADR-001: Refresh token strategy decided (Cookie + Body)
- ✅ ADR-002: File upload strategy decided (Local Storage + Static Resource Serving)
- ✅ AI workflow setup (.claude/commands/)
- ✅ Permission CRUD module (Entity, Repository, DTOs, Service, Controller)
- ✅ Permission tests (unit + integration)
- ✅ Company CRUD module (Entity, Repository, DTOs, Service, Controller)
- ✅ Company tests (unit + integration)
- ✅ Role CRUD module + ManyToMany Permission (Entity, Repository, DTOs, Service, Controller)
- ✅ Role tests (unit + integration)
- ✅ Role CONTEXT.md
- ✅ User CRUD nâng cấp (DTO + quan hệ Company/Role + test)
- ✅ User CONTEXT.md
- ✅ File upload module (filesystem storage, validation, endpoint + tests)
- ✅ File CONTEXT.md

## In Progress
_Nothing._

## Deferred Issues
_None._

## Warnings
_None._

## Next Tasks
1. **[P0]** Bổ sung `AppException` để hoàn tất base exception hierarchy
2. **[P1]** Hoàn thiện auth endpoints và enforce JWT đầy đủ
3. **[P2]** Refresh token flow theo ADR-001
4. **[P3]** RBAC permission-based authorization (path + method)
5. **[P4]** Bổ sung 401/403 test coverage cho các endpoint CRUD hiện có
6. **[P5]** Hoàn thiện checklist review + polish tài liệu cuối phase

## Milestones

### Phase 0 — Foundation
- [ ] Base exception classes (thiếu `AppException`; đã có ResourceNotFoundException, InvalidRequestException)
- [x] GlobalExceptionHandler
- [x] ApiResponse wrapper
- [x] SecurityConfig cơ bản
- [x] JwtConfig (JwtEncoder, JwtDecoder)

### Phase 1 — Independent Entities
- [x] Permission CRUD + unit test + integration test + CONTEXT.md
- [x] Company CRUD + unit test + integration test + CONTEXT.md

### Phase 2 — Role (depends on Permission)
- [x] Role CRUD + ManyToMany Permission + test + CONTEXT.md

### Phase 3 — User (depends on Role + Company)
- [x] User CRUD + ManyToOne Company + ManyToMany Role + test + CONTEXT.md

### Phase 4 — Authentication
- [x] CustomUserDetailsService
- [ ] POST /auth/login + POST /auth/register + test
- [x] Enable JWT enforce in SecurityConfig
- [ ] GET /auth/me + test

### Phase 5 — Refresh Token (ADR-001)
- [x] RefreshToken entity + repository
- [x] POST /auth/refresh (cookie SPA + body mobile)
- [x] POST /auth/logout (revoke + clear cookie)
- [ ] Full auth flow test

### Phase 6 — File Upload (ADR-002)
- [x] StorageService (upload)
- [x] POST /api/v1/files (multipart/form-data)
- [x] File validation (name, extension, folder, size)
- [x] Unit + integration tests

### Phase 7 — RBAC (Permission-based Authorization)
- [ ] Middleware: match request (path + method) → Permission → Role
- [ ] Integrate into SecurityFilterChain
- [ ] Test: 200 (authorized) + 403 (forbidden)
- [ ] Add 401/403 test cases to Phase 1-3 endpoints

### Phase 8 — Polish
- [x] Pagination + sorting for all list endpoints
- [ ] Search / filter (if needed)
- [ ] Scheduled job: cleanup expired refresh tokens
- [ ] Full review (/review-pr) + final docs update
