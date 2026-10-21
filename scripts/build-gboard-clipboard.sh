#!/usr/bin/env bash
# ==============================================================================
# App 1/3 - Gboard + Clipboard History (.mpp)
#
#   ./scripts/build-gboard-clipboard.sh
#
# Input : Gboard base APK (input/gboard-merged.apk, input/splits/ or $GBOARD_APK)
# Patch : patches/GboardClipboardHistory.mpp
# Output: output/Gboard-com.sj010.gboard.apk   (package com.sj010.gboard)
#
# Two stage build on purpose:
#   stage 1  rename only  -> package becomes com.sj010.gboard
#   stage 2  clipboard history on top of the renamed base
# The provider authority and the signature permission are derived from the host
# package, so the rename has to be finished before the clipboard patch runs.
# ==============================================================================

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib/common.sh"

init_tools
[[ -f "$MPP_OUT" ]] || { log "patch bundle missing, building it"; "$SCRIPTS_DIR/build-mpp.sh"; }

GBOARD_APK_PATH="$(ensure_gboard_apk)"

log "Gboard base : $GBOARD_APK_PATH"
log "patch bundle: $MPP_OUT"

BASE_APK="$OUTPUT_DIR/A1-Gboard-renamed-base.apk"
RESULT1="$OUTPUT_DIR/result-gboard-rename.json"
OUT_APK="$OUTPUT_DIR/Gboard-com.sj010.gboard.apk"
RESULT2="$OUTPUT_DIR/result-gboard-clipboard.json"

log "stage 1/2: package rename (com.sj010.gboard)"
run_patcher "$BASE_APK" "$RESULT1" "$GBOARD_APK_PATH" \
    -p="$MPP_OUT" \
    -d "Clipboard Foreground Sync" \
    -d "Gboard Clipboard History"

log "stage 2/2: clipboard history"
run_patcher "$OUT_APK" "$RESULT2" "$BASE_APK" \
    -p="$MPP_OUT"

log "done: $OUT_APK ($(du -h "$OUT_APK" | cut -f1))"
cat "$RESULT2"; echo
