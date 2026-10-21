#!/usr/bin/env bash
# ==============================================================================
# Build every patched APK in this repository.
#
#   ./scripts/build-all.sh
#
#   1. patches/GboardClipboardHistory.mpp
#   2. output/Gboard-com.sj010.gboard.apk           (Gboard + Clipboard History)
#   3. output/Gboard-PixelBoard-akshaykadam.apk    (Gboard + PixelBoard + Clipboard History)
#   4. output/PixelBoard-clipboard-sj010.apk       (released PixelBoard + Clipboard History)
# ==============================================================================

set -euo pipefail

SCRIPTS_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

"$SCRIPTS_DIR/build-mpp.sh"
"$SCRIPTS_DIR/build-gboard-clipboard.sh"
"$SCRIPTS_DIR/build-gboard-clipboard-pixelboard.sh"
"$SCRIPTS_DIR/build-pixelboard-clipboard.sh"

echo
echo "=== outputs ==="
ls -la "$(cd "$SCRIPTS_DIR/.." && pwd)/output"/*.apk
