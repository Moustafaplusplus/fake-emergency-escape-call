#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SOURCE="$ROOT/app/src/main/assets/branding/app_icon_source.png"
RES="$ROOT/app/src/main/res"

if [[ ! -f "$SOURCE" ]]; then
  echo "Missing source icon: $SOURCE" >&2
  exit 1
fi

generate() {
  local size=$1
  local out=$2
  sips -z "$size" "$size" "$SOURCE" --out "$out" >/dev/null
  echo "  $out (${size}x${size})"
}

echo "Generating launcher icons from $SOURCE"

generate 48  "$RES/mipmap-mdpi/ic_launcher.png"
generate 48  "$RES/mipmap-mdpi/ic_launcher_round.png"
generate 72  "$RES/mipmap-hdpi/ic_launcher.png"
generate 72  "$RES/mipmap-hdpi/ic_launcher_round.png"
generate 96  "$RES/mipmap-xhdpi/ic_launcher.png"
generate 96  "$RES/mipmap-xhdpi/ic_launcher_round.png"
generate 144 "$RES/mipmap-xxhdpi/ic_launcher.png"
generate 144 "$RES/mipmap-xxhdpi/ic_launcher_round.png"
generate 192 "$RES/mipmap-xxxhdpi/ic_launcher.png"
generate 192 "$RES/mipmap-xxxhdpi/ic_launcher_round.png"

echo "Generating adaptive foreground + in-app branding assets"
generate 432 "$RES/drawable-nodpi/ic_launcher_foreground.png"
generate 512 "$RES/drawable-nodpi/ic_app_logo.png"
generate 288 "$RES/drawable-nodpi/ic_splash_logo.png"

echo "Done."
