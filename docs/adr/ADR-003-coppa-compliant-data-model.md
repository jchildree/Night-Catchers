# ADR-003: COPPA-Compliant Child Data Model

**Project:** Night Catchers
**ADR ID:** ADR-003
**Status:** ACCEPTED
**Date:** 2026-05-17
**Authors:** Night Catchers Team
**Affected Layers:** Core-Data, Core-Domain, Core-Security

---

## Context

Night Catchers is "a personal app for kids." COPPA prohibits collecting
personal information from children without verifiable parental consent.
Children use the app directly, so the data model itself must make
non-compliant data physically impossible to collect or sync.

## Decision

| Aspect | Details |
|--------|---------|
| **Primary Decision** | Children have no Firebase Auth identity — a locally-generated UUID (`childId`) only. Store `childFirstName` exclusively (no full name, DOB, or email). `MonsterEntity.toFirestoreMap()` must never include `captureLatLng` or GPS. No analytics for `AccountTier.CHILD`. Parent email gated behind `ParentSessionManager`. |
| **Rationale** | Eliminating child PII and child-account auth removes the consent-gathering burden for the child surface and prevents accidental cloud leakage of location/identity. |
| **Trade-offs** | Gain: COPPA compliance by construction, no child PII at rest or in transit. Lose: no per-child cloud analytics; cross-device child continuity is limited. |

## Consequences

| Positive | Negative |
|----------|----------|
| GPS can never leave the device via Firestore | No location features for capture history |
| No child identity to breach | Reduced telemetry for child UX tuning |
| Parent data access is session-gated | Extra checks in serialization + sync paths |

## Implementation Notes

- Enforce in `:core:data` serialization (`toFirestoreMap()`) and in
  `ParentSessionManager` (`:core:security`).
- These are the non-negotiable "COPPA Invariants" in `CLAUDE.md`; any change
  here requires a superseding ADR, not an ad-hoc edit.
- Firestore rules additionally enforce parent-only writes (see ADR-004).

## Related ADRs

- ADR-004: On-device security model (auth/storage enforcement)
- ADR-002: UDF + Repository pattern (repository is the enforcement layer)
