#!/usr/bin/env bash
# ==============================================================================
# Shared helpers for every build script in this repository.
#
# Provides:
#   - path/tool discovery (java, kotlinc, d8, patcher jar, APKEditor)
#   - input provisioning (Gboard base APK, PixelBoard APK, PixelBoard .mpp)
#   - signing + Morphe patcher invocation
#
# Override any default with an environment variable, e.g.
#   JAVA_HOME=... KOTLINC=... SIGNING_KEY=pixelboard ./scripts/build-all.sh
# ==============================================================================

set -euo pipefail

LIB_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCRIPTS_DIR="$(cd "$LIB_DIR/.." && pwd)"
PROJECT_ROOT="$(cd "$SCRIPTS_DIR/.." && pwd)"

SIBLING_DIR="${SIBLING_DIR:-$PROJECT_ROOT/../PixelBoard}"
INPUT_DIR="${INPUT_DIR:-$PROJECT_ROOT/input}"
OUTPUT_DIR="${OUTPUT_DIR:-$PROJECT_ROOT/output}"
TOOLS_DIR="${TOOLS_DIR:-$PROJECT_ROOT/tools}"
BUILD_DIR="${BUILD_DIR:-$PROJECT_ROOT/build/local}"
MPP_OUT="${MPP_OUT:-$PROJECT_ROOT/patches/GboardClipboardHistory.mpp}"

GBOARD_VERSION="18.4.1.985164140-release-arm64-v8a"
PIXELBOARD_REPO="${PIXELBOARD_REPO:-Akshayykadam/PixelBoard}"
PIXELBOARD_TAG="${PIXELBOARD_TAG:-v18.4.1-Stable-V7}"
PIXELBOARD_MPP_TAG="${PIXELBOARD_MPP_TAG:-v18.4.1-Stable-V7}"

log()  { printf '\033[1;34m==>\033[0m %s\n' "$*" >&2; }
warn() { printf '\033[1;33mWARN:\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[1;31mERR:\033[0m %s\n' "$*" >&2; exit 1; }

# ---------------------------------------------------------------- java ------
find_java() {
    if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
        echo "$JAVA_HOME/bin/java"; return 0
    fi
    for candidate in \
        "/opt/homebrew/opt/openjdk/bin/java" \
        "/opt/homebrew/opt/openjdk@21/bin/java" \
        "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" \
        "$(/usr/libexec/java_home -v 21 2>/dev/null || true)/bin/java" \
        "$(command -v java 2>/dev/null || true)"; do
        [[ -n "$candidate" && -x "$candidate" ]] || continue
        ver=$("$candidate" -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1 || true)
        [[ -n "$ver" && "$ver" -ge 21 ]] && { echo "$candidate"; return 0; }
    done
    return 1
}

# ---------------------------------------------------------------- tools -----
find_kotlinc() {
    for candidate in \
        "${KOTLINC:-}" \
        "$SIBLING_DIR/scratch/kotlinc/bin/kotlinc" \
        "$(command -v kotlinc 2>/dev/null || true)"; do
        [[ -n "$candidate" && -x "$candidate" ]] && { echo "$candidate"; return 0; }
    done
    return 1
}

find_d8() {
    local sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
    local d8
    d8=$(ls -1 "$sdk"/build-tools/*/d8 2>/dev/null | sort -V | tail -1 || true)
    [[ -n "$d8" ]] && { echo "$d8"; return 0; }
    return 1
}

find_android_jar() {
    local sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
    local jar
    jar=$(ls -1d "$sdk"/platforms/android-3[4-9]*/android.jar 2>/dev/null | sort -V | tail -1 || true)
    [[ -n "$jar" ]] && { echo "$jar"; return 0; }
    return 1
}

find_apkeditor() {
    for candidate in \
        "${APKEDITOR:-}" \
        "$SIBLING_DIR/scratch/APKEditor.jar" \
        "$TOOLS_DIR/APKEditor.jar"; do
        [[ -n "$candidate" && -f "$candidate" ]] && { echo "$candidate"; return 0; }
    done
    return 1
}

find_aapt2() {
    local sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-$HOME/Library/Android/sdk}}"
    local bin
    bin=$(ls -1 "$sdk"/build-tools/*/aapt2 2>/dev/null | sort -V | tail -1 || true)
    [[ -n "$bin" ]] && { echo "$bin"; return 0; }
    return 1
}

# Morphe patcher jar: prefer the local sibling checkout, fall back to the
# upstream PixelBoard repository (tools/patcher.jar is tracked there).
ensure_patcher_jar() {
    if [[ -f "$TOOLS_DIR/patcher.jar" ]]; then
        echo "$TOOLS_DIR/patcher.jar"; return 0
    fi
    if [[ -f "$SIBLING_DIR/tools/patcher.jar" ]]; then
        log "copying patcher.jar from $SIBLING_DIR/tools"
        mkdir -p "$TOOLS_DIR"
        cp "$SIBLING_DIR/tools/patcher.jar" "$TOOLS_DIR/patcher.jar"
        echo "$TOOLS_DIR/patcher.jar"; return 0
    fi
    log "downloading patcher.jar from $PIXELBOARD_REPO"
    mkdir -p "$TOOLS_DIR"
    curl -fL --retry 3 -o "$TOOLS_DIR/patcher.jar" \
        "https://raw.githubusercontent.com/$PIXELBOARD_REPO/main/tools/patcher.jar" \
        || die "cannot fetch patcher.jar"
    echo "$TOOLS_DIR/patcher.jar"
}

# -------------------------------------------------------------- keystore ----
# SIGNING_KEY=clipboard -> repository keystore (CN=sj010)
# SIGNING_KEY=pixelboard -> sibling PixelBoard keystore (same cert as older builds)
resolve_keystore() {
    local key="${SIGNING_KEY:-pixelboard}"
    case "$key" in
        pixelboard)
            SIGNER_NAME="PixelBoard"
            KEYSTORE_PATH="${KEYSTORE_PATH:-$SIBLING_DIR/tools/patcher-data/pixelboard.keystore}"
            KEYSTORE_PASSWORD="pixelboard"
            KEYSTORE_ALIAS="pixelboard"
            ;;
        *)
            SIGNER_NAME="sj010"
            KEYSTORE_PATH="${KEYSTORE_PATH:-$TOOLS_DIR/patcher-data/clipboard.keystore}"
            KEYSTORE_PASSWORD="clipboard"
            KEYSTORE_ALIAS="clipboard"
            ;;
    esac
    if [[ ! -f "$KEYSTORE_PATH" ]]; then
        log "generating keystore $KEYSTORE_PATH"
        mkdir -p "$(dirname "$KEYSTORE_PATH")"
        keytool -genkeypair \
            -keystore "$KEYSTORE_PATH" -alias "$KEYSTORE_ALIAS" \
            -keyalg RSA -keysize 2048 -validity 10000 \
            -storepass "$KEYSTORE_PASSWORD" -keypass "$KEYSTORE_PASSWORD" \
            -dname "CN=$SIGNER_NAME, OU=Keyboard, O=OpenSource, L=Unknown, ST=Unknown, C=US" \
            >/dev/null 2>&1
    fi
}

# ---------------------------------------------------------------- inputs ----
# Gboard base APK. Priority:
#   1. $INPUT_DIR/gboard-merged.apk (already merged, preferred)
#   2. $INPUT_DIR/splits/*.apk merged on the fly with APKEditor
#   3. $GBOARD_APK (explicit override)
ensure_gboard_apk() {
    if [[ -n "${GBOARD_APK:-}" && -f "$GBOARD_APK" ]]; then
        echo "$GBOARD_APK"; return 0
    fi
    local merged="$INPUT_DIR/gboard-merged.apk"
    if [[ -f "$merged" ]]; then
        echo "$merged"; return 0
    fi
    if [[ -d "$INPUT_DIR/splits" ]]; then
        log "merging Gboard splits from $INPUT_DIR/splits"
        local editor
        editor=$(find_apkeditor) || die "APKEditor.jar not found (needed to merge splits)"
        local merged_out="$INPUT_DIR/gboard-merged.apk"
        rm -rf "$BUILD_DIR/splits-merge"
        "$JAVA_BIN" -jar "$editor" m \
            -i "$INPUT_DIR/splits" \
            -o "$merged_out" >/dev/null 2>&1 || die "APKEditor merge failed"
        echo "$merged_out"; return 0
    fi
    die "no Gboard base APK found. Place one at $merged or $INPUT_DIR/splits/, or set GBOARD_APK=<path>"
}

# Latest released PixelBoard APK (release asset).
ensure_pixelboard_apk() {
    local target="$INPUT_DIR/PixelBoard-${PIXELBOARD_TAG}.apk"
    if [[ -f "$target" ]]; then
        echo "$target"; return 0
    fi
    mkdir -p "$INPUT_DIR"
    log "downloading PixelBoard APK ($PIXELBOARD_TAG)"
    curl -fL --retry 3 -o "$target" \
        "https://github.com/$PIXELBOARD_REPO/releases/download/$PIXELBOARD_TAG/PixelBoard-18.4.1-V7.apk" \
        || die "cannot download PixelBoard APK"
    echo "$target"
}

# PixelBoard patch bundle (.mpp) used for the combined builds.
ensure_pixelboard_mpp() {
    local target="$INPUT_DIR/patches-1.2.0.mpp"
    if [[ -f "$target" ]]; then
        echo "$target"; return 0
    fi
    mkdir -p "$INPUT_DIR"
    log "downloading PixelBoard .mpp ($PIXELBOARD_MPP_TAG)"
    curl -fL --retry 3 -o "$target" \
        "https://github.com/$PIXELBOARD_REPO/releases/download/$PIXELBOARD_MPP_TAG/patches-1.2.0.mpp" \
        || die "cannot download PixelBoard .mpp"
    echo "$target"
}

# ---------------------------------------------------------------- patcher ---
# run_patcher <output apk> <result json> <apk> [-- extra morphe args ...]
# Extra args are passed straight through to `morphe-desktop patch`.
run_patcher() {
    local out="$1"; shift
    local result="$1"; shift
    local apk="$1"; shift

    mkdir -p "$OUTPUT_DIR" "$TOOLS_DIR/patcher-data/tmp"
    "$JAVA_BIN" -jar "$PATCHER_JAR" patch \
        --keystore="$KEYSTORE_PATH" \
        --keystore-password="$KEYSTORE_PASSWORD" \
        --keystore-entry-alias="$KEYSTORE_ALIAS" \
        --keystore-entry-password="$KEYSTORE_PASSWORD" \
        -t="$TOOLS_DIR/patcher-data/tmp" \
        --signer="$SIGNER_NAME" \
        -o="$out" \
        -r="$result" \
        "$@" \
        "$apk"
}

# Common tool bootstrap, call once per script.
init_tools() {
    JAVA_BIN="${JAVA_BIN:-$(find_java || true)}"
    [[ -n "$JAVA_BIN" ]] || die "Java 21+ not found (set JAVA_HOME)"

    PATCHER_JAR="${PATCHER_JAR:-$(ensure_patcher_jar)}"
    D8_BIN="${D8_BIN:-$(find_d8 || true)}"
    ANDROID_JAR="${ANDROID_JAR:-$(find_android_jar || true)}"

    mkdir -p "$OUTPUT_DIR" "$INPUT_DIR" "$TOOLS_DIR" "$BUILD_DIR"
    resolve_keystore
}
