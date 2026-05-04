# DESIGN_GUIDE.md

This file provides design guidance to Claude Code (claude.ai/code) when working with code in this repository.

> **Spec vs. implementation:** This document describes the intended design. Where implementation diverges, it is noted inline. For current implementation details see `CLAUDE.md`.

---

## Night Catchers — Design Guide

**Version:** 1.1  
**For:** Android · Kotlin 2.0 · Jetpack Compose  
**Aesthetic:** Ghostbusters (Capture) × "If" (Pet)

---

## Design Philosophy

Every screen must honour these six principles:

1. **Whimsical · Not Childish** — Playful with craft. Surprising details. Every interaction has personality.
2. **Dark-Safe First** — Kids hunt monsters at night. All UI readable at maximum darkness. OLED-friendly.
3. **No Death, No Punishment** — Monsters are immortal friends. Low stats mean sadness, not loss.
4. **Kids-First Accessibility** — 48×48dp minimum touch targets. No spoiler screen-reading. Haptic feedback everywhere.
5. **Emotional Expressiveness** — Bond is the reward. Every interaction visibly/haptically confirms the monster felt it.
6. **Performance Is Design** — A filter that drops frames breaks immersion. ≤16ms/frame on Tier B. Fallbacks first-class.

---

## Visual System

### Color Palette

#### Capture Mode — Ghostbusters

| Token | Hex | Usage |
| ------- | ----- | ------- |
| **Slime Green** | `#7FFF00` | Primary CTA, proton beam, active states |
| **Ecto Cyan** | `#00F5FF` | AR anchors, HUD, radar blips |
| **Alert Red** | `#FF6B6B` | Danger states, low stats, escape |
| **Rarity Gold** | `#FFD700` | Rare/legendary, XP, achievements |
| **Beam Orange** | `#FF9F43` | Beam mid-range, warm accent |

#### Pet Mode — "If" Palette

| Token | Hex | Usage |
|-------|-----|-------|
| **Peach** | `#FFB347` | Warm backgrounds, hungry mood |
| **Lavender** | `#C77DFF` | Pet UI primary, playful mood |
| **Mint Fresh** | `#A8EDCA` | Positive interactions, feed confirmation |
| **Cloud White** | `#EAF4FB` | Soft backgrounds, pop-ups |
| **Blush** | `#FFD6E7` | Scared mood, gentle alerts |
| **Butter** | `#FFF3B0` | Sleepy mood, warm sparkles |

#### Neutrals

| Token | Hex | Usage |
|-------|-----|-------|
| **App Background** | `#080C14` | Primary dark background |
| **Panel / Sidebar** | `#0D1420` | Secondary surface |
| **Card Surface** | `#111827` | Tertiary surface |
| **Primary Text** | `#E8F4FD` | Main readable text |
| **Mid Text** | `rgba(232,244,253,0.60)` | Secondary text |
| **Dim Text** | `rgba(232,244,253,0.30)` | Tertiary / decorative text |
| **Mute Text** | `rgba(232,244,253,0.14)` | Lowest contrast, labels only |

**Rule:** Never hardcode a hex value in a Composable. Always reference tokens from `:core:ui DesignTokens.kt` with namespaces `DesignTokens.Capture.*` or `DesignTokens.Pet.*`.

### Typography

Three fonts, strict role assignments:

| Font | Weight | Usage |
|------|--------|-------|
| **Fredoka One** | 400 only | Monster names, section titles, mood labels, evolution names, CTA buttons |
| **Space Mono** | 400, 700 | Stats, filter names, shader labels, PKE HUD, timestamps, badges |
| **Outfit** | 300, 400, 500, 600 | Body text, descriptions, instructions, accessibility labels |

**Forbidden:** Never use Outfit for display or Space Mono for body copy. Never mix fonts on a single element.

### Motion & Haptics

#### Capture Mode

| Animation | Duration | Haptic |
|-----------|----------|--------|
| **Proton Lock** | Hold duration | `VibrationEffect.createWaveform([0,40,60,80],[-1,80,100,255])` — wave escalation |
| **Capture Success** | 800ms (Lottie) | Single strong `VibrationEffect.EFFECT_HEAVY_CLICK` + white flash overlay |
| **Monster Escape** | 300ms screen shake | Triple pulse; radar blip fade-out |

#### Pet Mode

| Animation | Duration | Haptic |
|-----------|----------|--------|
| **Mood Transition** | 400ms crossfade | None (just animation) |
| **Feed Interaction** | 600ms particle burst | Gentle tick |
| **Evolution Ritual** | 8 seconds | Escalating waveform; strong thud at peak |

**Lottie Rules:**
- All idle animations: `iterations = LottieConstants.IterateForever`
- Triggered animations (feed, cuddle, capture burst): play once via `isPlaying` state
- All JSON files in `assets/pet/moods/` — no remote loading
- Mood swaps: never hard-cut; use `animateFloatAsState` for crossfade

---

## AR Filter System

### Architecture Overview

```
CameraX SurfaceTexture
    ↓
ARCore Session (plane/anchor detection)
    ↓
ML Kit Object Anchoring
    ↓
MediaPipe FaceLandmarker
    ↓
UniformBus (StateFlow-backed data channel)
    ↓
FilterLayerManager (ordered shader stack)
    ↓
OpenGL ES 3.0 FBO chain
    ↓
Display Surface
```

### Key Components

| Component | Purpose |
|-----------|---------|
| **FilterLayerManager** | Maintains active shader stack as ordered `List<FilterLayer>`. Stack ordered cheapest → most expensive for Tier B fallback. |
| **UniformBus** | StateFlow-backed data channel feeding real-time values (face mesh, anchor position, hold progress, lux) into GLSL uniforms. Never query MediaPipe from GL thread. |
| **Shared Vertex Shader** | One vertex shader bound once per GL session. All fragment shaders consume `vTexCoord`. Never re-bind between passes. |
| **Tier Detection** | `TierDetector` identifies device tier at startup. Tier A (6 FBO max), B (2 FBO max), C (0 GL, Lottie fallback). |

### Filter Catalog — 8 Filters

| Icon | Name | Cost | Tiers | Type |
|------|------|------|-------|------|
| ⚡ | Proton Beam Glow | 1 FBO | A/B | Touch-held, intensity driven by `uCharge` uniform |
| 🌿 | Slime Vignette | 0 FBO | A/B/C | Composited into Proton Beam shader at zero cost |
| 🌙 | Night Vision | 1 FBO | A/B | Auto-triggers at lux < 3.0; shares 1-pass slot with Proton on Tier B |
| 📡 | Ghost Radar HUD | 0 FBO | A/B/C | Compose Canvas overlay (zero GL cost); radar sweep + directional blip |
| 💚 | Ectoplasm Splatter | 1 FBO | A/B | Capture success one-shot; auto-removes after 800ms |
| 👻 | Ghost Goggles | 1 FBO | A only | MediaPipe face mesh (eye corners). Tier A only. |
| 🎯 | Proton Pack HUD Visor | 1 FBO | A only | Forehead + eye HUD overlay using landmarks 10/151/337/108 |
| 🌐 | Shared AR World | 2 FBO | A only | Multiplayer shared monster sighting via Nearby Connections (V3 scope) |

**Critical Rule:** No shader loads on an unsupported tier. Check `DeviceTier` first. Tier B: share 1-pass slot between Night Vision + Proton (Proton wins on ACTION_DOWN).

### MediaPipe Face Landmarker

**Architecture Rule:** Fragment shader never queries MediaPipe directly. FaceLandmarkDetector runs on `Dispatcher.Default`, publishes results to `UniformBus` as FloatArray, GL thread consumes via uniform uploads.

**Key Landmarks:**
- Eyes (33, 133, 362, 263) → `uEyeCorners[8]`
- Forehead band (10, 151, 337, 108) → `uForeheadBand[8]`
- Cheek-to-cheek (454, 234) → `uFaceWidth`

**Model:** Bundled as `assets/face_landmarker.task` — GPU inference via MediaPipe GPU delegate.

### Performance Budget

| Tier | Device | Max FBO | Face Mesh | Target Frame | Fallback |
|------|--------|---------|-----------|--------------|----------|
| A | Pixel 7+ | 6 | ✅ 30fps | ≤10ms | None |
| B | Pixel 6a, Samsung A54 | 2 | ❌ Disabled | ≤16ms | Share 1-pass between filters |
| C | Older / Low-RAM | 0 | ❌ Disabled | N/A | Lottie overlay + Canvas HUD |

**Key Rule:** Compose Canvas DrawScope overlays (HUD, text, simple overlays) are free on all tiers. Prefer Canvas over GL passes for non-visual-effect content.

### Shader Template — Fragment Skeleton

```glsl
precision mediump float;
varying vec2 vTexCoord;

uniform sampler2D uCameraTexture;   // CameraX SurfaceTexture
uniform vec2 uTouchOrigin;           // normalised [0..1]
uniform float uCharge;               // 0.0 → 1.0 as hold progresses
uniform float uTime;                 // elapsed seconds

void main() {
    vec4 base = texture2D(uCameraTexture, vTexCoord);
    // ... compositing logic ...
    gl_FragColor = mix(base, effect, mask);
}
```

**Privacy Hard Rule:** Filters are view-only. Camera frames processed via GL are never written to MediaStore or file. `eglSwapBuffers()` targets display surface only. No screenshot capture path exists.

---

## Pet System

### Design Heart

In the film *"If"*, imaginary friends are forgotten because people stop believing. Our system **inverts this**: the bond is designed so you can never truly lose the relationship. Bond never decreases. Monsters never die. Low stats mean sad moods, not loss.

### Stats & Decay

| Stat | Range | Decay | Critical Threshold | Raised By |
|------|-------|-------|--------------------|-----------| 
| **Hunger** | 0–100 | −5 every 4h *(impl: −4)* | < 20 → GRUMBLY | Feed |
| **Happiness** | 0–100 | −4 every 6h *(impl: −3)* | < 25 → LONELY | Play, Cuddle, Mini-games |
| **Energy** | 0–100 | Reset to 100 at midnight *(impl: −2 per cycle)* | < 20 → SLEEPY | Sleep (automatic) |
| **Spookiness** | 0–100 | No decay *(impl: +1 per cycle)* | > 85 → SPOOKED | Personality stat |
| **Trust** | 0–100 | No decay | Gates Bond computation | All positive interactions |

**Immortality Rule:** No stat reaching 0 causes irreversible consequence. Zero stat = worst mood for that stat, nothing more. WorkManager uses `ExistingPeriodicWorkPolicy.KEEP` so decay schedules survive app restarts.

### Mood Engine — 8 Moods

Evaluated top-to-bottom; first match wins:

1. **MISSING_YOU** (7+ days inactive) — HIGHEST priority *(not yet implemented)*
2. **GRUMBLY** (hunger < 20) *(implemented as `GRUMPY`)*
3. **LONELY** (happiness < 25)
4. **SLEEPY** (energy < 20)
5. **SPOOKED** (spookiness > 85)
6. **BONDED** (bond level ≥ 8) *(not yet implemented)*
7. **ECSTATIC** (all stats > 80) *(implemented as `EXCITED`: happiness > 80 && energy > 70)*
8. **CONTENT** (default healthy state)

> **Current implementation** in `GetMoodStateUseCase` has 7 moods (SLEEPY, GRUMPY, LONELY, SPOOKED, EXCITED, PLAYFUL, CONTENT). MISSING_YOU and BONDED are specified here but not yet wired up.

Each mood has a Lottie animation file (`idle_happy_loop.json`, etc.). Mood swaps crossfade over 400ms via `animateFloatAsState`.

### Bond Engine — 0 to 10

Bond progresses through interaction history, never decreases:

| Level | Label | Unlocked Feature | Message |
|-------|-------|------------------|---------|
| 0–2 | Wary | Feed, Watch | "[Name] is still getting used to you." |
| 3–4 | Curious | + Play, Name assignment | "[Name] is starting to trust you!" |
| 5–6 | Friendly | + Cuddle, Habitat customisation | "[Name] loves hanging out with you." |
| 7 | Close Friend | + Secret ability unlocked | "You and [Name] have a special bond." |
| 8–9 | Lifelong | + BONDED mood state | "[Name] would follow you anywhere." |
| 10 | Soul Bond | Shared evolution burst | "[Name] and you are forever friends 💜" |

**Computation:**
- Trust score: 60% of bond (max 60 pts) — `pet.trust * 0.6`
- Interaction score: 40% (max 40 pts) — feed (15%), play (20%), cuddle (25%), consecutive days active (40%)
- Raw 0–100 → 0–10 level via division

### Evolution Ritual — 3 Stages

Unskippable 8-second ritual triggered when Bond thresholds met. State machine resumable on app kill.

#### Stages

1. **Hatchling** (Stage 1) — Bond 0. Wary, spooky, high energy. Round, huddled.
2. **Familiar** (Stage 2) — Trust ≥ 40. Personality emerging. Colour saturation increases.
3. **Companion** (Stage 3) — Trust ≥ 80 + Bond ≥ 4. Full personality. Unique ability. Vibrant, confident.

#### 8-Second Ritual Sequence

| Time | Phase | Visual | Haptic |
|------|-------|--------|--------|
| 0–2s | **Ghostbusters Surge** | Proton green floods screen. PKE meter animation. | Escalation waveform |
| 2–4s | **Energy Peak** | White flash at 3s. Monster silhouette visible. | (Same as capture success) |
| 4–6s | **"If" Bloom** | Ghostbusters fades. Lavender aura, peach light, mint sparkles. Soundtrack shift. | (Gentle) |
| 6–8s | **Reveal** | New form revealed with idle_ecstatic_loop.json. Name card fades in. Confetti from Canvas. | Triple gentle pulse |

**Rule:** Ritual timing runs always (8s), but animation complexity drops if user enables Reduce Motion.

### Pet Room UI

Spookiness-adaptive palette. High-Spookiness monsters live in dark, proton-green lit rooms. Low-Spookiness live in peach-lavender "If" world.

**Layout Structure:**
1. Ambient particle system (Canvas DrawScope, 0 GL cost)
2. Monster Lottie (centred, mood-driven animation)
3. Stat bars (top of screen, Space Mono readouts)
4. Interaction tray (bottom, Fredoka One labels, 48×48dp+ buttons)

**Background Adaptive Rules:**
- Low Spookiness (< 40) → Peach walls, Lavender glow, soft cushions, butter-yellow warm lighting
- High Spookiness (> 60) → Dark containment unit, proton green ambient, PKE furniture, mystery particles

---

## Data Model

### 7 Core Entities

| Entity | Table | Purpose | Retention |
|--------|-------|---------|-----------|
| **PetEntity** | `pets` | Live stat snapshot (all 5 stats + bond + evolution stage) | Forever |
| **MoodHistoryEntity** | `mood_history` | Append-only mood transitions + duration. Feeds parental dashboard charts. | 90 days |
| **DailyStatSnapshotEntity** | `daily_snapshots` | End-of-day stat capture for trend graphs. Avoids expensive queries at render. | 365 days |
| **EvolutionHistoryEntity** | `evolution_history` | Immutable log of every stage transition. Legacy display. | Forever |
| **EvolutionRitualStateEntity** | `evolution_ritual_state` | Resumable state machine for 8-second ritual. Survives app kill. | Until completion |
| **InteractionLogEntity** | `interaction_log` | Append-only audit trail (feed, play, cuddle, etc.). Capped at 1000 rows/pet. | 1000 rows cap |
| **MonsterEntity** | `monsters` | Archetype catalogue. Display names, rarity, spawn bias. | Forever |

**Encryption:** All Room databases use SQLCipher AES-256. Keys sourced from Android KeyStore hardware-backed store via `:core:security`.

**COPPA Rule:** MonsterEntity.toFirestoreMap() must never include `captureLatLng` or any GPS data.

---

## Code Rules

### ✅ Always Do

- **KDoc on every public class, function, and entity** — explain *why*, not just what
- **One responsibility per class** — UseCases do one thing, named `[Verb][Noun]UseCase`
- **Repository pattern** — all DB writes go through Repository, never from ViewModel
- **@Transaction for multi-table writes** in Room
- **StateFlow > LiveData** throughout
- **48×48dp minimum touch targets** on all interactive Composables
- **Modifier.semantics { contentDescription = "..." }** on icon-only buttons
- **Tier checks before loading any shader** — read `DeviceTier` from `TierDetector`

### ❌ Never Do

- Hardcode hex colours — always use `DesignTokens.*`
- Mutate `PetEntity` from Composable or ViewModel directly
- Query MediaPipe from GL thread — use `UniformBus`
- Load a shader on Tier C — check tier first, use Lottie fallback
- Write camera frames to MediaStore (privacy rule)
- Use `LiveData` in new code
- Implement death mechanic or irreversible stat loss
- Skip `ArCoreApk.checkAvailability()` call at launch

### Naming Conventions

| Pattern | Example | Rule |
|---------|---------|------|
| UseCase | `FeedPetUseCase` | Verb + Noun. Single public method. |
| ViewModel UiState | `PetUiState` | Data class, immutable, sealed hierarchy for loading/success/error. |
| Shader file | `proton_beam_frag.glsl` | Snake case. Suffix `_vert` / `_frag`. In `assets/shaders/`. |
| Lottie file | `idle_happy_loop.json` | Snake case. Prefix `idle_` (loops) or `trigger_` (one-shots). |
| Design Token | `DesignTokens.Capture.Lime` | Namespace by mode, then semantic name. |
| WorkManager Tag | `pet_decay_HUNGER` | Prefix `pet_`, suffix is decay type in caps. |

---

## Safety & Compliance

### Privacy & COPPA

**COPPA applies to all users under 13.** Verifiable parental consent required before any data collection.

#### Consent Flow (3 Phases)

1. **Parent Identification** — Parent email (hashed SHA-256 + app salt, never plain) + full-screen gate
2. **Verifiable Consent** — Choose: email verification, knowledge question, or micro-charge ($0.01 refunded)
3. **Consent Acknowledgement** — Plain-language privacy summary + explicit checkboxes for data collection & analytics (default unchecked) + 4-digit PIN for purchase gates

**Data Erasure:** Within 30 days of `DELETE /user/{uid}/data` request, all Room records, Firestore documents, Firebase Auth account, and Storage files deleted. `DataErasureAuditEntity` (no PII, just uid hash + timestamp) retained 7 years for legal proof.

### Age Segmentation — 3 Buckets

| Bucket | Age | Key Restrictions |
|--------|-----|------------------|
| **A** | 2–5 | No text input. Single-tap only. 72×72dp icons. Audio cues for all actions. No social, no IAP visible. No AR mode (Lottie-only). |
| **B** | 6–8 | Limited text (name only, parent-gated). 48×48dp targets. AR capture enabled. Mini-games 1–2. No friend lists. IAP behind PIN. |
| **C** | 9–12 | Text for monster nicknames. All 3 mini-games. Optional friend list (per-friend consent). Trading (parent approval per trade). V2 face filters. |

**Gate Examples:**
- AR Camera Mode → `ageBucket ≥ B`
- Face Filters V2 → `ageBucket = C`
- Monster Nickname → `ageBucket ≥ B + parentApproved`
- Social / Friends → `ageBucket = C + parentConsentPerFriend`

### Accessibility

**Target:** WCAG 2.1 Level AA. Android Accessibility Test Framework (ATF) must pass zero critical errors before release.

#### Contrast Ratios (WCAG 2.1 AA minimum 4.5:1)

| Pair | Ratio | Status |
|------|-------|--------|
| `#E8F4FD` on `#080C14` | 16.8:1 | ✅ |
| `#7FFF00` on `#080C14` | 14.2:1 | ✅ |
| `#C77DFF` on `#080C14` | 5.8:1 | ✅ |
| `#FFB347` on `#080C14` | 6.2:1 | ✅ |
| `rgba(232,244,253,0.30)` on `#080C14` | 2.1:1 | ❌ (Decorative only) |

**Forbidden:** `var(--dim)` and `var(--mute)` tokens must never carry readable text. Decorative labels and timestamps only. Use `var(--mid)` (5.0:1) or `var(--text)` (16.8:1) minimum for information-carrying text.

#### Font Scaling

| Use Case | Base Size | Max Scale | Behaviour |
|----------|-----------|-----------|-----------|
| Body copy (Outfit) | 14sp | 2.0× | Text wraps; card height expands. Never truncate. |
| Stat labels (Space Mono) | 8sp → 10sp min | 1.5× | Use `minFontSize` — never below 10sp. |
| Monster name (Fredoka One) | 28sp | 1.5× | Layout reflows; wraps if needed. |
| Badge text (Space Mono caps) | 8sp | 1.0× (fixed) | Decorative — locked scale. |

#### TalkBack / Screen Reader

- **Every icon-only button:** Include `contentDescription` with context
- **Lottie mood animations:** Announce on mood change via `accessibilityManager.announce()`
- **AR anchor blips:** Announce "Monster detected nearby"
- **Stat values:** "Current hunger: 85 out of 100"

#### Reduce Motion & Alternative Input

- Respect `LocalReduceMotion.current`. When true: disable particles, instant Lottie crossfades, skip evolution ritual animation (timing still runs)
- Capture action available via volume-up button (configurable)
- All pet interactions reachable via switch access
- No action requires hold > 3s (configurable to 5s for motor-impaired)

### Parental Controls & Monetization

**Philosophy:** No behavioural ads. No dark patterns. No loot boxes. Revenue: cosmetic IAP + optional subscription tier.

#### Purchase Gate Flow

1. Child taps cosmetic item
2. Full-screen "Ask grown-up for parent PIN" modal
3. 4-digit input, rate-limited 5 attempts per 10 min
4. Parent sees item, price, description + explicit "Buy Now"
5. Play Store Billing API invoked; receipt emailed to parent
6. Item delivered; celebration animation shown

**Rules:**
- All IAP cosmetic only — never pay-to-win
- Parental PIN required always
- No urgency language ("Only 1 left!", countdowns)
- Loot boxes banned; predictable outcome purchases only
- Behavioural / targeted ads banned regardless of consent

### Analytics & Telemetry

**Default: OFF for all child accounts.** No analytics event fires until parent explicitly opts in via Parent Dashboard.

#### Allowed Events (Allowlist Enforced)

| Event | Allowed Fields | Retention |
|-------|---|-----------|
| `session_start` | `uid_hash`, `session_id`, `age_bucket`, `device_tier` | 90 days |
| `session_end` | `session_id`, `duration_seconds` | 90 days |
| `capture_attempt` | `session_id`, `monster_archetype`, `rarity` | 90 days |
| `capture_success` | `session_id`, `monster_archetype`, `hold_duration_ms` | 90 days |
| `pet_interaction` | `session_id`, `interaction_type`, `bond_level_before` | 90 days |
| `evolution_triggered` | `session_id`, `evolution_stage`, `days_since_capture` | 90 days |
| `filter_applied` | `session_id`, `filter_id`, `device_tier` | 90 days |
| `crash_report` | Stack trace (sanitised), `device_tier`, app version | 30 days |

#### PII Ban (Never Include)

- Real name, email, phone number
- Raw Firebase UID (hash with SHA-256 + salt only)
- Face landmark coordinates
- Camera frames or screenshots
- Device GPS location or IP address
- Monster nickname (child-entered text)
- Parent email (even hashed) in analytics

---

## Testing & QA

### Child Usability Testing

Before each major release, run structured sessions with real children (with parental consent):

| Bucket | Cohort | Key Tasks | Success Metric |
|--------|--------|-----------|---|
| A (2–5) | 5 children + parent | Find monster, tap interact, recognise happy mood | ≥ 80% completion without assist |
| B (6–8) | 6 children | Capture, feed, understand mood change | ≥ 80% without hint; ≤ 2 errors/session |
| C (9–12) | 6 children | Full capture, 3 mini-games, name assignment | ≥ 90% without hint; subjective enjoyment ≥ 4/5 |

### Automated Test Requirements

#### Accessibility

- ATF integrated in CI — zero critical errors gate merges
- TalkBack walkthrough script: Pet Room → Feed → Mood change → Capture trigger
- Contrast ratio assertions via `AccessibilityChecks.enable()` in Espresso
- Touch target size check: all interactive ≥ 48×48dp
- Font scale 2.0 smoke test: no text truncation

#### Performance

- **Tier B frame time ≤ 16ms** (p95) for 2-filter stack — Macrobenchmark
- **Tier A face-filter ≤ 10ms** (p95) — Macrobenchmark
- **Pet Room startup:** First Lottie frame ≤ 300ms cold start
- **WorkManager decay:** Jobs survive app restart in Robolectric
- **Memory:** No OOM in 1-hour session on 2GB RAM device

### Test Coverage Requirements

| Module | Coverage | Rationale |
|--------|----------|-----------|
| `GetMoodStateUseCase` | 100% | Pure function — all 8 moods + priority edges |
| `ComputeBondLevelUseCase` | 100% | Core emotional feature — regression is high-impact |
| `AnalyticsGate` | 100% | PII/consent gate — must never fire without consent |
| `PetDecayWorker` | 90%+ | Decay must not over-decrement |
| `FilterLayerManager` | 80%+ | Tier checks, stack ordering, FBO budget |
| All other UseCases | 80%+ | Single-responsibility — straightforward to test |

### Privacy & Security Audit Checklist

| # | Check | How | Owner |
|---|-------|-----|-------|
| 1 | No camera frames to MediaStore | Run capture session; verify 0 new rows in `content://media/external/images` | AR team |
| 2 | SQLCipher active on all DBs | Confirm DB file unreadable without key via `sqlite3` | Platform |
| 3 | KeyStore key rotation | Keys rotate on day 90; test via mocked date | Platform |
| 4 | Analytics gate functional | `analyticsOptIn = false` → zero Firebase events | Platform |
| 5 | No PII in analytics | Event schema diff against ban checklist | Platform |
| 6 | Parental PIN rate-limiting | 5 wrong PINs → 10min lockout; 6th rejected | Platform |
| 7 | Data erasure | Trigger erasure; verify Room + Firestore + Storage emptied in < 5 min | Backend |
| 8 | Dependency security | OWASP check in CI; no high/critical CVEs | Platform |

---

## Quick-Start Build Order

1. **`:core:ui`** → DesignTokens, AppTheme, shared Composables
2. **`:core:data`** → Room schema (7 entities above)
3. **`:feature:ar`** → CameraX session setup
4. **`:feature:filters`** → FilterLayerManager + first 2 shaders (Proton Beam, Night Vision)
5. **`:feature:pet`** → PetEntity + WorkManager decay
6. **`:feature:pet`** → PetRoomScreen Compose UI
7. **`:feature:onboarding`** → Parent consent gate + age bucket selection
8. **`:feature:parental`** → Parent Dashboard (analytics view, controls)

Each module depends on the previous. Never skip ahead.

---

## Reference

**Full Interactive Guide:** See `docs/design-guide.html` for visual mockups and interactive component specifications.

**For detailed implementation specs** (GLSL shader templates, Firebase Firestore schema, exact API endpoints, Lottie animation specs), refer to the HTML guide.
