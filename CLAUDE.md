# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Module Map

```text
app/                          # Shell activity, Hilt entry point, nav host
core/
  common/                     # Result<T>, DeviceTier, AppDispatchers, extensions
  data/                       # Room + SQLCipher, Firestore sync, repository impls
  domain/                     # Use cases, domain models (pure Kotlin, no Android deps)
  network/                    # Retrofit/OkHttp, Firebase wrappers
  security/                   # KeyStore, BiometricPrompt, bcrypt PIN
  ui/                         # Design system, theme, shared Composables
  testing/                    # Fakes, test utilities
feature/
  ar/                         # CameraX pipeline, ARCore, MonsterSpawnEngine
  capture/                    # Capture ritual UI + CaptureViewModel state machine
  dex/                        # Monster Dex, achievements, shareable cards
  filters/                    # FilterLayerManager, ShaderProgramCache, GLSL shaders
  onboarding/                 # First-run flow, permission requests
  parental/                   # PIN gate, parental dashboard
  pet/                        # Pet stats, mood engine, mini-games, workers
  vault/                      # Monster collection, containment unit
build-logic/                  # Convention plugins (android-library, hilt, compose, etc.)
```

## Build

```bash
./gradlew build                                                        # compile all modules
./gradlew test                                                         # all unit tests
./gradlew :core:domain:test                                            # single module's tests
./gradlew :core:domain:test --tests "com.nightcatchers.GetMoodStateUseCaseTest"  # single test class
./gradlew detekt                                                       # static analysis
./gradlew ktlintCheck                                                  # lint check
./gradlew ktlintFormat                                                 # auto-fix lint
```

Build variants: `debug` uses `DebugAppCheckProviderFactory`; `release` uses `PlayIntegrityAppCheckProviderFactory`. Controlled via `BuildConfig.DEBUG` in `NightCatchersApplication`.

Convention plugins (apply in `build.gradle.kts`):

- `nightcatchers.android.library` — standard Android library (minSdk 26, compileSdk 35, Java 17, coroutines opt-ins)
- `nightcatchers.android.feature` — library + Hilt + Compose + Navigation; **auto-adds** `:core:ui`, `:core:domain`, `:core:common` as implementation deps
- `nightcatchers.android.application` — app module, enables minification + ProGuard
- `nightcatchers.hilt` — Hilt 2.51.1 + KSP; adds `hilt-android` + `hilt-android-compiler`
- `nightcatchers.compose` — Compose BOM 2024.12.01 + compiler; adds material3, animation, foundation, tooling
- `nightcatchers.testing` — JUnit5 5.11.4, Kotest 5.9.1, MockK 1.13.13, Turbine 1.2.0; sets `useJUnitPlatform()`

All dependency versions live in `gradle/libs.versions.toml`. Never hardcode versions in module `build.gradle.kts` files.

## Architecture Rules

- **UDF only**: ViewModels expose `StateFlow<UiState>` and `SharedFlow<UiEvent>`. No two-way data binding.
- **Repository pattern**: Features depend on domain interfaces, never on `*RepositoryImpl` or Room DAOs directly.
- **Use cases**: Business logic lives in `:core:domain` use cases. ViewModels call use cases, not repositories.
- **No Android in domain**: `:core:domain` has zero Android dependencies. Pure Kotlin only.
- **Hilt injection**: All ViewModels are `@HiltViewModel`. All Workers are `@HiltWorker`.
- **Offline-first**: Room is the source of truth. Firestore is a write-behind sync target (conflict-resolved by `updatedAt` server timestamp). ViewModels never read from Firestore directly.

## COPPA Invariants (never violate)

- `MonsterEntity.toFirestoreMap()` **must never** include `captureLatLng` or any GPS data.
- Child identity is a locally-generated UUID (`childId`). No child Firebase Auth.
- Only `childFirstName` (first name) is stored — never full name, DOB, or email.
- No analytics on child sessions (`AccountTier.CHILD`).
- Parent email is gated behind `ParentSessionManager`.

## Security Invariants

- SQLCipher AES-256 key derived from Android KeyStore — never hardcoded.
- Firestore rules enforce `request.auth.uid == parentUID` on all writes.
- Parent PIN stored as bcrypt hash via `UserRepositoryImpl.hashPin()`.
- `AuditEvent.prevHash` forms a SHA-256 tamper chain — never skip seq numbers.
- File sharing uses FileProvider only — never raw file:// URIs.
- Firebase App Check (Play Integrity) initialised in `NightCatchersApplication`.
- Soft-delete pattern for released monsters: set `isReleased = true`, 30-day recovery window for parents.

## Device Tiers

| Tier | Criteria | GL Behaviour |
| ---- | -------- | ------------ |
| A | ≥6 GB RAM + ARCore + OpenGL ES 3.1 | All lenses, full res |
| B | ≥3 GB RAM + OpenGL ES 3.0 | Max 2 FBO passes, half res |
| C | < 3 GB RAM or no OpenGL ES 3.0 | No OpenGL — Lottie fallback |

Emergency downgrade: 3 consecutive frames > 20ms triggers tier fallback in `FilterLayerManager`. AR degradation is always silent — never show capability errors to a child.

## Lens Composition Rules (Section 17)

- Max 2 simultaneous lenses on stack.
- Celebration lenses (`LensId.isCelebration == true`) are **solo** — clear stack before push.
- `PROTON_PACK` always sits at the top of the stack.
- `NIGHT_VISION` and `ECTO_GOGGLES` are mutually exclusive (same slot).
- Stack API: `push()`, `pop()`, `replaceAll()`, `restorePrevious()`, `canPush()`.

## Pet Stat Effects (PetInteraction)

| Interaction | hunger | happiness | energy | spookiness | trust |
| ----------- | ------ | --------- | ------ | ---------- | ----- |
| Feed | +25 | +5 | — | — | — |
| Play | — | +20 | -10 | — | +3 |
| Train | — | — | -15 | -5 | +8 |
| Story | — | +10 | -5 | — | +5 |
| Comfort | — | +15 | -5 | -10 | +6 |
| Praise | — | +12 | — | — | +4 |

Stats clamp 0–100. Decay runs every 4 hours via `StatDecayWorker`: hunger −4, happiness −3, energy −2, spookiness +1 per cycle. Only applied to non-released monsters (`isReleased == false`).

## Mini-Games (Section 19)

Mini-game results flow through `ApplyMiniGameOutcomeUseCase` → `PetRepository.applyStatDelta()`. Game ViewModels never write stats directly.

### Roster

| ID | Tier | Energy cost | Base rewards |
| -- | ---- | ----------- | ------------ |
| `FOOD_TOSS` | BONDING | 10 | hunger +25, happiness +10 |
| `SPOOK_TAG` | BONDING | 20 | happiness +20, spookiness +15 |
| `CUDDLE_STORM` | BONDING | 15 | happiness +30, trust +8, spookiness -10 |
| `SLIME_SORT` | SKILL | 15 | happiness +20, trust +10, spookiness -5 |
| `GHOST_DASH` | SKILL | 20 | happiness +15, trust +5, spookiness +25 |
| `PROTON_WRANGLE` | SKILL | 25 | happiness +10, trust +20, spookiness +10 |

Rewards scale by `scoreFraction` (0.0–1.0) with a 25% floor: `scale = 0.25 + 0.75 * scoreFraction`. Energy cost is always paid in full regardless of score.

### Unlock Gating (`IsMiniGameUnlockedUseCase`)

- SKILL games require `energy ≥ 25`.
- BONDING games require `energy ≥ 10`, except:
  - `FOOD_TOSS` is always unlocked when `hunger < 15` (starving override).
  - `FOOD_TOSS` is locked when `hunger ≥ 90` (TooFull — no point feeding a full monster).
  - `CUDDLE_STORM` is always unlocked when `trust < 10` (newly captured).

### Score Serialisation Across Navigation

`PetPlay` → `PetPlayResult` passes `scoreBps` (integer basis points = `scoreFraction * 10_000`) to avoid floating-point in nav args.

## Mood Priority (GetMoodStateUseCase)

Signature: `invoke(stats: PetStats, lastInteractedAt: Instant = Instant.now()): Mood`

Evaluated top-to-bottom, first match wins:

1. `daysSince(lastInteractedAt) >= 7` → MISSING_YOU
2. `energy < 20` → SLEEPY
3. `hunger < 20` → GRUMPY
4. `happiness < 20` → LONELY
5. `spookiness > 85` → SPOOKED
6. `trust >= 80` → BONDED
7. `hunger > 80 && happiness > 80 && energy > 80` → ECSTATIC
8. `happiness > 80 && energy > 70` → EXCITED
9. `happiness > 60 && trust > 50` → PLAYFUL
10. else → CONTENT

`PetViewModel` calls `getMoodState(petState.stats, petState.lastInteractedAt)` at display time so MISSING_YOU is computed live, not read from the stored mood field.

## Bond Stage & Room Stage

Both are driven by `trust` score (0–100). Trust only ever increases; it resets on release.

### BondStage (`GetBondStageUseCase`)

| Stage | Trust min | Unlocked interactions |
| ----- | --------- | --------------------- |
| STRANGER | 0 | Feed (throws only), Watch |
| CURIOUS | 20 | Feed (any method), Shadow Dance |
| FRIENDLY | 40 | Feed, Play (all), Lullaby Hum, Name |
| BONDED | 60 | Spook Training, Diary, Accessories, Photo Mode |
| BEST_FRIENDS | 80 | Evolve, Greeting, Comfort Mode, Friendship Card |

### EvolutionStage

| Stage | Trust gate |
| ----- | ---------- |
| BABY | 0 |
| TEEN | 40 |
| ADULT | 80 |

### RoomStage (`GetRoomStageUseCase`) — pet room visual theme

| Stage | Trust range | Room name |
| ----- | ----------- | --------- |
| HOLDING_PEN | 0–19 | The Holding Pen |
| COSY_CORNER | 20–39 | The Cosy Corner |
| BEDROOM | 40–59 | The Bedroom |
| SANCTUARY | 60–79 | The Sanctuary |
| DREAM_ROOM | 80–100 | The Dream Room |

## Day Phase (`DayPhase`)

Governs mini-game availability and stat bonuses (Section 18). `NIGHT` wraps midnight (21:00–05:59).

| Phase | Hours |
| ----- | ----- |
| MORNING | 06:00–11:59 |
| AFTERNOON | 12:00–17:59 |
| EVENING | 18:00–20:59 |
| NIGHT | 21:00–05:59 |

## Account Tiers & Safety Policy

`SafetyPolicy.evaluate()` returns `ALLOW`, `SOFT_BLOCK` (≤5 min from cap), or `HARD_BLOCK`.

| Tier | Daily cap | Bedtime |
| ---- | --------- | ------- |
| CHILD | 30 min | 20:00–07:00 |
| TEEN | 60 min | 22:00–07:00 |
| ADULT | none | none |

Bedtime triggers `HARD_BLOCK` for CHILD and TEEN tiers. ADULT has no cap or bedtime.

## Colour Tokens (never use raw hex in Composables)

Capture mode: `SlimeGreen`, `EctoplasmCyan`, `DeepNight`, `MonsterPurple`, `RarityGold`
Pet mode ("If" palette): `SoftLavender`, `PeachWarm`, `MintFresh`, `PetRoomBgTop`, `PetRoomBgBottom`
Rarity: `RarityCommon`, `RarityUncommon`, `RarityRare`, `RarityLegendary`
All defined in `:core:ui` `Color.kt`.

## Navigation

Routes defined in `app/navigation/Dest.kt` as `@Serializable sealed interface`:

| Group | Destinations |
| ----- | ------------ |
| Root | `Splash`, `Onboarding`, `Home` |
| Scan | `ScanCamera`, `ScanFilters`, `ScanCapture(archetypeId)`, `ScanResult(monsterId)` |
| Vault | `Vault`, `VaultDetail(monsterId)`, `VaultRelease(monsterId)` |
| Games | `Games` (top-level picker) |
| Pet | `PetRoom(monsterId)`, `PetPlayMenu(monsterId)`, `PetPlay(monsterId, game)`, `PetPlayResult(monsterId, game, rawScore, scoreBps)`, `PetEvolve(monsterId)` |
| Dex | `Dex`, `DexDetail(archetypeId)`, `DexAchievement(achievementId)`, `DexShare(monsterId)` |
| Settings | `Settings`, `SettingsParent`, `SettingsParentTime`, `SettingsParentPinChange` |
| Tab graphs | `MonsterGraph`, `ScanGraph`, `GamesGraph`, `DexGraph`, `SettingsGraph` (parent route markers only) |

Use `navigateTo*` extension helpers from `NightCatchersNavGraph.kt` — don't build routes manually.
Bottom-nav tabs use `saveState = true` / `restoreState = true`.

Deep link scheme: `monstercatcher://` (handles `pet/{id}`, `vault/{id}`, `capture`, `dex/{id}`).

## Testing Conventions

- Unit tests: JUnit5 (`@Test`, `@BeforeEach`) + Kotest matchers (`shouldBe`, `shouldNotBeNull`)
- Async/Flow: Turbine (`flow.test { … }`)
- Mocking: MockK (`mockk<T>()`, `coEvery`, `coVerify`)
- Fakes preferred over mocks for repositories
- No Robolectric — use fakes and pure unit tests for domain/data layers

## WorkManager Jobs

| Worker | Cadence | Purpose |
| ------ | ------- | ------- |
| `StatDecayWorker` | every 4h | Decay pet stats (hunger −4, happiness −3, energy −2, spookiness +1) |
| `AnniversaryCheckWorker` | every 24h | Trigger Birthday Mode |
| `ShareReviewWorker` | every 6h | Expire 48h+ pending shares |

All workers are `@HiltWorker` with `@AssistedInject`. Scheduled on app creation with `ExistingPeriodicWorkPolicy.KEEP`. WorkManager auto-init is disabled in the manifest; `HiltWorkerFactory` is provided via `NightCatchersApplication : Configuration.Provider`.

## Reference

`DESIGN_GUIDE.md` — visual system, AR filter catalogue, pet system details, and recommended build order. Consult before making visual or UX changes.

`design-docs/section-19-play-minigames/` — detailed Section 19 mini-game design spec.
