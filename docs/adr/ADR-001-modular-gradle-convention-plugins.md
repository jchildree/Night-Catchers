# ADR-001: Modular Gradle Architecture with Convention Plugins

**Project:** Night Catchers
**ADR ID:** ADR-001
**Status:** ACCEPTED
**Date:** 2026-05-17
**Authors:** Night Catchers Team
**Affected Layers:** App, Feature, Core-Domain, Core-Data, Core-UI, Core-Security, Build-Logic

---

## Context

Night Catchers spans AR/camera, a pet system, a vault, parental controls, and
onboarding. A single-module app would couple unrelated features and slow
incremental builds. We need enforced layering and consistent module setup
without copy-pasted Gradle boilerplate.

## Decision

| Aspect | Details |
|--------|---------|
| **Primary Decision** | Split the app into `app/` → `feature/*` → `core/*` modules, configured exclusively through `build-logic` convention plugins. |
| **Rationale** | Convention plugins (`nightcatchers.android.library`, `.feature`, `.application`, `.hilt`, `.compose`, `.testing`) centralise SDK levels, Java 17, Hilt/KSP, and Compose setup. `.feature` auto-adds `:core:ui`, `:core:domain`, `:core:common`, so feature modules cannot accidentally depend on the wrong layer. All versions live in `gradle/libs.versions.toml`. |
| **Trade-offs** | Gain: fast incremental builds, enforced layering, one place to bump versions. Lose: an extra `build-logic` indirection that new contributors must learn. |

## Consequences

| Positive | Negative |
|----------|----------|
| Feature modules share a single, audited dependency set | Convention-plugin changes ripple across all modules |
| No hardcoded dependency versions in module build files | Requires understanding the plugin layer before adding a module |
| Parallel module compilation | Module graph must be kept acyclic by hand |

## Implementation Notes

- Convention plugins live in `build-logic/`; applied in each module's
  `build.gradle.kts`.
- All versions in `gradle/libs.versions.toml` — never hardcode in modules.
- Module map and plugin responsibilities are documented in `CLAUDE.md`
  ("Module Map" and "Build").
- New modules: scaffold via the existing `.claude/commands/scaffold-module.md`.

## Related ADRs

- ADR-002: UDF + Repository + UseCase pattern (the layering this enables)
