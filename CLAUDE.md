# CLAUDE.md — working notes for this repo

Instructions for future Claude sessions on **La Ruota Della Fortuna** (Italian Wheel of
Fortune, Android phone + TV). Read this first.

## What this is
Kotlin + Jetpack Compose game, pass-and-play, localized in Italian. Multi-module Gradle:
- `:core` — **pure Kotlin** game engine (no Android). All game rules live here and are
  JVM-unit-tested. Packages: `model`, `text` (ItalianText), `board`, `wheel`, `puzzle`
  (PhraseParser), `engine` (GameEngine state machine, GameState, Outcome).
- `:ui-common` — shared Compose UI (Android library). `wheel`, `board`, `keyboard`,
  `setup`, `prefs` (DataStore), `game` (GameViewModel), `screen` (GameScreen +
  RuotaGameApp — the whole game as one Composable). Phrase pack: `src/main/assets/phrases_it.json`.
- `:app-mobile` / `:app-tv` — thin activities hosting `RuotaGameApp` inside a theme. TV
  has the Leanback manifest. Both apps set `configChanges` so rotation does NOT recreate
  the activity (keeps in-memory game state).

Design keeps ALL logic in `:core` so it's testable off-device; UI is shared so phone and
TV behave identically (Material3 controls are focusable → D-pad works on TV).

## Environment (IMPORTANT)
- **Build with JDK 17**, not the machine default (Java 25, too new for AGP):
  `export JAVA_HOME=/home/eolivelli/.sdkman/candidates/java/17.0.19-tem`
- Android SDK: `~/Android/Sdk`. Gradle wrapper is 8.11.1. AGP 8.7.3, Kotlin 2.1.0.
- `local.properties` (gitignored) holds `sdk.dir`. Each git worktree needs its own copy.
- AVDs: `Pixel_Phone` (android-34 google_apis x86_64) and `Android_TV`
  (**android-36** android-tv x86_64 — android-34 TV only ships 32-bit x86). KVM available.
  Boot headless: `-no-window -gpu swiftshader_indirect`.

## Common commands
```bash
export JAVA_HOME=/home/eolivelli/.sdkman/candidates/java/17.0.19-tem
./gradlew test                                   # ALL JVM unit tests (core + ui-common)
./gradlew :core:test                             # engine tests only
./gradlew assembleDebug                          # both debug APKs
./gradlew :app-mobile:connectedDebugAndroidTest  # instrumented tests (needs a running AVD)
scripts/run.sh mobile   # build + boot Pixel_Phone AVD + install + launch
scripts/run.sh tv       # same for Android_TV
```
APKs: `app-mobile/build/outputs/apk/debug/app-mobile-debug.apk` (pkg `com.ruota.fortuna`),
`app-tv/build/outputs/apk/debug/app-tv-debug.apk` (pkg `com.ruota.fortuna.tv`, LEANBACK).

## Verifying on an emulator (headless)
```bash
export ANDROID_HOME=$HOME/Android/Sdk; export PATH="$ANDROID_HOME/platform-tools:$PATH"
$ANDROID_HOME/emulator/emulator -avd Pixel_Phone -no-window -no-snapshot -gpu swiftshader_indirect -port 5554 &
adb -s emulator-5554 wait-for-device
# poll: adb -s emulator-5554 shell getprop sys.boot_completed  (until "1")
adb -s emulator-5554 install -r app-mobile/build/outputs/apk/debug/app-mobile-debug.apk
adb -s emulator-5554 shell monkey -p com.ruota.fortuna -c android.intent.category.LAUNCHER 1
adb -s emulator-5554 exec-out screencap -p > shot.png     # then Read shot.png
adb -s emulator-5554 emu kill                              # shut down when done
```

## Conventions / rules of the road
- **Tests must pass before a step is "done".** Add unit tests for new engine rules in
  `:core`; validate the bundled phrase pack via `PhrasePackAssetTest`.
- Keep game logic in `:core` (pure Kotlin). UI-only helpers that are pure (e.g.
  `outcomeMessage`, `WheelGeometry`, `BoardLayout`) get JVM tests in `:ui-common`.
- All player-facing strings are **Italian**.
- Commit messages end with the Co-Authored-By / Claude-Session trailer.

## Adding phrases
Edit `ui-common/src/main/assets/phrases_it.json` — array of `{category, text}`. Accents ok
(folded on match). No code changes needed. `PhrasePackAssetTest` enforces ≥100, unique,
playable.

## Release process (GitHub)
Repo: https://github.com/eolivelli/ruota_fortuna (public, `gh` account `eolivelli`).
```bash
./gradlew :app-mobile:assembleDebug
cp app-mobile/build/outputs/apk/debug/app-mobile-debug.apk /tmp/LaRuotaDellaFortuna-<ver>.apk
gh release create <tag> --title "..." --notes "..." /tmp/LaRuotaDellaFortuna-<ver>.apk
```
APKs are unsigned debug builds (fine for testing; Play Store needs a signed release).

## Known follow-ups / ideas
- App icons & sounds, round timer, signed release build, more phrases, online multiplayer.
