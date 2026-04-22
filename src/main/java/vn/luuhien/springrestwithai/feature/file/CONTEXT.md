# File Feature Context

## Scope
- Upload single image file for user avatar or company logo.
- Files are stored on local filesystem, not in database.

## API Contract
- `POST /api/v1/files` with multipart fields: `file`, `folder`.
- `folder` only accepts `avatars` or `logos`.

## Validation & Business Rules
- File is required and must not be empty.
- File name must be non-blank and only include characters: letters, digits, `-`, `_`, `.`.
- Allowed extensions: `jpg`, `jpeg`, `png`, `gif`, `webp`.
- File size max: 5 MB.
- Stored file name format: `{epochMillis}_{originalFileName}`.

## Security
- Upload endpoint requires authentication.
- Static file path `/uploads/**` is public for image rendering.

## Testing Notes
- Unit tests mock only upload properties and use `MockMultipartFile`.
- Integration tests verify full HTTP flow with MockMvc and auth/unauth cases.
