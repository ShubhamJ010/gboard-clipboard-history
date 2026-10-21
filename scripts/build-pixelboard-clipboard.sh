#!/usr/bin/env bash
# ==============================================================================
# App 3/3 - PixelBoard (released APK) + Clipboard History (.mpp)
#
#   ./scripts/build-pixelboard-clipboard.sh
#
# Input : latest released PixelBoard APK (auto-downloaded into input/)
# Patch : patches/GboardClipboardHistory.mpp
# Output: output/PixelBoard-clipboard-sj010.apk
#         (package stays com.akshaykadam.pixelboard - upstream base is already renamed)
# ==============================================================================

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib/common.sh"

init_tools
[[ -f "$MPP_OUT" ]] || { log "patch bundle missing, building it"; "$SCRIPTS_DIR/build-mpp.sh"; }

PIXELBOARD_APK="$(ensure_pixelboard_apk)"

log "pixelboard  : $PIXELBOARD_APK"
log "patch bundle: $MPP_OUT"

OUT_APK="$OUTPUT_DIR/PixelBoard-clipboard-sj010.apk"
RESULT="$OUTPUT_DIR/result-pixelboard-clipboard.json"

run_patcher "$OUT_APK" "$RESULT" "$PIXELBOARD_APK" \
    -p="$MPP_OUT"

log "done: $OUT_APK ($(du -h "$OUT_APK" | cut -f1))"
cat "$RESULT"; echo
