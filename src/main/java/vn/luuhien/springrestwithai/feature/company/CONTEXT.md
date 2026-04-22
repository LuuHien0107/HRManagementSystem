# Company Feature Context

## Scope
- Quản lý CRUD cho Company (`name`, `description`, `address`, `logo`).
- Company hiện được triển khai độc lập, chưa map quan hệ JPA ngược với User trong phase này.

## API Contract
- `GET /companies` (pagination qua `page`, `size`, `sort`)
- `GET /companies/{id}`
- `POST /companies`
- `PUT /companies` (request body chứa `id`)
- `DELETE /companies/{id}`

## Validation & Business Rules
- `name` là bắt buộc.
- `name` phải unique không phân biệt hoa thường.
- `description`, `address`, `logo` được trim; chuỗi rỗng sau trim sẽ lưu `null`.

## Exceptions
- `ResourceNotFoundException` khi không tìm thấy company theo id.
- `DuplicateResourceException` khi tạo/cập nhật trùng `name`.

## Testing Notes
- Unit test tập trung vào logic service bằng Mockito.
- Integration test dùng MockMvc để kiểm tra đầy đủ HTTP flow và auth/unauth.
