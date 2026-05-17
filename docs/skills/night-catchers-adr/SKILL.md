# Night Catchers Architectural Decision Record (ADR) Skill

## Skill Metadata

- **Skill Name:** Night Catchers Architectural Decision Records (ADRs)
- **Version:** 1.0
- **Description:** Governs creation of lightweight, CI-enforced architectural
  decision records for the Night Catchers Android app. Enforces the modular
  Gradle layering, UDF + repository + use-case pattern, COPPA/security
  invariants, and a mandatory folder structure validated by GitHub Actions.
- **Tags:** architecture, decisions, governance, android, kotlin, adr
- **Project:** Night Catchers (Android / Kotlin / Jetpack Compose)

> Adapted from a generic ADR skill. All stack-specific rules are rewritten for
> this repository. `CLAUDE.md` is the architectural source of truth.

---

## Trigger Patterns

Activate when the user says things like:

- "Create an ADR" / "Document this architectural decision" / "ADR for…"
- "I need to decide between X and Y" (architectural context)
- "Should we use Room or DataStore for…", "Where should this use case live?"
- "How do we gate parent vs child data?"
- Discussing trade-offs for module boundaries, navigation, WorkManager jobs,
  the filter pipeline, persistence, sync, or security/COPPA handling.

---

## Purpose

Document **why** architectural decisions were made so design intent survives,
the same debate isn't re-run, and agents can detect new code that contradicts an
ACCEPTED decision.

### When to create an ADR

- Choosing between competing patterns affecting multiple modules.
- Adding a pattern that crosses the `app → feature → core` boundary.
- Persistence/sync strategy (Room + SQLCipher, Firestore mapping).
- Auth/session/security handling, or anything touching COPPA invariants.
- Navigation graph structure or new WorkManager cadence.

**Not needed for:** bug fixes, refactors within an existing pattern, or
single-file changes.

---

## Key Constraints (every ADR must respect)

1. **Three Laws of Readable Code**
   - Names reveal intent (no cryptic abbreviations).
   - Each unit does one thing with explicit side effects.
   - Comments explain *why*, not *what*.
2. **Architecture Rules** (from `CLAUDE.md`)
   - UDF only: ViewModels expose `StateFlow<UiState>` + `SharedFlow<UiEvent>`.
   - Repository pattern: features depend on domain interfaces, never on
     `*RepositoryImpl` or Room DAOs.
   - Business logic lives in `:core:domain` use cases; ViewModels call use
     cases, not repositories.
   - `:core:domain` has zero Android dependencies (pure Kotlin).
   - Hilt: ViewModels `@HiltViewModel`, Workers `@HiltWorker`.
3. **COPPA & Security invariants** (never violate without a superseding ADR):
   no GPS in `toFirestoreMap()`, local UUID `childId` (no child auth), only
   `childFirstName`, no child analytics, parent email behind
   `ParentSessionManager`; SQLCipher key from KeyStore, bcrypt PIN, SHA-256
   audit chain, `FileProvider` only, Firebase App Check.
4. **Naming conventions**
   - Kotlin classes & Composables: `PascalCase`; packages: lowercase
     `com.nightcatchers.*`.
   - No raw hex in Composables — use colour tokens from `:core:ui` `Color.kt`.
   - ADR filenames: `ADR-###-[kebab-case-slug].md` (3-digit zero-padded).
5. **Mandatory folder structure** — all ADRs in `docs/adr/`; master registry at
   `docs/adr/INDEX.md`. GitHub Actions enforces this.

---

## ADR Template

See `docs/adr/_template.md`. Required header:

```
**Project:** Night Catchers
**ADR ID:** ADR-###
**Status:** PROPOSED | ACCEPTED | DEPRECATED
**Date:** YYYY-MM-DD
**Authors:** [Name]
**Affected Layers:** App | Feature | Core-Domain | Core-Data | Core-UI | Core-Security | Build-Logic
```

Body sections: **Context**, **Decision** (Primary Decision / Rationale /
Trade-offs table), **Consequences** (Positive | Negative table),
**Implementation Notes** (modules touched, Gradle/CI/WorkManager impact, test
scope), **Related ADRs**.

---

## Approval Checklist

- [ ] File at `docs/adr/ADR-###-[kebab-case-slug].md`, number zero-padded.
- [ ] Header complete: Project, ADR ID, Status, Date, Authors, Affected Layers.
- [ ] Status ∈ {PROPOSED, ACCEPTED, DEPRECATED}.
- [ ] Three Laws satisfied (intent-revealing, single-purpose, *why* comments).
- [ ] Respects UDF + repository + use-case rules; no Android in `:core:domain`.
- [ ] Does not contradict COPPA/security invariants (or explicitly supersedes a
      prior ADR that did).
- [ ] No raw hex in any UI guidance — references `:core:ui` colour tokens.
- [ ] `Affected Layers` uses the allowed module-layer values.
- [ ] `docs/adr/INDEX.md` updated (sorted ascending) with the new row.

---

## Naming Rules (CI-enforced)

Valid: `ADR-001-modular-gradle-convention-plugins.md`,
`ADR-010-firestore-sync-strategy.md`.
Invalid: `ADR-1-foo.md` (not zero-padded), `adr-001-foo.md` (lowercase),
`ADR_001_foo.md` (wrong separator), `ADR-001_foo-bar.md` (mixed separators).

Pattern: `ADR-###-[kebab-case-slug].md`, `###` always 3 digits.

---

## Master Index (`docs/adr/INDEX.md`)

| ADR | Title | Status | Affected Layers | Date |
|-----|-------|--------|-----------------|------|

- Sorted by ADR ID ascending.
- Updated on every new ADR and on every status change.
- CI checks: data-row count == ADR file count (excluding `_template.md`).

---

## Agent Discovery Protocol

1. Entry point: read `docs/adr/INDEX.md`.
2. Filter by Status and/or Affected Layers.
3. Load the full `docs/adr/ADR-###-*.md`.
4. Follow "Related ADRs" cross-references.
5. Enforce: block new decisions that contradict an ACCEPTED ADR.
6. Report decision history and any conflicts.

---

## GitHub Actions Validation

`.github/workflows/validate-adr.yml` runs on PRs/pushes touching `docs/adr/**`
and checks: filename format & zero-padding, required metadata, valid Status,
no duplicate numbers, all ADRs under `docs/adr/`, and INDEX.md row count ==
file count. A failure blocks the PR with a specific message.

---

## Folder Structure

```
docs/adr/
  INDEX.md                 # master registry (entry point)
  _template.md             # reference copy
  ADR-001-*.md ...
docs/skills/night-catchers-adr/
  SKILL.md                 # this skill
  QUICK_REFERENCE.md       # team checklist
.claude/skills/adr/
  SKILL.md                 # auto-discoverable Claude Code skill
```

---

## Conflict Resolution

`CLAUDE.md` is the source of truth. If an inherited instruction or an ADR
contradicts `CLAUDE.md`: flag the conflict, fix the ADR (or write a superseding
one), and update `CLAUDE.md`. Changes are additive — supersede, don't silently
rewrite history.
