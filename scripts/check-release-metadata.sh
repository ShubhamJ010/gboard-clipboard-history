#!/usr/bin/env bash
# ==============================================================================
# Check that every release metadata file carries the same version.
#
# The version lives in gradle.properties. patches-list.json, patches-bundle.json
# and the patch constants have to agree with it, and the copies that the
# Gradle build packages have to be identical to the copies in the repository
# root. A mismatch means a user installs a bundle that was never reviewed.
#
#   ./scripts/check-release-metadata.sh
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

fail() {
    printf 'ERR: %s\n' "$1" >&2
    exit 1
}

VERSION="$(grep -E '^version\s*=' "$PROJECT_ROOT/gradle.properties" | cut -d'=' -f2 | tr -d ' ')"
[[ -n "$VERSION" ]] || fail "cannot read version from gradle.properties"

json_version() {
    grep -m1 -E '"version"[[:space:]]*:' "$1" | sed -E 's/.*"version"[[:space:]]*:[[:space:]]*"([^"]+)".*/\1/'
}

check_json_version() {
    local file="$1" found
    found="$(json_version "$file")"
    [[ "$found" == "$VERSION" ]] \
        || fail "$file reports version '$found', gradle.properties says '$VERSION'"
}

check_json_version "$PROJECT_ROOT/patches-list.json"
check_json_version "$PROJECT_ROOT/patches-bundle.json"
check_json_version "$PROJECT_ROOT/patches/src/main/resources/patches-list.json"
check_json_version "$PROJECT_ROOT/patches/src/main/resources/patches-bundle.json"

diff -q "$PROJECT_ROOT/patches-list.json" \
    "$PROJECT_ROOT/patches/src/main/resources/patches-list.json" >/dev/null \
    || fail "patches-list.json differs between the repository root and patches/src/main/resources"
diff -q "$PROJECT_ROOT/patches-bundle.json" \
    "$PROJECT_ROOT/patches/src/main/resources/patches-bundle.json" >/dev/null \
    || fail "patches-bundle.json differs between the repository root and patches/src/main/resources"

grep -q "/$VERSION/" "$PROJECT_ROOT/patches-bundle.json" \
    || fail "patches-bundle.json download_url does not point at a $VERSION release"

KOTLIN_VERSION="$(grep -m1 'PATCH_VERSION' "$PROJECT_ROOT/patches/src/main/kotlin/com/sj010/gboardclipboard/patches/Constants.kt" \
    | sed -E 's/.*"([^"]+)".*/\1/')"
[[ "$KOTLIN_VERSION" == "$VERSION" ]] \
    || fail "Constants.kt PATCH_VERSION is '$KOTLIN_VERSION', gradle.properties says '$VERSION'"

printf 'release metadata OK (version %s)\n' "$VERSION"
