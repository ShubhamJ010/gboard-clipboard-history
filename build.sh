#!/usr/bin/env bash
# ==============================================================================
# Gboard Clipboard History - Standalone Build & Patch Script
# Maintainer: sj010
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

REBUILD_PATCH=false
for arg in "$@"; do
    case "$arg" in
        -r|--rebuild|--compile|-c)
            REBUILD_PATCH=true
            ;;
        -h|--help)
            echo "Usage: ./build.sh [options]"
            echo "Options:"
            echo "  -r, --rebuild    Recompile patch bundle from source using Gradle"
            echo "  -h, --help       Show this help message"
            echo ""
            echo "Place stock Gboard APK at input/gboard.apk to generate patched output."
            exit 0
            ;;
    esac
done

PATCH_VERSION=$(grep -E "^version\s*=" gradle.properties | cut -d'=' -f2 | tr -d ' ')
MPP_PATH="patches/GboardClipboardHistory.mpp"

if [[ "$REBUILD_PATCH" == true ]] || [[ ! -f "$MPP_PATH" ]]; then
    echo "=== 📋 Compiling Gboard Clipboard History Patch Bundle ==="
    ./gradlew :patches:buildAndroid

    COMPILED_MPP="patches/build/libs/patches-${PATCH_VERSION}.mpp"
    if [[ ! -f "$COMPILED_MPP" ]]; then
        COMPILED_MPP=$(ls patches/build/libs/*.mpp 2>/dev/null | grep -v 'javadoc' | grep -v 'sources' | head -n 1 || true)
    fi
    if [[ -z "${COMPILED_MPP:-}" ]] || [[ ! -f "$COMPILED_MPP" ]]; then
        echo "❌ Error: Patch bundle failed to build at patches/build/libs/"
        exit 1
    fi
    mkdir -p patches
    cp "$COMPILED_MPP" "$MPP_PATH"
    echo "✅ Patch bundle compiled & saved to: $MPP_PATH"
fi

# Locate Java 21+ for patcher execution
JAVA_BIN=""
for candidate in \
    "${JAVA_HOME:-}/bin/java" \
    "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" \
    "/opt/homebrew/opt/openjdk@21/bin/java" \
    /opt/homebrew/Cellar/openjdk@21/*/libexec/openjdk.jdk/Contents/Home/bin/java \
    "/opt/homebrew/opt/openjdk/bin/java" \
    "$(/usr/libexec/java_home -v 21 2>/dev/null || true)/bin/java" \
    "$(which java 2>/dev/null || true)"; do
    if [[ -n "$candidate" ]] && [[ -x "$candidate" ]]; then
        candidate_ver=$("$candidate" -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1 || true)
        if [[ -n "$candidate_ver" ]] && [[ "$candidate_ver" -ge 21 ]]; then
            JAVA_BIN="$candidate"
            break
        fi
    fi
done

if [[ ! -f "input/gboard.apk" ]]; then
    echo "ℹ️  Patch bundle ready at: $MPP_PATH"
    echo "💡 To patch Gboard directly, place a stock Gboard APK at input/gboard.apk and run ./build.sh again."
    exit 0
fi

if [[ -z "$JAVA_BIN" ]]; then
    echo "⚠️  Note: Morphe patcher requires Java 21+."
    echo "   Please install Java 21 (e.g. 'brew install openjdk@21') or specify JAVA_HOME."
    exit 1
fi

echo "=== 🔨 Applying Clipboard History Patch to input/gboard.apk ==="
mkdir -p output tools/patcher-data

KEYSTORE_PATH="tools/patcher-data/clipboard.keystore"
if [[ ! -f "$KEYSTORE_PATH" ]]; then
    echo "🔑 Generating signing keystore..."
    keytool -genkeypair \
        -keystore "$KEYSTORE_PATH" \
        -alias clipboard \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass clipboard \
        -keypass clipboard \
        -dname "CN=GboardClipboard, OU=Tools, O=OpenSource, L=Unknown, ST=Unknown, C=US" \
        >/dev/null 2>&1
fi

# Copy patcher.jar if available in parent PixelBoard tools
if [[ ! -f "tools/patcher.jar" ]] && [[ -f "../tools/patcher.jar" ]]; then
    cp "../tools/patcher.jar" "tools/patcher.jar"
fi

if [[ -f "tools/patcher.jar" ]]; then
    "$JAVA_BIN" -jar tools/patcher.jar patch \
        --patches="$MPP_PATH" \
        --signer="GboardClipboard" \
        --keystore="$KEYSTORE_PATH" \
        --keystore-password=clipboard \
        --keystore-entry-alias=clipboard \
        --keystore-entry-password=clipboard \
        --temporary-files-path="tools/patcher-data/tmp" \
        -o=output/GboardClipboard.apk \
        -r=output/patching-result.json \
        input/gboard.apk

    echo "🎉 Successfully built output/GboardClipboard.apk!"
else
    echo "⚠️  tools/patcher.jar not found. Patch with Morphe Manager or download Morphe CLI patcher."
fi
