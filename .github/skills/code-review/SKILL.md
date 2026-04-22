---
name: code-review
description: "Use when: reviewing changed code in this repository for architecture, security, quality, and documentation compliance."
---

# Project Code Review Checklist

Use this checklist on changed files and report findings with severity.

## Architecture
- Controller only orchestrates request -> service -> response.
- Service uses interface + implementation pattern.
- Controller injects interface, not implementation.
- No HTTP concerns in service layer.
- Entity-to-dto conversion is not in controller.

## Type Safety and DTO
- No entity returned directly from controller.
- Request/response DTOs follow project conventions (records preferred).
- Request DTO has Jakarta validation.
- Sensitive fields are excluded from response.

## Dependency Injection
- Constructor injection only.
- Dependencies are `private final`.
- Explicit constructor present.

## Error Handling
- Custom exceptions used instead of generic runtime exceptions.
- Not-found and conflict scenarios mapped to proper exception types.
- Controller avoids local try/catch for business errors handled globally.

## Security
- No hardcoded secrets.
- No sensitive data in logs/responses/JWT claims.
- Endpoints have explicit authorization policy.
- Public endpoint list in security config is intentional.

## JPA and Data
- Relationships default to LAZY unless justified.
- Transaction boundaries on write operations.
- Optional return for single lookup repository methods.
- Avoid obvious N+1 query patterns.

## Code Quality
- File and method sizes remain maintainable.
- Logging uses placeholders instead of string concatenation.
- No Lombok shortcuts that violate project constraints.

## Documentation
- Feature includes `CONTEXT.md` when required.
- `docs/API_SPEC.md` updated for endpoint changes.
- `docs/DATABASE.md` updated for schema changes.

## Output Format
1. Blockers (must fix)
2. Suggestions (non-blocking)
3. Good parts

Each finding should include concrete file references and risk explanation.
