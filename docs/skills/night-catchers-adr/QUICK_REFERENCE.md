# Night Catchers ADR — Quick Reference

### Create an ADR when…

- A decision spans multiple Gradle modules or the `app → feature → core` layers.
- Persistence/sync strategy (Room + SQLCipher, Firestore mapping).
- Auth/session/security or anything near a COPPA invariant.
- Navigation graph structure or a new WorkManager cadence.
- Filter-pipeline / device-tier behaviour.

Skip it for: bug fixes, in-pattern refactors, single-file tweaks.

### Steps

1. Copy `docs/adr/_template.md` → `docs/adr/ADR-###-[slug].md`
   (next number, zero-padded, kebab-case slug).
2. Fill the header (Project / ADR ID / Status / Date / Authors / Affected
   Layers) and all body sections.
3. Add a row to `docs/adr/INDEX.md` (keep sorted ascending).
4. Push — `validate-adr.yml` checks format, metadata, uniqueness, INDEX sync.

### Naming

| Context | Style | Example |
|---------|-------|---------|
| ADR filename | `ADR-###-kebab-case` | `ADR-006-firestore-sync-strategy.md` |
| Kotlin class / Composable | `PascalCase` | `PetRoomScreen.kt` |
| Package | lowercase | `com.nightcatchers.pet` |
| UI colour | token, never raw hex | `SlimeGreen` (from `:core:ui` `Color.kt`) |

### Affected Layers (allowed values)

`App` · `Feature` · `Core-Domain` · `Core-Data` · `Core-UI` ·
`Core-Security` · `Build-Logic` (and `Core-Common` / `Core-Network` where
relevant).

### Hard rules

- All ADRs in `docs/adr/` — no exceptions (CI-enforced).
- `INDEX.md` row count must equal ADR file count.
- Never contradict a COPPA/Security invariant without a superseding ADR.
- `CLAUDE.md` wins any conflict.
