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
  filters/                    # FilterLayerManager, ShaderProgramCache, GLSL shaders
  onboarding/                 # First-run flow, permission requests
  parental/                   # PIN gate, parental dashboard
  pet/                        # Pet stats, mood engine, mini-games
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

## Device Tiers

| Tier | Criteria | GL Behaviour |
| ---- | -------- | ------------ |
| A | ≥6 GB RAM + ARCore + OpenGL ES 3.1 | All lenses, full res |
| B | ≥3 GB RAM + OpenGL ES 3.0 | Max 2 FBO passes, half res |
| C | < 3 GB RAM or no OpenGL ES 3.0 | No OpenGL — Lottie fallback |

Emergency downgrade: 3 consecutive frames > 20ms triggers tier fallback in `FilterLayerManager`.

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
| Pet | `PetRoom(monsterId)`, `PetPlay(monsterId, game)`, `PetEvolve(monsterId)` |
| Dex | `Dex`, `DexDetail(archetypeId)`, `DexAchievement(achievementId)`, `DexShare(monsterId)` |
| Settings | `Settings`, `SettingsParent`, `SettingsParentTime` |

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
