# Permission Feature Context

## Scope
- Manage CRUD for Permission entity (`name`, `apiPath`, `method`, `module`).
- Permission is independent in current phase (no relationship mapping yet).

## API Contract
- `GET /permissions` (pagination via `page`, `size`, `sort`)
- `GET /permissions/{id}`
- `POST /permissions`
- `PUT /permissions` (request body includes `id`)
- `DELETE /permissions/{id}`

## Validation & Business Rules
- `apiPath + method` must be unique.
- `method` must be one of: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`.
- `module` and `method` are normalized to uppercase before persist.

## Exceptions
- `ResourceNotFoundException` when permission id does not exist.
- `DuplicateResourceException` when duplicate `apiPath + method`.
- `InvalidRequestException` for invalid method values after normalization.

## Testing Notes
- Unit tests cover service logic with Mockito.
- Integration tests cover full HTTP flow with MockMvc and Spring Security auth/unauth cases.
