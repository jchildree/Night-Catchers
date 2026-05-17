# Architectural Decision Records — Index

This is the master registry for Night Catchers ADRs. It is the entry point for
humans and agents: filter by **Status** or **Affected Layers**, then open the
full `ADR-###-*.md` file. Keep rows sorted by ADR ID ascending and update this
table whenever an ADR is added or its status changes. CI
(`.github/workflows/validate-adr.yml`) checks that the row count matches the
number of ADR files (excluding `_template.md`).

| ADR | Title | Status | Affected Layers | Date |
|-----|-------|--------|-----------------|------|
| ADR-001 | Modular Gradle Architecture with Convention Plugins | ACCEPTED | App, Feature, Core-Domain, Core-Data, Core-UI, Core-Security, Build-Logic | 2026-05-17 |
| ADR-002 | Unidirectional Data Flow with Repository and Use-Case Layers | ACCEPTED | Feature, Core-Domain, Core-Data | 2026-05-17 |
| ADR-003 | COPPA-Compliant Child Data Model | ACCEPTED | Core-Data, Core-Domain, Core-Security | 2026-05-17 |
| ADR-004 | On-Device Security Model | ACCEPTED | Core-Security, Core-Data, App | 2026-05-17 |
| ADR-005 | Device-Tier GPU Degradation Strategy | ACCEPTED | Feature, Core-Common | 2026-05-17 |

## Status legend

- **PROPOSED** — under discussion, not yet binding.
- **ACCEPTED** — binding; new code must not contradict it.
- **DEPRECATED** — superseded; see the replacing ADR in its "Related ADRs".

## Conflict resolution

`CLAUDE.md` is the source of truth. If an ADR and `CLAUDE.md` disagree, fix the
ADR (or write a superseding one) and update `CLAUDE.md` — never silently
diverge.
