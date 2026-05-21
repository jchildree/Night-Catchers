# 👻 Monster Catcher — CLAUDE.md
> Android · Kotlin · Jetpack Compose · ARCore · Firebase
> Design pillars: Ghostbusters capture ritual × "If" creature warmth × Snapchat-style AR filters

---

## 🗺️ What This Project Is

Monster Catcher is a child-facing Android app where kids use augmented reality to find and capture monsters
hiding under beds and in closets. Captured monsters become living Tamagotchi-style companions in a modernised
pet system inspired by *Ghostbusters* (capture mechanics, PKE-meter UI, containment aesthetic) and *"If"*
(soft, expressive, emotionally rich creature design).

**Core loops:**
1. **Scan → Capture** — ARCore plane detection + Snapchat-style GLSL filter pipeline
2. **Bond → Evolve** — Modernised Tamagotchi pet system; 5 stats, 7 moods, 3 evolution stages
3. **Play Mini-Games** — Compose Canvas games that feed directly into pet stats

**Audience:** Children (primary user) + Parents (gated controls, purchases, oversight)

---

## 🏗️ Architecture at a Glance

```
Presentation  →  ViewModel  →  UseCase  →  Repository  →  Room (source of truth)
                                                        ↘  Firestore (sync target)
```

- **Offline-first.** Room is always the source of truth. ViewModels and use cases read from Room only.
  Firestore is a write-behind sync target, debounced, conflict-resolved by `updatedAt` server timestamp.
- **Clean MVVM + Repository.** No business logic in Fragments or Composables. Ever.
- **Hilt DI.** All dependencies injected — nothing instantiated manually inside feature modules.
- **Kotlin Coroutines + Flow.** UI state flows downward; events flow upward. `StateFlow` for UI state,
  `SharedFlow` for one-shot events (navigation, toasts, haptics).

---

## 📦 Module Structure

```
:app
:core:ui          — DesignTokens, shared Composables, theme
:core:data        — Room DB, Firestore sync, Repository impls
:core:arcore      — ARCore bridge, PlaneDetector, TierDetector, Lottie fallback
:core:permissions — Runtime permission helpers
:core:notifications — FCM, WorkManager notification dispatch

:feature:camera       — CameraX preview, torch, orientation
:feature:filters      — GLSL filter pipeline, FilterLayerManager, ShaderProgramCache
:feature:capture      — Capture ritual controller, haptics, FrameExporter
:feature:spawn        — SpawnWeightEngine, monster placement logic
:feature:vault        — Monster Vault UI (containment grid)
:feature:pet          — Tamagotchi core loop, PetViewModel, UseCase layer
:feature:pet-room     — Pet Room UI, HabitatThemeManager
:feature:mood-engine  — Pure-function MoodState machine (fully unit-tested)
:feature:mini-games   — All Compose Canvas mini-games (see § Mini-Games below)
:feature:evolution    — EvolvePetUseCase, Lottie transformation sequences
:feature:auth         — Firebase Auth, parent onboarding, child profile setup
:feature:parent       — Parental dashboard, PIN gate, time budgets
:feature:achievements — Monster Dex, streaks, shareable cards (V2)
:feature:social       — ShareCompat capture clips, achievement cards (V2)
```

---

## 🎮 Mini-Games — Design & Structure

> Mini-games are the **fastest path to stat boosts**. Each one is an isolated Compose Canvas game loop
> communicating stat deltas back to `:feature:pet` via `MiniGameScoreMapper`. No mini-game writes
> directly to Room — it fires a result event and the pet UseCase layer handles persistence.

### Architecture Rules for Mini-Games

```
MiniGameScreen (Composable)
  └── MiniGameViewModel (coroutine game loop, timer, score)
        └── MiniGameScoreMapper → FeedPetUseCase / HappilyUseCase / TrustUseCase
```

- **Each game lives in its own file** inside `:feature:mini-games`. One game = one `Screen`, one `ViewModel`,
  one `GameState` data class. No shared mutable state between games.
- **Game loop = coroutine.** Use `while (isActive)` inside `viewModelScope.launch` with a fixed-timestep
  `delay(16L)` tick (≈60fps). Canvas redraws are driven by `animationFrameMillis` from Compose's
  `LaunchedEffect`, not from the ViewModel tick.
- **Energy gate.** Every game checks `pet.energy >= game.energyCost` before launching. If energy is too low,
  show a friendly "your monster is sleepy 😴" message — never a hard lock or error state.
- **Daily variety rotation.** The game featured first each day = `dayOfYear % numberOfGames`. All games
  are always accessible after the first is completed. This ensures all stat categories receive input weekly.
- **Personality bias.** Spawn the recommended game first based on monster personality:
  - `Shy` → Hide & Seek (Spook Tag)
  - `Bold` → Food Toss
  - `Lazy` → Cuddle Storm (lowest energy cost)
  - `Curious` → any — rotate freely
  - `Mischievous` → Spook Tag (Spookiness path)

### The Mini-Game Roster

#### 🍖 Food Toss (`FoodTossGame.kt`)
> Feed interaction entry point. Drag-to-aim food at the monster's open mouth.

| Property | Value |
|---|---|
| Input | Drag gesture (DragGesture on Canvas) |
| Rounds | 3 throws per session |
| Hit detection | Mouth bounding box (Compose Canvas `Rect`) |
| Energy cost | –10 |
| Stat rewards | Hunger +25, Happiness +10 |
| Design tone | Ghostbusters "ghost trap" aim-and-throw ritual |

**Implementation notes:**
- Monster mouth bounding box is defined in the `MonsterArchetype` config, not hardcoded.
- Miss animation: food item bounces off and rolls away (simple `Offset` tween).
- Hit animation: monster chomps + heart particle burst (Lottie one-shot).
- Designed for one-handed bedtime play — drag arc starts from bottom 40% of screen only.

#### 👻 Spook Tag / Hide & Seek (`SpookTagGame.kt`)
> Raises Spookiness + Happiness. The more you play, the spookier (and prouder) the monster becomes.

| Property | Value |
|---|---|
| Input | Tap gesture (3 door containers) |
| Rounds | 5 rounds per session |
| Difficulty | Door swap speed increases each round (coroutine delay: 1200ms → 600ms) |
| Energy cost | –20 |
| Stat rewards | Spookiness +15, Happiness +20 |
| Design tone | Ghostbusters "ghost hiding" meets *"If"* peek-a-boo warmth |

**Implementation notes:**
- Doors are `AnimatedVisibility`-wrapped Compose containers with a slide-in/out reveal.
- Correct door is determined by `Random.nextInt(3)` each round — seeded from system clock, not pet ID,
  to prevent a child memorising a pattern.
- Wrong tap: monster peeks out from behind the wrong door with a giggle animation.
- Round timer displayed as a shrinking progress arc (Canvas `drawArc`) — no countdown number
  (reduces anxiety for younger children).

#### 💚 Cuddle Storm (`CuddleStormGame.kt`)
> Fastest Trust builder. Tap the monster rapidly for 10 seconds.

| Property | Value |
|---|---|
| Input | Rapid tap anywhere on monster (tap target = 120dp radius circle) |
| Duration | 10 seconds fixed |
| Energy cost | –15 |
| Stat rewards | Trust +5, Happiness +30, Spookiness –10 |
| Design tone | Pure *"If"* — soft, warm, no competitive pressure |

**Implementation notes:**
- Each tap triggers `VibrationEffect.createOneShot(20ms, 80)` micro-haptic + Lottie heart splash.
- Tap count displayed as a live score — encourages repeat engagement ("beat your last score").
- Spookiness decrease is intentional: Cuddle Storm makes monsters softer and more "If"-like over time.
  High-Spookiness paths (Lurkmancer, Voidwalker) should discourage over-cuddling via personality tooltips.
- No tap rate cap — mash away. The 10-second window is the natural limiter.

#### 🌙 Dream Drift (`DreamDriftGame.kt`) — V2
> Bedtime-only game. Guide the monster through a dream cloud maze.

| Property | Value |
|---|---|
| Availability | Unlocked after 9pm (local device time) |
| Input | Tilt (SensorManager accelerometer) or swipe (accessibility mode) |
| Duration | 60 seconds or maze completion |
| Energy cost | –5 (lowest — it's a wind-down game) |
| Stat rewards | Energy +20 (restores energy!), Trust +3 |
| Design tone | Ambient, dreamy *"If"* colour world — peach, lavender, cloud white |

**Implementation notes:**
- Only mini-game that *restores* energy rather than depleting it.
- Maze is procedurally generated per session using a simple recursive backtracker seeded by `LocalDate.now()`.
  Same maze per calendar day — children can discuss it with friends.
- Tilt controls use `SensorManager.SENSOR_DELAY_GAME`. Accessibility fallback: four-directional swipe.
- Music: ambient lullaby loop (short `.ogg`, royalty-free). Fades in over 2 seconds; fades out on exit.

### MiniGameScoreMapper

```kotlin
// Maps raw game results → stat delta events consumed by PetStatUseCases
object MiniGameScoreMapper {

    fun map(result: MiniGameResult): List<StatDelta> = when (result.gameId) {
        GameId.FOOD_TOSS    -> listOf(hunger(+25), happiness(+10))
        GameId.SPOOK_TAG    -> listOf(spookiness(+15), happiness(+20))
        GameId.CUDDLE_STORM -> listOf(trust(+5), happiness(+30), spookiness(-10))
        GameId.DREAM_DRIFT  -> listOf(energy(+20), trust(+3))
    }

    // Clamps are enforced inside the UseCase, not here — mapper is pure.
    private fun hunger(v: Int)      = StatDelta(Stat.HUNGER, v)
    private fun happiness(v: Int)   = StatDelta(Stat.HAPPINESS, v)
    private fun spookiness(v: Int)  = StatDelta(Stat.SPOOKINESS, v)
    private fun trust(v: Int)       = StatDelta(Stat.TRUST, v)
    private fun energy(v: Int)      = StatDelta(Stat.ENERGY, v)
}
```

---

## 📷 Snapchat-Style AR Filter System

### Filter Pipeline Overview

```
CameraX Preview
  → ARCore Plane Detection (floor / wall / bed)
  → ML Kit Object Anchoring
  → MediaPipe Face Mesh (478 points)
  → FilterLayerManager (GLSL shader stack)
      → Off-screen FBO → SurfaceTexture composite
  → Compose UI overlay (HUD, meters, buttons)
  → Lottie fallback (Tier C devices — silent, no error shown to child)
```

### Device Tiers

| Tier | API | ARCore | GL ES | Behaviour |
|------|-----|--------|-------|-----------|
| A | 34+ | Full | 3.0 | Full GLSL pipeline, multi-FBO, face mesh |
| B | 31–33 | Lite | 2.0 | Max 2 simultaneous full-screen shader passes |
| C | 26–30 | None | — | Lottie sprite overlays, no GL pipeline |

**Rule:** Degradation is always silent. Children never see error messages about AR capability.
`TierDetector` runs at app start; `FilterLayerManager` configures itself automatically.

### Active Filters (Capture Mode)

| Filter | Trigger | GL Cost |
|---|---|---|
| Ghost Radar HUD | Always active (Capture mode) | Compose overlay only |
| Night Vision | Auto when lux < 50 | Single pass, green LUT |
| Proton Beam Glow | Touch + hold | Additive blend, 1 FBO |
| Slime Vignette | Monster detected | Radial gradient shader |
| Ectoplasm Splatter | Capture success | One-shot 800ms, then cleared |
| Ghost Goggles | Face detected (Tier A) | MediaPipe-anchored |
| Proton Pack HUD Visor | Face detected (Tier A) | Forehead + eye anchors |

### Filter Layer Rules

- **Stack order:** Compose overlays first (cheapest) → single-pass shaders → multi-FBO last.
- **Max simultaneous full-screen passes:** 2 on Tier B, unlimited on Tier A.
- Filters are independent `FilterLayer` objects added/removed without restarting the GL loop.
- `ShaderProgramCache` compiles shaders once at session start, never at runtime during capture.
- Frame budget: ≤ 16ms on Pixel 6a. Profile with Android GPU Inspector before adding new passes.

---

## 🐾 Modernised Tamagotchi Pet System

### Core Philosophy

**No death. No punishment. No stress.**

Low stats trigger a "Missing You 💔" mood state — not death, not a penalty. The pet safely
pauses when the screen-time limit expires. Every mechanic rewards engagement, never punishes absence.

### Stat Engine

| Stat | Range | Decay Rate | Decay Schedule |
|---|---|---|---|
| Hunger | 0–100 | –8/hr | WorkManager `PeriodicWorkRequest` (15min min interval) |
| Happiness | 0–100 | –5/hr | Same worker |
| Energy | 0–100 | –4/hr daytime, –2/hr night | Time-of-day aware |
| Spookiness | 0–100 | ±varies | Mini-games, interactions only |
| Trust | 0–100 | No decay | Only increases; resets on release |

### Mood State Machine

```
             ┌─────────────────────────────────────┐
             │         GetMoodStateUseCase          │
             │  (pure function — 100% unit tested)  │
             └──────────────┬──────────────────────┘
                            │ 5 stats in → MoodState enum out
              ┌─────────────▼─────────────────────────────┐
              │  Happy │ Hungry │ Tired │ Playful │ Scared │
              │  Grumpy │ Proud  │  (Missing You — no net) │
              └───────────────────────────────────────────┘
```

Each mood triggers: unique idle Lottie animation + ambient sound + room colour wash.
Personality traits (`Shy`, `Bold`, `Curious`, `Lazy`, `Mischievous`) bias mood thresholds per archetype.

### Bond Engine (5 Stages)

| Stage | Bond Score | Name | Unlock |
|---|---|---|---|
| 0 | 0–9 | Stranger | Basic feed/play |
| 1 | 10–29 | Acquaintance | Cuddle interaction |
| 2 | 30–49 | Friend | Spook Training |
| 3 | 50–79 | Companion | Evolution Stage 2 |
| 4 | 80–99 | Soul Bond | Evolution Stage 3, Legacy Name prompt |

### Evolution System

All 7 archetypes follow a 3-stage evolution gated by Trust score:

| Archetype | Stage 1 | Stage 2 (Trust 40) | Stage 3 (Trust 80) |
|---|---|---|---|
| Bed Lurker | Lurkling 👾 | Lurkmire 🌀 | Lurkmancer 👑 |
| Closet Creep | Peekling 👀 | Creepling 🚪 | Voidwalker 🌑 |
| Shadow Wisp | Mist 💨 | Shimmer 🌫️ | Eclipsis 🌘 |
| Dust Bunny | Flufflet 🐰 | Puffball ☁️ | Cumulus 🌤️ |
| Sock Thief | Pilferer 🧦 | Filcher 🎭 | Kleptomancer 🎪 |
| Door Rattler | Knocker 🚪 | Thumper 💢 | Boomstrike ⚡ |
| Under-Mattress Mole | Diglet 🌱 | Burrower 🕳️ | Underking 🏔️ |

Evolution uses an 8-second Lottie transformation sequence. `EvolvePetUseCase` increments stage and
unlocks abilities inside a Room `@Transaction`. Compose cross-fades mask the transition.

---

## 🔒 Security & COPPA

**Two-tier auth is non-negotiable — never weaken it.**

- **Parents** authenticate via Firebase Auth (Google Sign-In or Email/Password).
- **Children** never authenticate. Child profiles are locally-generated UUIDs linked to parent UID.
- All Firestore writes from a child session carry the **parent's** Firebase ID token.
- Security rules enforce `request.auth.uid == parentUID` on every Firestore path.
- PIN stored as `bcrypt` hash (cost=12) in `EncryptedSharedPreferences`. 5-failure lockout.
- `captureLatLng` stored in Room only — Firestore security rules **block** any write containing GPS data.
- No child email, DOB, or phone ever collected. No advertising ID on child session. COPPA by design.

**Encryption layers:**

1. Room DB → SQLCipher (AES-256-CBC)
2. DataStore → `EncryptedFile` (AES-256-GCM)
3. Firebase Storage → AES-256 at rest (Google KMS, per-UID key)
4. Network → TLS 1.3 minimum, certificate pinning via OkHttp

---

## 🧪 Testing Strategy

### What runs on AVD emulator
- JVM unit tests (`./gradlew test`)
- Room DB, Firestore sync via Firebase Emulator Suite
- UI / navigation (Compose testing)
- WorkManager stat decay logic
- `GetMoodStateUseCase` — must be 100% unit test coverage (pure function)
- `MiniGameScoreMapper` — 100% unit test coverage (pure function)

### What requires physical device (Pixel 6a preferred)
- ARCore plane detection
- MediaPipe face mesh
- CameraX preview
- OpenGL ES 3.0 shaders
- Haptics (`VibrationEffect`)
- Tilt sensor (`SensorManager`) — Dream Drift game

### Key commands

```bash
# Firebase emulators
firebase emulators:start

# Launch AVD
emulator -avd <your_avd_name>

# Run all unit tests
./gradlew test

# Install debug build to connected device
./gradlew installDebug

# Check connected devices
adb devices
```

---

## 🎨 Design Language

**Dual palette:** Capture mode uses Ghostbusters / PKE-meter aesthetics (lime `#7FFF00`, cyan `#00F5FF`,
dark backgrounds). Pet Room uses the warm *"If"* palette (lavender `#C77DFF`, peach `#FFB347`,
mint `#A8EDC8`, cloud white).

**Fonts:** Fredoka One (display/headings) · Space Mono (labels/code) · Nunito (body)

**Motion principles:**
- Captures: punchy, energetic — short sharp curves, haptic punctuation
- Pet room: slow, breathing — ease-in-out, never abrupt
- Transitions: 300ms cross-fade between capture and pet room modes

**Accessibility:**
- TalkBack-compatible content descriptions on all interactive elements
- Colour-blind modes available (deuteranopia, protanopia, tritanopia)
- Haptic redundancy — all audio cues have a haptic equivalent
- Gentle Mode: disables all jump-scare animations for anxious children
- Mini-game tilt controls always have a swipe fallback

---

## ⚠️ Hard Rules

These are invariants. Do not work around them.

1. **Room is the source of truth.** Never read UI state directly from Firestore.
2. **No child ever authenticates.** `childUID` is always a local UUID, never a Firebase Auth UID.
3. **No stat delta outside the UseCase layer.** `MiniGameScoreMapper` returns deltas; it does not write.
4. **AR degradation is always silent.** Never show an error to a child about missing AR capability.
5. **No death mechanic.** Stat floor is "Missing You" mood — not game over, not deletion.
6. **PIN gate every parental action.** bcrypt + BiometricPrompt. No exceptions.
7. **No GPS data to Firestore.** `captureLatLng` lives in Room only. Firestore rules enforce this.
8. **Monetisation is cosmetics-only.** No ads, no loot boxes. All IAP PIN-gated to parents.
9. **Soft-delete pattern for releases.** Set `isReleased = true`. 30-day recovery window for parents.
10. **Mini-game results go through `MiniGameScoreMapper` → UseCase.** Never write stats directly from a game ViewModel.

---

## 📁 Key File Locations

```
app/src/main/
  java/com/monstercatcher/
    core/
      data/          — Room entities, DAOs, Firestore sync workers
      ui/            — DesignTokens.kt, shared Composables
      arcore/        — ArSessionManager, TierDetector, PlaneDetector
    feature/
      camera/        — CameraViewModel, CameraScreen
      filters/       — FilterLayerManager, ShaderProgramCache, GLSL assets
      capture/       — CaptureRitualController, HapticEngine, FrameExporter
      pet/           — PetViewModel, PetRepository, stat UseCases
      mini_games/
        food_toss/   — FoodTossGame.kt, FoodTossViewModel.kt
        spook_tag/   — SpookTagGame.kt, SpookTagViewModel.kt
        cuddle_storm/— CuddleStormGame.kt, CuddleStormViewModel.kt
        dream_drift/ — DreamDriftGame.kt, DreamDriftViewModel.kt (V2)
        shared/      — MiniGameScoreMapper.kt, MiniGameResult.kt, GameId.kt
      pet_room/      — PetRoomScreen, HabitatThemeManager
      mood_engine/   — GetMoodStateUseCase.kt (pure function, no side effects)
      evolution/     — EvolvePetUseCase.kt, EvolutionAnimController.kt
      auth/          — FirebaseAuthManager, ChildProfileSetup
      parent/        — PinGateViewModel, ParentalDashboard, TimeBudgetManager
    res/
      raw/           — GLSL shader files (.glsl), Lottie JSON files (.json)
```

---

## 🌿 Green is the colour of trust.

The lime green `#7FFF00` PKE-meter colour throughout capture mode is the visual anchor for
"you are in control — you are the Ghostbuster here." Use it consistently for success states,
progress indicators, and capture confirmation. It's also the colour of Trust stat fill bars
in the Pet Room. Intentional through-line.

---

*Last updated: auto-generated from project documentation v2.1*
*Ghostbusters × "If" × ARCore × Compose Canvas · Monster Catcher Android*

---

## Agent skills

### Issue tracker

Issues live in GitHub Issues (`gh` CLI). See `docs/agents/issue-tracker.md`.

### Triage labels

Default canonical label strings (needs-triage, needs-info, ready-for-agent, ready-for-human, wontfix). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context: one `CONTEXT.md` + `docs/adr/` at the repo root. See `docs/agents/domain.md`.
