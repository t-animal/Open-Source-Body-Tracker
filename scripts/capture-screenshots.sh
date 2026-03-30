#!/usr/bin/env bash
set -euo pipefail

# Capture release screenshots for Open Source Body Tracker
# Can be run locally or in CI

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_ROOT}"

SCREENSHOT_PACKAGE="de.t_animal.opensourcebodytracker.screenshot"
SCREENSHOT_TEST_RUNNER="androidx.test.runner.AndroidJUnitRunner"
SCREENSHOT_TEST_CLASS="de.t_animal.opensourcebodytracker.ReleaseScreenshotInstrumentedTest"
SCREENSHOT_DEVICE_DIR="/sdcard/Android/media/${SCREENSHOT_PACKAGE}/test-screenshots"
SCREENSHOT_HOST_DIR="app/build/outputs/test-screenshots"

# Colors for output
if [ -t 1 ]; then
  RED='\033[0;31m'
  GREEN='\033[0;32m'
  YELLOW='\033[1;33m'
  NC='\033[0m' # No Color
else
  RED=''
  GREEN=''
  YELLOW=''
  NC=''
fi

log_info() {
  echo -e "${GREEN}[INFO]${NC} $*"
}

log_warn() {
  echo -e "${YELLOW}[WARN]${NC} $*"
}

log_error() {
  echo -e "${RED}[ERROR]${NC} $*"
}

# Check if adb is available
if ! command -v adb &> /dev/null; then
  log_error "adb not found. Please ensure Android SDK platform-tools are installed and in PATH."
  exit 1
fi

# Check if an emulator/device is connected
if ! adb devices | grep -q "device$"; then
  log_error "No Android device or emulator detected."
  log_info "Please start an emulator or connect a device, then run this script again."
  log_info ""
  log_info "To start an emulator, you can use:"
  log_info "  emulator -avd <avd-name> -no-snapshot-load"
  log_info ""
  log_info "To list available AVDs:"
  log_info "  emulator -list-avds"
  exit 1
fi

DEVICE_NAME=$(adb devices | grep "device$" | head -n1 | awk '{print $1}')
log_info "Using device/emulator: ${DEVICE_NAME}"

IS_CI="${CI:-}"

adb_push_install() {
  local apk="$1"
  local remote="/data/local/tmp/$(basename "${apk}")"
  if [ -n "${IS_CI}" ]; then
    adb push "${apk}" "${remote}" > /dev/null
  else
    adb push "${apk}" "${remote}"
  fi

  echo "Triggering installation of $(basename "${apk}")... This can take a while."
  adb shell pm install -t "${remote}"

  echo "Cleaning up remote APK..."
  adb shell rm "${remote}"
}

# Wait for device to be ready
log_info "Waiting for device to be fully booted..."
adb wait-for-device
adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done'
log_info "Device ready"

# Clean previous screenshots
log_info "Cleaning previous screenshots..."
rm -rf "${SCREENSHOT_HOST_DIR}"
mkdir -p "${SCREENSHOT_HOST_DIR}"

# Build APKs
log_info "Building APKs..."
./gradlew --no-daemon \
  :app:assembleScreenshot \
  :app:assembleScreenshotAndroidTest \
  --console=plain

APP_APK="$(find app/build/outputs/apk/screenshot -name '*.apk' | head -n1)"
TEST_APK="$(find app/build/outputs/apk/androidTest/screenshot -name '*.apk' | head -n1)"

log_info "App APK:  ${APP_APK}"
log_info "Test APK: ${TEST_APK}"

# Install APKs
log_info "Installing APKs..."
adb_push_install "${APP_APK}"
adb_push_install "${TEST_APK}"

# Run tests
log_info "Running screenshot tests..."
set +e
adb shell am instrument -w \
  -e class "${SCREENSHOT_TEST_CLASS}" \
  "${SCREENSHOT_PACKAGE}.test/${SCREENSHOT_TEST_RUNNER}"
TEST_EXIT_CODE=$?
set -e

# Show recent errors from logcat if test failed
if [ $TEST_EXIT_CODE -ne 0 ]; then
  log_warn "Test failed, showing recent logcat errors..."
  adb logcat -d | grep -E "ReleaseScreenshotTest|MainActivity|FATAL|AndroidRuntime" | tail -100 || true
  echo ""
fi

# Pull screenshots before uninstalling — Android deletes app-specific external dirs
# (/sdcard/Android/media/<package>/ and /sdcard/Android/data/<package>/) on uninstall.
# /sdcard/Android/media/ is accessible via plain adb pull on both real devices and
# emulators (unlike /sdcard/Android/data/ which is blocked by scoped storage on
# Android 11+ real devices).
log_info "Pulling screenshots from device..."
if adb pull "${SCREENSHOT_DEVICE_DIR}/." "${SCREENSHOT_HOST_DIR}" 2>/dev/null; then
  SCREENSHOT_COUNT=$(find "${SCREENSHOT_HOST_DIR}" -name "*.png" 2>/dev/null | wc -l)
  log_info "Pulled ${SCREENSHOT_COUNT} screenshot(s)"
else
  log_warn "Could not pull screenshots from device (path: ${SCREENSHOT_DEVICE_DIR})"
fi

# Uninstall APKs
log_info "Uninstalling APKs..."
adb uninstall "${SCREENSHOT_PACKAGE}" 2>/dev/null || true
adb uninstall "${SCREENSHOT_PACKAGE}.test" 2>/dev/null || true

# Generate summary if test failed and no summary was created
if [ ! -f "${SCREENSHOT_HOST_DIR}/summary.txt" ]; then
  log_warn "No summary.txt found, generating failure summary"
  printf 'Release screenshot capture summary\nFAILED - screenshot instrumentation task exited with code %s\n' "${TEST_EXIT_CODE}" > "${SCREENSHOT_HOST_DIR}/summary.txt"
fi

# Display summary
echo ""
log_info "Screenshot capture summary:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
cat "${SCREENSHOT_HOST_DIR}/summary.txt"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# List generated screenshots
if compgen -G "${SCREENSHOT_HOST_DIR}/*.png" > /dev/null; then
  log_info "Generated screenshots:"
  ls -lh "${SCREENSHOT_HOST_DIR}"/*.png | awk '{print "  - " $9 " (" $5 ")"}'
  echo ""
  log_info "Screenshots saved to: ${SCREENSHOT_HOST_DIR}/"
else
  log_error "No screenshots were generated"
  exit 1
fi

log_info "Screenshot capture complete!"
exit 0
