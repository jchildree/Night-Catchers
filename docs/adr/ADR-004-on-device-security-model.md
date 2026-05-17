# ADR-004: On-Device Security Model

**Project:** Night Catchers
**ADR ID:** ADR-004
**Status:** ACCEPTED
**Date:** 2026-05-17
**Authors:** Night Catchers Team
**Affected Layers:** Core-Security, Core-Data, App

---

## Context

The app stores a child's monster collection and pet state locally and syncs
parent-owned data to Firestore. It also has a parental PIN gate and an audit
trail. Keys, credentials, and tamper evidence must withstand device
compromise and untrusted local inspection.

## Decision

| Aspect | Details |
|--------|---------|
| **Primary Decision** | SQLCipher AES-256 key derived from the Android KeyStore (never hardcoded). Parent PIN stored as a bcrypt hash via `UserRepositoryImpl.hashPin()`. `AuditEvent.prevHash` forms a SHA-256 tamper chain with no skipped seq numbers. File sharing through `FileProvider` only — never raw `file://`. Firebase App Check (Play Integrity in release, Debug provider in debug) initialised in `NightCatchersApplication`. Firestore rules require `request.auth.uid == parentUID` on all writes. |
| **Rationale** | Hardware-backed key storage + hashed credentials + an append-only audit chain give defense in depth without shipping secrets in the APK. |
| **Trade-offs** | Gain: encrypted-at-rest data, tamper-evident audit, attestation-gated backend. Lose: KeyStore/App Check edge cases on some OEM devices; bcrypt cost adds PIN-check latency. |

## Consequences

| Positive | Negative |
|----------|----------|
| DB unreadable without the KeyStore-bound key | KeyStore reset (e.g. lock-screen change) can invalidate keys |
| PIN never recoverable from storage | bcrypt work factor adds verify latency |
| Audit gaps are detectable | seq/hash continuity must be preserved on every write |

## Implementation Notes

- Key derivation + Biometric/PIN in `:core:security`; App Check wired in
  `NightCatchersApplication` via `BuildConfig.DEBUG`.
- Firestore rules live in `firestore.rules`.
- These are the "Security Invariants" of `CLAUDE.md`; deviations require a
  superseding ADR.

## Related ADRs

- ADR-003: COPPA-compliant data model (what this protects)
