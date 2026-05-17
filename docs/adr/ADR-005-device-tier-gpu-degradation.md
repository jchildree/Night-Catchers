# ADR-005: Device-Tier GPU Degradation Strategy

**Project:** Night Catchers
**ADR ID:** ADR-005
**Status:** ACCEPTED
**Date:** 2026-05-17
**Authors:** Night Catchers Team
**Affected Layers:** Feature, Core-Common

---

## Context

The AR filter pipeline (`feature/filters`, `feature/ar`) runs GLSL shaders and
multiple FBO passes. Target devices range from flagship to low-RAM phones with
no OpenGL ES 3.0. A one-size pipeline either crashes weak devices or wastes
capable ones. Degradation must be silent to a child — never an error screen.

## Decision

| Aspect | Details |
|--------|---------|
| **Primary Decision** | Classify devices into Tier A (≥6 GB RAM + ARCore + GL ES 3.1: all lenses, full res), Tier B (≥3 GB RAM + GL ES 3.0: max 2 FBO passes, half res), Tier C (<3 GB RAM or no GL ES 3.0: no OpenGL, Lottie fallback). Emergency downgrade when 3 consecutive frames exceed 20 ms, handled in `FilterLayerManager`. |
| **Rationale** | A static tier sets a safe baseline; the runtime frame-time trigger catches thermal throttling and misclassification without user-visible failure. |
| **Trade-offs** | Gain: no crashes on weak hardware, full fidelity on strong hardware, silent UX. Lose: tier-specific code paths and broader test matrix. |

## Consequences

| Positive | Negative |
|----------|----------|
| Low-end devices stay usable via Lottie | Three rendering paths to maintain and test |
| High-end devices get full lens stack | Tier thresholds need periodic re-validation |
| Throttling handled gracefully mid-session | Frame-time heuristic needs careful tuning |

## Implementation Notes

- Tier detection in `:core:common` (`DeviceTier`); emergency downgrade in
  `FilterLayerManager` (`feature/filters`).
- Lens composition constraints (max 2 lenses, solo celebration lenses,
  `PROTON_PACK` on top) interact with tier limits — see `CLAUDE.md`
  "Device Tiers" and "Lens Composition Rules".
- Degradation must never surface a capability error to the child.

## Related ADRs

- ADR-001: Modular Gradle architecture (tier logic placement across modules)
