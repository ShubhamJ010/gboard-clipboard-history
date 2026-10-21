#!/usr/bin/env bash
# ==============================================================================
# Gboard Clipboard History - local (credential free) build & patch script.
#
# The Gradle build (`app.morphe.patches`) resolves the plugin from GitHub Packages
# and therefore needs a GitHub token. This script reproduces the same MPP layout
# using only local tooling (kotlinc + d8 + the Morphe patcher jar that ships with
# the sibling PixelBoard checkout).
#
#   ./build-local.sh [--rebuild-extension] [--patch <stock.apk>]
#
# Outputs:
#   patches/GboardClipboardHistory.mpp   patched bundle
#   output/Gboard-com.sj010.gboard.apk   patched APK (when --patch is used)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
PIXELBOARD_DIR="$ROOT_DIR/PixelBoard"

JAVA="${JAVA_HOME:+$JAVA_HOME/bin/java}"
if [[ -z "$JAVA" ]] || [[ ! -x "$JAVA" ]]; then
    for candidate in \
        "/opt/homebrew/opt/openjdk/bin/java" \
        "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" \
        "/opt/homebrew/opt/openjdk@21/bin/java" \
        "$(/usr/libexec/java_home -v 21 2>/dev/null || true)/bin/java" \
        "$(command -v java 2>/dev/null || true)"; do
        if [[ -n "$candidate" ]] && [[ -x "$candidate" ]]; then
            JAVA="$candidate"
            break
        fi
    done
fi
if [[ -z "$JAVA" ]] || [[ ! -x "$JAVA" ]]; then
    echo "❌ Java 21+ not found. Set JAVA_HOME."
    exit 1
fi

KOTLINC="$PIXELBOARD_DIR/scratch/kotlinc/bin/kotlinc"
PATCHER_JAR="$PIXELBOARD_DIR/tools/patcher.jar"
BT="$HOME/Library/Android/sdk/build-tools/35.0.0"
ANDROID_JAR="$HOME/Library/Android/sdk/platforms/android-36/android.jar"

MPP_OUT="patches/GboardClipboardHistory.mpp"
BUILD_DIR="build/local"
PATCH_CLASSES="$BUILD_DIR/patchclasses"
PATCH_DEX="$BUILD_DIR/patchdex"
MPP_STAGE="$BUILD_DIR/mpp"
EXT_OUT="$BUILD_DIR/extension"
VERSION="$(grep -E '^version\s*=' gradle.properties | cut -d'=' -f2 | tr -d ' ')"

REBUILD_EXTENSION=false
STOCK_APK=""
SIGNING_KEY="clipboard"
while [[ $# -gt 0 ]]; do
    case "$1" in
        --rebuild-extension) REBUILD_EXTENSION=true; shift ;;
        --patch) STOCK_APK="${2:-}"; shift 2 ;;
        --signing-key) SIGNING_KEY="${2:-}"; shift 2 ;;
        -h|--help) sed -n '2,20p' "$0"; exit 0 ;;
        *) echo "Unknown argument: $1"; exit 1 ;;
    esac
done

# Signing keystores. "clipboard" is this repo's own keystore, "pixelboard" reuses the sibling
# PixelBoard keystore so the build keeps the same signature as an already installed build
# (Android refuses updates when the signing certificate differs).
case "$SIGNING_KEY" in
    pixelboard)
        SIGNER_NAME="PixelBoard"
        KEYSTORE_PATH="$PIXELBOARD_DIR/tools/patcher-data/pixelboard.keystore"
        KEYSTORE_PASSWORD="pixelboard"
        KEYSTORE_ALIAS="pixelboard"
        ;;
    *)
        SIGNER_NAME="GboardClipboard"
        KEYSTORE_PATH="tools/patcher-data/clipboard.keystore"
        KEYSTORE_PASSWORD="clipboard"
        KEYSTORE_ALIAS="clipboard"
        ;;
esac

for required in "$KOTLINC" "$PATCHER_JAR" "$BT/d8" "$ANDROID_JAR"; do
    if [[ ! -e "$required" ]]; then
        echo "❌ Missing required tool: $required"
        exit 1
    fi
done

echo "==> 1/5 extension (.rve dex)"
mkdir -p "$EXT_OUT"
if [[ "$REBUILD_EXTENSION" == true || ! -f "$EXT_OUT/gboard-clipboard-extension.rve" ]]; then
    find extensions/extension/src/main/java -name "*.java" > "$BUILD_DIR/extension-sources.txt"
    rm -rf "$EXT_OUT/classes" && mkdir -p "$EXT_OUT/classes"
    javac -nowarn -d "$EXT_OUT/classes" -cp "$ANDROID_JAR" "@$BUILD_DIR/extension-sources.txt"
    jar cf "$EXT_OUT/ext.jar" -C "$EXT_OUT/classes" .
    "$BT/d8" --release --min-api 29 --lib "$ANDROID_JAR" --output "$EXT_OUT" "$EXT_OUT/ext.jar"
    echo "    built $EXT_OUT/classes.dex"
else
    echo "    (unchanged, reusing)"
fi

echo "==> 2/5 patch module (kotlinc)"
rm -rf "$PATCH_CLASSES" && mkdir -p "$PATCH_CLASSES"
find patches/src/main/kotlin -name "*.kt" > "$BUILD_DIR/patch-sources.txt"
"$KOTLINC" -nowarn -Xcontext-parameters \
    -cp "$PATCHER_JAR" \
    -d "$PATCH_CLASSES" "@$BUILD_DIR/patch-sources.txt"

echo "==> 3/5 patch dex"
rm -rf "$PATCH_DEX" && mkdir -p "$PATCH_DEX"
"$BT/d8" --release --min-api 29 --lib "$PATCHER_JAR" --output "$PATCH_DEX" \
    $(find "$PATCH_CLASSES" -name "*.class")

echo "==> 4/5 assemble .mpp"
rm -rf "$MPP_STAGE" && mkdir -p "$MPP_STAGE/extensions" "$MPP_STAGE/META-INF"
cat > "$MPP_STAGE/META-INF/MANIFEST.MF" <<EOF
Manifest-Version: 1.0
Name: Gboard Clipboard History
Description: Standalone clipboard history manager for Gboard with search, export, launcher icon, and ContentProvider API.
Version: $VERSION
Timestamp: $(date +%s000)
Source: https://github.com/sj010/gboard-clipboard-history
Author: sj010
Contact: https://github.com/sj010/gboard-clipboard-history/issues
Website: https://github.com/sj010/gboard-clipboard-history
License: GPLv3
Patcher-Version: 1.8.0
EOF
cp "$PATCH_DEX/classes.dex" "$MPP_STAGE/classes.dex"
cp "$EXT_OUT/classes.dex" "$MPP_STAGE/extensions/gboard-clipboard-extension.rve"
if [[ -f "$PATCH_CLASSES/META-INF/main.kotlin_module" ]]; then
    cp "$PATCH_CLASSES/META-INF/main.kotlin_module" "$MPP_STAGE/META-INF/patches.kotlin_module"
fi
cp -R "$PATCH_CLASSES/com" "$MPP_STAGE/com"
# Ship the patch metadata inside the bundle so Morphe Manager can list the patches.
cp patches-list.json patches-bundle.json "$MPP_STAGE/"
mkdir -p patches
rm -f "$MPP_OUT"
(cd "$MPP_STAGE" && zip -q -r -X "$SCRIPT_DIR/$MPP_OUT" .)

echo "    built $MPP_OUT ($(du -h "$MPP_OUT" | cut -f1))"

if [[ -z "$STOCK_APK" ]]; then
    echo "==> 5/5 skipped (no --patch given)"
    echo "✅ Done. Patch with: $JAVA -jar $PATCHER_JAR patch --patches=$MPP_OUT ... <gboard.apk>"
    exit 0
fi

echo "==> 5/5 patch stock APK"
mkdir -p output tools/patcher-data
if [[ ! -f "$KEYSTORE_PATH" ]]; then
    echo "    generating signing keystore..."
    mkdir -p "$(dirname "$KEYSTORE_PATH")"
    keytool -genkeypair \
        -keystore "$KEYSTORE_PATH" \
        -alias "$KEYSTORE_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass "$KEYSTORE_PASSWORD" \
        -keypass "$KEYSTORE_PASSWORD" \
        -dname "CN=$SIGNER_NAME, OU=Tools, O=OpenSource, L=Unknown, ST=Unknown, C=US" \
        >/dev/null 2>&1
fi

"$JAVA" -jar "$PATCHER_JAR" patch \
    --patches="$MPP_OUT" \
    --signer="$SIGNER_NAME" \
    --keystore="$KEYSTORE_PATH" \
    --keystore-password="$KEYSTORE_PASSWORD" \
    --keystore-entry-alias="$KEYSTORE_ALIAS" \
    --keystore-entry-password="$KEYSTORE_PASSWORD" \
    --temporary-files-path="tools/patcher-data/tmp" \
    -o="output/Gboard-com.sj010.gboard.apk" \
    -r="output/patching-result-local.json" \
    "$STOCK_APK"

echo "✅ Done: output/Gboard-com.sj010.gboard.apk ($(du -h output/Gboard-com.sj010.gboard.apk | cut -f1))"
