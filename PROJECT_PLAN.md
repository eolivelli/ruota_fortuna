# La Ruota Della Fortuna — Project Plan

An Android game (mobile + Android TV) implementing the Italian "Wheel of Fortune".
Kotlin + Jetpack Compose, pass-and-play, localized in Italian.

## Context

Greenfield build. Target: phones and Android TVs from one Gradle project. The game
engine is pure Kotlin so it is fully unit-testable off-device; Compose UI is split
between a mobile app (Material3) and a TV app (Compose for TV, D-pad focus), sharing
Composables via a `:ui-common` module.

## Locked decisions

| Topic | Choice |
|---|---|
| Stack | Kotlin + Jetpack Compose, multi-module Gradle |
| Multiplayer | Pass-and-play, single device, 1–4 players |
| Rounds | Default 4, settable in preferences |
| Vowels | Buy a vowel for $250 from round total; consonants earn money |
| Wheel | Money wedges + BANKRUPT (zero round total) + LOSE A TURN (pass) |
| Solve check | Setting: on-screen keyboard (auto-check) OR host-confirm buttons |
| Phrases | Bundled `phrases_it.json`, ~15 seeds + documented format |
| SDK setup | Android command-line tools; phone + TV AVDs (KVM accelerated) |
| Build JDK | 17 (Temurin, via SDKMAN) |

## Module layout

```
ruota_fortuna/
  settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml
  core/            # pure Kotlin: models + game engine + JVM unit tests
  ui-common/       # shared Composables: Wheel, Board, Keyboard, theme
  app-mobile/      # Compose Material3 Activity
  app-tv/          # Compose for TV Activity (Leanback manifest, D-pad)
```

## Game engine (`:core`) — the critical path

Pure Kotlin, deterministic (inject `Random(seed)` for tests). Key types:
- `Player(name, grandTotal, roundTotal)`
- `WheelWedge`: `Money(amount)`, `Bankrupt`, `LoseATurn`
- `Puzzle(category, text)` + `Board` (revealed letters, masking, vowel/consonant sets)
- `GameConfig(numRounds, numPlayers, vowelCost=250, solveMode)`
- `GameEngine` state machine: `spin()` → `guessConsonant(c)` → `buyVowel(v)` →
  `attemptSolve(text)` / `hostConfirm(correct)` → turn/round/game transitions
- Italian text normalization (case-fold, strip accents à→a, è→e, keep only A–Z for
  comparison) shared by board masking and solve checking.
- Win condition: highest `grandTotal` after last round.

Every engine rule gets a unit test (JUnit5) — must be green before the module is "done".

## Execution phases (multi-agent, git worktrees)

**Phase 0 — Foundation (sequential, blocking).** `git init`, `.gitignore`, Gradle
wrapper, version catalog, empty modules that compile, `local.properties`. Commit.

**Phase 1 — Android SDK + emulators (parallel, background).** Install cmdline-tools,
platform-tools, build-tools, `platforms;android-34`, emulator, system images
(phone `google_apis`, tv `android-tv`); create `Pixel_Phone` and `Android_TV` AVDs;
accept licenses. Independent of code authoring.

**Phase 2 — Parallel feature dev (worktrees + subagents).** After Phase 0 commit:
- Agent A (critical): `:core` engine + models + JVM unit tests.
- Agent B: `:ui-common` Wheel Composable (spin animation, wedge rendering) + tests.
- Agent C: `:ui-common` Board tiles + D-pad on-screen keyboard + tests.
- Agent D: Preferences (DataStore) + player-setup + navigation scaffold.
Agents B/C/D code against the `:core` API surface defined first in Agent A's contract.
Each works in its own `git worktree`, writes tests, merges only when green.

**Phase 3 — Integration.** Wire engine → screens in `:app-mobile` and `:app-tv`
(setup → play → round-summary → game-over). Localized `strings.xml` (values-it default,
values English fallback). Compose UI tests.

**Phase 4 — Run & verify.** Boot both AVDs, `installDebug`, drive a full game, capture
screenshots. Instrumented smoke test. Fix issues.

**Phase 5 — Polish & docs.** Sounds (optional), README with build/run instructions,
phrase-format docs.

## Definition of done per step
- Code compiles across all modules.
- Unit/integration tests written and **all passing** (`./gradlew test`).
- For UI steps: verified on at least one running AVD (screenshot).
- Work committed on a branch/worktree, merged to `main` when green.

## Verification
- `./gradlew :core:test` — engine rules.
- `./gradlew test` — all module unit tests.
- `./gradlew :app-mobile:connectedDebugAndroidTest` on phone AVD.
- Manual: install on phone AVD + TV AVD, play a full round each, screenshot.
