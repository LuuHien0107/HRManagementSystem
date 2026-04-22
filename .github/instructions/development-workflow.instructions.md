---
description: "Use when: any coding or planning task in this repository. Enforce project reading order and session startup checks."
applyTo: "**"
---

# Development Workflow

Before writing or modifying code, always read in this order:
1. `CLAUDE.md`
2. `docs/PROJECT-STATUS.md`

Read additional docs only if relevant:
- `docs/PROJECT-RULES.md` for coding conventions and architecture contracts.
- `docs/ARCHITECTURE.md` for cross-module work/new features.
- `docs/DATABASE.md` for entity/schema/table changes.
- `docs/API_SPEC.md` for endpoint design or API updates.

At session start, summarize:
1. Last completed work
2. In-progress work
3. Active warnings/deferred issues
4. Next recommended task

Do not start implementation until the user confirms the task after startup summary.
