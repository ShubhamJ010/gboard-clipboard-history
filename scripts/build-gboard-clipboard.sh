#!/usr/bin/env bash
# ==============================================================================
# App 1/3 - Gboard + Clipboard History (.mpp)
#
#   ./scripts/build-gboard-clipboard.sh
#
# Input : Gboard base APK (input/gboard-merged.apk, input/splits/ or $GBOARD_APK)
# Patch : patches/GboardClipboardHistory.mpp
# Output: output/Gboard-com.sj010.gboard.apk   (package com.sj010.gboard)
# ==============================================================================

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib/common.sh"

init_tools
[[ -f "$MPP_OUT" ]] || { log "patch bundle missing, building it"; "$SCRIPTS_DIR/build-mpp.sh"; }

GBOARD_APK_PATH="$(ensure_gboard_apk)"
log "Gboard base : $GBOARD_APK_PATH"
log "patch bundle: $MPP_OUT"

OUT_APK="$OUTPUT_DIR/Gboard-com.sj010.gboard.apk"
RESULT="$OUTPUT_DIR/result-gboard-clipboard.json"

run_patcher "$OUT_APK" "$RESULT" "$GBOARD_APK_PATH" \
    -p="$MPP_OUT"

log "done: $OUT_APK ($(du -h "$OUT_APK" | cut -f1))"
cat "$RESULT"; echo
