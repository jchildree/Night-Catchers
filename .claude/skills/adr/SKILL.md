---
name: adr
description: Create and manage Architectural Decision Records for the Night Catchers Android app. Use when the user wants to write an ADR, document an architectural decision, or decide between competing patterns affecting multiple Gradle modules, persistence/sync, navigation, WorkManager, the filter pipeline, or COPPA/security handling.
---

# Night Catchers ADR Skill

Authoritative spec: `docs/skills/night-catchers-adr/SKILL.md`.
Team checklist: `docs/skills/night-catchers-adr/QUICK_REFERENCE.md`.
`CLAUDE.md` is the architectural source of truth — never contradict it.

## When to use

Create an ADR for decisions that span multiple modules or the
`app → feature → core` layers: persistence/sync (Room + SQLCipher, Firestore
mapping), auth/session/security, COPPA-adjacent data handling, navigation graph
structure, new WorkManager cadences, or filter-pipeline / device-tier behaviour.
Skip for bug fixes, in-pattern refactors, and single-file changes.

## Procedure

1. Read `docs/adr/INDEX.md` to find the next ADR number and check no ACCEPTED
   ADR already contradicts the proposed decision.
2. Copy `docs/adr/_template.md` to
   `docs/adr/ADR-###-[kebab-case-slug].md` (number zero-padded to 3 digits).
3. Fill the header and body:
   - Header: `Project: Night Catchers`, `ADR ID`, `Status`
     (PROPOSED | ACCEPTED | DEPRECATED), `Date` (YYYY-MM-DD), `Authors`,
     `Affected Layers` (App | Feature | Core-Domain | Core-Data | Core-UI |
     Core-Security | Build-Logic / Core-Common / Core-Network).
   - Body: Context; Decision (Primary Decision / Rationale / Trade-offs);
     Consequences (Positive | Negative); Implementation Notes (modules touched,
     Gradle/CI/WorkManager impact, test scope); Related ADRs.
4. Add a row to `docs/adr/INDEX.md`, keeping rows sorted by ADR ID ascending.
5. Run the approval checklist in the authoritative spec.

## Constraints to honour

- Three Laws of Readable Code (intent-revealing names, single-purpose units,
  *why* comments).
- CLAUDE.md Architecture Rules: UDF, repository pattern, use cases in
  `:core:domain` (no Android there), Hilt injection.
- COPPA/Security invariants: no GPS in Firestore, local UUID `childId`, only
  `childFirstName`, no child analytics, parent email gated, SQLCipher key from
  KeyStore, bcrypt PIN, SHA-256 audit chain, FileProvider, App Check.
- No raw hex in Composables — use `:core:ui` colour tokens.

CI (`.github/workflows/validate-adr.yml`) enforces filename format, metadata,
valid status, uniqueness, and INDEX-count sync; a failure blocks the PR.
