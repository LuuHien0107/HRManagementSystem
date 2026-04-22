---
description: "Use when: generating or reviewing Java/Spring code in this repository. Apply local coding rules from PROJECT-RULES.md."
applyTo: "src/**/*.java"
---

# Project Rules Snapshot

Apply these rules by default:
- Keep feature package pattern: entity + repository + service interface + service impl + controller + dto.
- Use constructor injection only with explicit constructors; avoid field injection.
- Controller depends on service interface, not implementation class.
- No business logic in controller; service handles entity-to-dto mapping.
- Every endpoint returns `ApiResponse<T>` wrapped in `ResponseEntity`.
- Add `@Valid` on every request body DTO.
- Prefer custom exceptions and GlobalExceptionHandler flow.
- Use Java records for request/response DTOs.
- Add/update tests for happy path + error path.

When code conflicts with this snapshot, treat `docs/PROJECT-RULES.md` as source of truth.
