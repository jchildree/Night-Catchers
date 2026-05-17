# ADR-002: Unidirectional Data Flow with Repository and Use-Case Layers

**Project:** Night Catchers
**ADR ID:** ADR-002
**Status:** ACCEPTED
**Date:** 2026-05-17
**Authors:** Night Catchers Team
**Affected Layers:** Feature, Core-Domain, Core-Data

---

## Context

Compose UI needs a predictable state model, and business logic must be testable
without Android. Without an enforced pattern, ViewModels tend to reach directly
into Room DAOs or repository implementations, making logic untestable and
coupling features to data details.

## Decision

| Aspect | Details |
|--------|---------|
| **Primary Decision** | Adopt strict Unidirectional Data Flow: ViewModels expose `StateFlow<UiState>` + `SharedFlow<UiEvent>`, call **use cases** in `:core:domain`, and never touch repositories or DAOs directly. Features depend on domain interfaces only. |
| **Rationale** | `:core:domain` stays pure Kotlin (zero Android deps) so business rules are unit-testable. The repository pattern hides Room/Firestore behind interfaces. UDF removes two-way binding and makes state reproducible. |
| **Trade-offs** | Gain: testable domain, decoupled features, predictable UI state. Lose: more boilerplate (use case per operation, interface + impl per repository). |

## Consequences

| Positive | Negative |
|----------|----------|
| Domain logic tested with JUnit5 + Kotest, no Robolectric | More classes per feature |
| Features swap fakes for repositories in tests easily | Use-case proliferation for trivial operations |
| Single state object per screen, no binding races | Discipline required to keep `:core:domain` Android-free |

## Implementation Notes

- ViewModels are `@HiltViewModel`; Workers are `@HiltWorker`.
- Business logic in `:core:domain` use cases; `:core:data` holds
  `*RepositoryImpl` + Room/Firestore.
- Async/Flow tested with Turbine; repositories faked via `:core:testing`.
- Rules formalised here are the "Architecture Rules" section of `CLAUDE.md`.

## Related ADRs

- ADR-001: Modular Gradle architecture (enforces the layer boundaries)
- ADR-003: COPPA-compliant data model (repository-level enforcement point)
