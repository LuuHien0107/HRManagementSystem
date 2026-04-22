---
name: spring-testing-strategy
description: "Use when: generating or reviewing unit and integration tests for feature modules in this repository."
---

# Spring Testing Strategy

## 1) Read Before Writing
- Read feature source files first (entity, service, service impl, controller, DTO).
- Read feature `CONTEXT.md` if present for known trade-offs.
- Read `docs/API_SPEC.md` for expected response fields and status behavior.

## 2) Unit Test Scope (`{Feature}ServiceImplTest`)
Use Mockito extension and mock only direct dependencies.

Coverage per service method:
- Create: success, conflict/duplicate, related entity not found, verify save call.
- GetById: success, not found.
- List/GetAll: non-empty and empty list behavior.
- Update: success, not found, conflict/validation path.
- Delete: success and not found.

Conventions:
- One behavior per test method.
- Method naming: `methodName_scenario_expectedResult`.
- Add `@DisplayName` for behavior clarity.
- Use `assertThrows` and verify meaningful exception messages.

## 3) Integration Test Scope (`{Feature}ControllerTest`)
Use SpringBootTest + MockMvc and validate full HTTP flow.

Coverage per endpoint:
- POST: success, validation error, duplicate/conflict, unauthorized.
- GET by id: success, not found, unauthorized.
- GET list: pagination behavior and empty result behavior.
- PUT: success, validation error, not found, unauthorized.
- DELETE: success, not found, unauthorized.

Assertions:
- HTTP status code.
- ApiResponse shape (`statusCode`, `data`, `message`, `timestamp` as available in project implementation).
- Content type and key payload fields.

## 4) Anti-Patterns to Avoid
- Do not test trivial getters/setters.
- Do not rely on test order.
- Do not use real database in unit tests.
- Do not duplicate Spring framework tests (basic repository built-ins).

## 5) Verification
- Run target tests or full `mvn test` when feasible.
- Report uncovered branches explicitly if time/scope constrained.
