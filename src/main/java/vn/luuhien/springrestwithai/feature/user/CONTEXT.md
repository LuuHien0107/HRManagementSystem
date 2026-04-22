# User Feature Context

## Scope
- Quản lý CRUD cho User với các trường hồ sơ cơ bản.
- User có quan hệ `ManyToOne` với Company và `ManyToMany` với Role.
- Endpoint không trả entity trực tiếp, luôn trả DTO `UserResponse`.

## API Contract
- `GET /users` (pagination qua `page`, `size`, `sort`)
- `GET /users/{id}`
- `POST /users`
- `PUT /users` (request body chứa `id`)
- `DELETE /users/{id}`

## Validation & Business Rules
- `name`, `email`, `password` bắt buộc khi tạo mới.
- `email` phải đúng định dạng và unique không phân biệt hoa thường.
- `roleIds` bắt buộc và phải có ít nhất 1 phần tử cho cả create/update.
- `companyId` là tùy chọn; nếu có phải tồn tại.
- Tất cả `roleIds` phải tồn tại; thiếu bất kỳ id nào sẽ trả not found.
- `email` và `password` không cập nhật qua endpoint update user.

## Relationship Notes
- Owner side:
  - `User.company` với `@JoinColumn(name = "company_id")`
  - `User.roles` với `@JoinTable(name = "user_role")`
- Inverse side:
  - `Company.users` và `Role.users` dùng `mappedBy` + `@JsonIgnore` để tránh recursion.
- Tất cả quan hệ dùng `FetchType.LAZY`.

## Exceptions
- `ResourceNotFoundException` khi không tìm thấy User/Company/Role.
- `DuplicateResourceException` khi trùng email.

## Testing Notes
- Unit test (`UserServiceImplTest`): kiểm tra logic service bằng Mockito.
- Integration test (`UserControllerTest`, `UserControllerMutationTest`): kiểm tra full HTTP flow với MockMvc, gồm unauthorized và các nhánh lỗi chính.
