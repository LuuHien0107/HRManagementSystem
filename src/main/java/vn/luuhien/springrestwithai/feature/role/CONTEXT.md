# Role Feature Context

## Scope
- Quản lý CRUD cho Role (`name`, `description`) và gán Permission qua `permissionIds`.
- Role có quan hệ ManyToMany với Permission qua bảng `permission_role`.

## API Contract
- `GET /roles` (pagination qua `page`, `size`, `sort`)
- `GET /roles/{id}`
- `POST /roles`
- `PUT /roles` (request body chứa `id`)
- `DELETE /roles/{id}`

## Validation & Business Rules
- `name` là bắt buộc, trim trước khi lưu.
- `name` phải unique không phân biệt hoa thường.
- `description` được trim; chuỗi rỗng sau trim sẽ lưu `null`.
- `permissionIds` là danh sách thay thế toàn bộ quyền của role khi create/update.
- Nếu có `permissionIds` không tồn tại, trả `ResourceNotFoundException`.

## Relationship Notes
- Owner side ở `Role.permissions` với `@JoinTable(name = "permission_role")`.
- Inverse side ở `Permission.roles` với `mappedBy = "permissions"` và `@JsonIgnore` để tránh recursion khi serialize.
- Tất cả quan hệ dùng `FetchType.LAZY`.

## Exceptions
- `ResourceNotFoundException` khi không tìm thấy role theo id hoặc permission ids không tồn tại.
- `DuplicateResourceException` khi tạo/cập nhật trùng `name`.

## Testing Notes
- Unit test: tập trung business logic service bằng Mockito.
- Integration test: full HTTP flow với MockMvc + security auth/unauth cho các endpoint chính.
