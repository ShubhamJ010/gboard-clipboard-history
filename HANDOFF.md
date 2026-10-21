# Handoff — Gboard Clipboard History (Morphe patch)

> **Date:** 2026-10-21
> **Repo:** `/Users/sj/Desktop/ws/gboard-clipboard-history`
> **Remote:** `https://github.com/ShubhamJ010/gboard-clipboard-history` (branch `main`)
> **Sibling checkout:** `/Users/sj/Desktop/ws/PixelBoard` (Akshaykadam/PixelBoard fork, has patcher.jar, kotlinc, APKEditor)

---

## 1. Goal

Ship a Morphe (`.mpp`) patch that adds persistent clipboard history to Gboard, and produce three
patched APKs from it:

| App | Script | Output | Package |
| :--- | :--- | :--- | :--- |
| 1 | `scripts/build-gboard-clipboard.sh` | `output/Gboard-com.sj010.gboard.apk` | `com.sj010.gboard` |
| 2 | `scripts/build-gboard-clipboard-pixelboard.sh` | `output/Gboard-PixelBoard-akshaykadam.apk` | `com.akshaykadam.pixelboard` |
| 3 | `scripts/build-pixelboard-clipboard.sh` | `output/PixelBoard-clipboard-sj010.apk` | `com.akshaykadam.pixelboard` |

Apps 2 and 3 share a package name → only one can be installed at a time.

---

## 2. Current state

* Working tree clean, everything committed and pushed.
* HEAD = `ae0f184` ("fix(extension): compile against current Android SDK …").
* Tag `0.0.3-alpha` force-updated to that commit and pushed.
* All three APKs built and verified locally (authority/permission/package checks pass, see §6).
* Device `192.168.1.80:43159` (OnePlus 8T) has **`com.sj010.gboard`** (App 1, latest build) and
  **`com.akshaykadam.pixelboard`** (App 2) installed. Both are registered IMEs.

### Still open (next session should start here)

1. **GitHub release `0.0.3-alpha` is NOT published yet.** The tag exists, but the CI workflow
   (`.github/workflows/build.yml`) is the only thing that creates the release, and it was failing
   before the last fix. Check it, then publish the release manually if needed:
   ```bash
   cd /Users/sj/Desktop/ws/gboard-clipboard-history
   cp patches/GboardClipboardHistory.mpp output/patches-0.0.3-alpha.mpp
   gh release create 0.0.3-alpha \
     --title "Gboard Clipboard History 0.0.3-alpha" \
     --notes-file <notes> \
     output/patches-0.0.3-alpha.mpp \
     output/Gboard-com.sj010.gboard.apk \
     output/Gboard-PixelBoard-akshaykadam.apk \
     output/PixelBoard-clipboard-sj010.apk
   ```
   (`gh` is installed and authenticated as `ShubhamJ010`.)
2. **CI is red on `main` and on the tag** (last run before `ae0f184`). The failure was the
   extension compile error that `ae0f184` fixes — re-run and confirm green.
   `gh run list` / `gh run view <id> --log-failed`.
3. The version in `patches-bundle.json` points at
   `releases/download/0.0.3-alpha/patches-0.0.3-alpha.mpp` — only valid once the release exists.

---

## 3. Repo layout (after the scripts refactor)

```
gboard-clipboard-history/
├── extensions/extension/src/main/java/com/sj010/gboardclipboard/extension/
│   ├── GboardClipboardHistoryActivity.java     # Material 3 UI
│   ├── GboardClipboardHistoryProvider.java     # read-only ContentProvider
│   ├── GboardClipboardHistoryRepository.java    # SQLite reader/writer
│   ├── GboardClipboardHistorySettings.java     # prefs + authority/permission resolution
│   ├── GboardClipboardBackfill.java
│   └── GboardClipboardSyncRuntime.java
├── patches/src/main/kotlin/com/sj010/gboardclipboard/patches/
│   ├── Constants.kt                 # compat: Gboard + PixelBoard only (SJ targets removed)
│   ├── ClipboardHistoryPatch.kt     # manifest activity/provider + settings entry
│   ├── ClipboardCarrierPatch.kt     # merges the extension DEX
│   ├── GboardClipboardSyncPatch.kt  # onStartInputView hook (Lfml;.ft)
│   ├── GboardPackageRenamePatch.kt  # rename -> com.sj010.gboard + provider identity
│   └── XmlManifestUtils.kt
├── scripts/
│   ├── lib/common.sh                            # tool/keystore/input/patcher helpers
│   ├── build-mpp.sh                             # .mpp bundle (kotlinc + d8 + patcher)
│   ├── build-gradle.sh                          # gradle bundle (needs GITHUB_TOKEN)
│   ├── build-gboard-clipboard.sh                # App 1
│   ├── build-gboard-clipboard-pixelboard.sh     # App 2
│   ├── build-pixelboard-clipboard.sh            # App 3
│   ├── build-all.sh
│   └── install.sh                               # adb install helper
├── input/        # gitignored build inputs (see §5)
├── output/       # gitignored build outputs
└── patches/GboardClipboardHistory.mpp          # gitignored built bundle
```

`build.sh` / `build-local.sh` were moved into `scripts/` and renamed. The old
`HANDOFF.md` and `HANDOFF-CLIPBOARD-UI.md` were deleted.

---

## 4. Build environment

| Tool | Location |
| :--- | :--- |
| Java 27 (≥21 required) | `/opt/homebrew/opt/openjdk/bin/java` |
| Morphe patcher | `../PixelBoard/tools/patcher.jar` (copied to `tools/patcher.jar`, downloadable from Akshay's repo `tools/patcher.jar`) |
| kotlinc | `../PixelBoard/scratch/kotlinc/bin/kotlinc` |
| d8 / aapt2 / apksigner | `~/Library/Android/sdk/build-tools/35.0.0/` |
| android.jar | `~/Library/Android/sdk/platforms/android-36/android.jar` |
| APKEditor | `../PixelBoard/scratch/APKEditor.jar` |
| keystore (default) | `../PixelBoard/tools/patcher-data/pixelboard.keystore` (alias/password `pixelboard`, cert `CN=PixelBoard`, SHA-256 `e3451251…`) |
| keystore (alt) | `SIGNING_KEY=clipboard` → `tools/patcher-data/clipboard.keystore` (`CN=sj010`) |

```bash
cd /Users/sj/Desktop/ws/gboard-clipboard-history
./scripts/build-all.sh                     # bundle + all three APKs (~6 min)
./scripts/install.sh output/Gboard-com.sj010.gboard.apk --device 192.168.1.80:43159
```

---

## 5. Build inputs (`input/`, gitignored)

| File | Source |
| :--- | :--- |
| `input/gboard-merged.apk` | Gboard v18.4.1.985164140-release-arm64-v8a base, merged from Akshay's split APKs. Also accepts `input/splits/` (auto-merged with APKEditor) or `GBOARD_APK=<path>`. |
| `input/patches-1.2.0.mpp` | PixelBoard bundle from `Akshayykadam/PixelBoard` release `v18.4.1-Stable-V7`. |
| `input/PixelBoard-v18.4.1-Stable-V7.apk` | Latest released PixelBoard APK from the same release. |

GitHub's 100 MB file limit means none of these can be committed.

---

## 6. Verification checklist (all three APKs pass)

* `aapt2 dump badging` → correct package + `versionName 18.4.1.985164140-release-arm64-v8a`.
* Manifest: our activity (`exported=false`, no LAUNCHER/MAIN filter), our provider
  (`exported=true`), exactly **one** clipboard permission declaration.
* Authority / permission per host (see §7).
* All six `com.sj010.gboardclipboard.extension.*` classes present in the dex.
* `res/xml/setting_clipboard.xml` contains the `pref_key_clipboard_history_manager`
  HeaderPreference; `settings.xml` contains **no** clipboard entry (entry only lives in the
  Clipboard section).
* `android:targetPackage` of the entry = the final host package.

Quick check:

```bash
AAPT=~/Library/Android/sdk/build-tools/35.0.0/aapt2
$AAPT dump xmltree output/Gboard-com.sj010.gboard.apk --file AndroidManifest.xml \
  | grep -E "READ_CLIPBOARD_HISTORY|gboardclipboard.history"
```

---

## 7. Provider identity rules (important, easy to break)

Authority + signature permission are global namespaces, so two patched keyboards clashed with
`INSTALL_FAILED_CONFLICTING_PROVIDER`. Current rule:

| Host package | Authority | Permission |
| :--- | :--- | :--- |
| `com.sj010.gboard` | `com.sj010.gboardclipboard.history` | `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY` |
| `com.akshaykadam.pixelboard` | `com.akshaykadam.pixelboard.gboardclipboard.history` | `com.akshaykadam.pixelboard.permission.READ_CLIPBOARD_HISTORY` |

* `ClipboardHistoryPatch` derives both from the manifest package it sees.
* `GboardPackageRenamePatch` normalises them to the `com.sj010.gboard` values after a rename,
  using a **raw text** rewrite of `AndroidManifest.xml` (DOM attribute mutations of the decoded
  manifest are *not* flushed to the workspace — the same trap as `res/xml/*.xml`).
* The extension resolves the authority from the package manager and accepts both permission
  names, so runtime and manifest always agree.

---

## 8. Hard-won gotchas (do not repeat)

1. **Morphe CLI scoping**: `-e`, `-d`, `-O` are *patch scoped* — they must sit next to the `-p`
   they belong to. `-p A -d "X" -p B` disables `X` in bundle **A**. `-O` without `-e` is an
   error.
2. **Patch order**: PixelBoard's `Package Rename` works in the patcher *finalize* phase, so in a
   single run it lands **after** our clipboard patch → `res/xml/setting_clipboard.xml` kept the
   old `targetPackage`. Hence App 2 is a two stage build.
3. **App 1 stage order** is now: stage 1 clipboard history (stock package) → stage 2 package
   rename. The rename stage rewrites the provider identity to the documented values.
4. **DOM mutations are unreliable** for `res/xml/*.xml` **and** `AndroidManifest.xml`. Prefer raw
   text rewrites there.
5. **Two APKs with the same package name cannot be installed together** (App 2 vs App 3).
6. **Signing**: Android refuses updates when the certificate differs. Keep
   `SIGNING_KEY=pixelboard` for the already installed builds, or uninstall first
   (`--uninstall` wipes app data, including the clipboard history database).
7. **Gboard's own clipboard must be enabled** (`Gboard Settings → Clipboard → Turn on
   Clipboard`), otherwise `gboard_clipboard.db` stays empty and the manager shows 0 clips.
8. `GboardClipboardBackfill.backfillIfNecessary()` is still dead code — nothing calls it. Either
   wire it into the activity's `onResume()` or drop the "background backfill" claim from the
   README.

---

## 9. Device state

* `192.168.1.80:43159` — OnePlus 8T, adb over wifi.
* Installed: `com.sj010.gboard` (App 1), `com.akshaykadam.pixelboard` (App 2).
  Upstream Akshay's PixelBoard was uninstalled earlier (different signing certificate).
* Stock `com.google.android.inputmethod.latin` is untouched.

## 10. Next session checklist

- [ ] `gh run list` → CI green on the tag `0.0.3-alpha` (fix landed in `ae0f184`).
- [ ] Publish the GitHub release with the `.mpp` + three APKs (§2).
- [ ] Sanity check the release page and the `patches-bundle.json` download URL.
- [ ] Optional: refresh the README download table if the asset list changes.
- [ ] Optional follow-ups from §8 (backfill wiring, app label for renamed builds, Play Store
      links still pointing at `com.google.android.inputmethod.latin`).
