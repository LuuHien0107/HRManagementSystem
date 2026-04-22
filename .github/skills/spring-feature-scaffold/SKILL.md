---
name: spring-feature-scaffold
description: "Use when: creating a new Spring feature module with full CRUD, DTOs, tests, and docs updates in this repository."
---

# Spring Feature Scaffold

Follow this order exactly.

## 1) Understand Context First
- Read `CLAUDE.md`.
- Read `docs/PROJECT-RULES.md` for coding constraints.
- Read `docs/ARCHITECTURE.md` for module boundaries.
- Read `docs/DATABASE.md` and `docs/API_SPEC.md` to avoid duplicating existing schema/endpoints.
- Ask a clarification question if naming or scope is ambiguous.

## 2) Target Structure
Main package (example):
- `feature/{feature_name}/{Feature}.java`
- `feature/{feature_name}/{Feature}Controller.java`
- `feature/{feature_name}/{Feature}Service.java`
- `feature/{feature_name}/{Feature}ServiceImpl.java`
- `feature/{feature_name}/{Feature}Repository.java`
- `feature/{feature_name}/dto/Create{Feature}Request.java`
- `feature/{feature_name}/dto/Update{Feature}Request.java`
- `feature/{feature_name}/dto/{Feature}Response.java`

Test package (mirror source package):
- `{Feature}ServiceImplTest.java` (unit)
- `{Feature}ControllerTest.java` (integration)

## 3) Implementation Sequence
1. Entity with JPA mappings and audit fields where used.
2. Repository interface (Spring Data method naming).
3. DTO records with Jakarta validation and response mapping helper.
4. Service interface contract.
5. Service implementation with transaction boundaries and business validation.
6. Controller endpoints with `@Valid`, `ResponseEntity<ApiResponse<T>>`.

## 4) Required Rules
- Constructor injection only, explicit constructor.
- Controller injects service interface, not impl.
- No entity returned from controller.
- Service handles entity-to-dto conversion.
- Use custom exceptions for not found/conflict/invalid input.
- Keep relationships LAZY by default unless justified.

## 5) Docs and Status Updates
After coding, update when applicable:
- `docs/API_SPEC.md`
- `docs/DATABASE.md`
- `feature/{feature_name}/CONTEXT.md`
- `docs/PROJECT-STATUS.md`

## 6) Verification
- Build compiles.
- New tests added and passing when feasible.
- Public methods/endpoints have both success and error handling paths.
