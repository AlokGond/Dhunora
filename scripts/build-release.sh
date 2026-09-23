#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
python3 "$root/scripts/prepare-upstream.py"
cd "$root/upstream/SimpMusic"
./gradlew :androidApp:assembleRelease --no-configuration-cache "$@"
# Signing uses one persistent private key. No disposable key is created during builds.
if [[ -n "${DHUNORA_KEYSTORE:-}" ]]; then
  : "${ANDROID_HOME:?Set ANDROID_HOME}"
  : "${DHUNORA_STORE_PASSWORD:?Set DHUNORA_STORE_PASSWORD}"
  : "${DHUNORA_KEY_ALIAS:?Set DHUNORA_KEY_ALIAS}"
  : "${DHUNORA_KEY_PASSWORD:?Set DHUNORA_KEY_PASSWORD}"
  build_tools="$(find "$ANDROID_HOME/build-tools" -mindepth 1 -maxdepth 1 -type d | sort -V | tail -1)"
  mkdir -p "$root/dist"
  for apk in androidApp/build/outputs/apk/release/*-unsigned.apk; do
    [[ -f "$apk" ]] || continue
    name="$(basename "$apk" .apk)"
    name="${name/androidApp/Dhunora-v1.0.0}"
    target="$root/dist/${name/-unsigned/-release}.apk"
    "$build_tools/zipalign" -f -P 16 4 "$apk" "$target"
    "$build_tools/apksigner" sign --ks "$DHUNORA_KEYSTORE" --ks-key-alias "$DHUNORA_KEY_ALIAS" \
      --ks-pass env:DHUNORA_STORE_PASSWORD --key-pass env:DHUNORA_KEY_PASSWORD "$target"
    "$build_tools/apksigner" verify --verbose "$target"
  done
else
  echo 'Unsigned release built. Set the DHUNORA signing variables for an installable APK.'
fi
