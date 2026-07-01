#!/usr/bin/env bash
# Build, install and launch La Ruota Della Fortuna on an emulator.
# Usage: scripts/run.sh [mobile|tv]
set -euo pipefail

TARGET="${1:-mobile}"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# The Android Gradle Plugin needs JDK 17; fall back to a SDKMAN JDK 17 if the current
# java is newer (this machine's default is JDK 25).
if ! java -version 2>&1 | grep -q 'version "17'; then
  for candidate in "$HOME"/.sdkman/candidates/java/17*; do
    [ -x "$candidate/bin/java" ] && { export JAVA_HOME="$candidate"; break; }
  done
fi
echo "==> Using JDK: $(${JAVA_HOME:+$JAVA_HOME/bin/}java -version 2>&1 | head -1)"

# Windowed by default so you can see/play it; set HEADLESS=1 for no window.
WINDOW_FLAG=""
[ "${HEADLESS:-0}" = "1" ] && WINDOW_FLAG="-no-window"
GPU="${EMULATOR_GPU:-swiftshader_indirect}"

case "$TARGET" in
  mobile)
    AVD="Pixel_Phone"; PORT=5554
    APK="$ROOT/app-mobile/build/outputs/apk/debug/app-mobile-debug.apk"
    PKG="com.ruota.fortuna"; CATEGORY="android.intent.category.LAUNCHER"
    GRADLE_TASK=":app-mobile:assembleDebug"
    ;;
  tv)
    AVD="Android_TV"; PORT=5556
    APK="$ROOT/app-tv/build/outputs/apk/debug/app-tv-debug.apk"
    PKG="com.ruota.fortuna.tv"; CATEGORY="android.intent.category.LEANBACK_LAUNCHER"
    GRADLE_TASK=":app-tv:assembleDebug"
    ;;
  *)
    echo "Usage: $0 [mobile|tv]" >&2; exit 1
    ;;
esac

SERIAL="emulator-$PORT"

echo "==> Building $GRADLE_TASK"
( cd "$ROOT" && ./gradlew "$GRADLE_TASK" )

if ! adb devices | grep -q "^$SERIAL"; then
  echo "==> Booting AVD '$AVD' (headless)"
  nohup "$ANDROID_HOME/emulator/emulator" -avd "$AVD" -no-snapshot -no-boot-anim \
    $WINDOW_FLAG -gpu "$GPU" -port "$PORT" >/tmp/ruota_emu_$TARGET.log 2>&1 &
  adb -s "$SERIAL" wait-for-device
  echo -n "==> Waiting for boot"
  until [ "$(adb -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
    echo -n "."; sleep 3
  done
  echo " done"
fi

echo "==> Installing $APK"
adb -s "$SERIAL" install -r "$APK"

echo "==> Launching $PKG"
adb -s "$SERIAL" shell monkey -p "$PKG" -c "$CATEGORY" 1 >/dev/null
echo "==> Launched on $SERIAL"
