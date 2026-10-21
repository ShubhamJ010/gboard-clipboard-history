# Project Handoff: Gboard Clipboard History & Package Rename (`com.sj010.gboard`)

> **Update 2026-10-21** – This project was moved out of the PixelBoard checkout to
> `/Users/sj/Desktop/ws/gboard-clipboard-history` so both projects sit side by side in the root
> `ws` folder. The blocking `targetPackage` bug is **fixed and verified** (see §5).

## 1. Goal

Provide persistent, unlimited clipboard history integrated directly inside Gboard keyboard settings, packaging both:

1. **Pristine Gboard** renamed to **`com.sj010.gboard`** (to coexist alongside stock Gboard without root or signature conflict).
2. **PixelBoard** renamed to **`com.sj010.pixelboard`** (or `com.akshaykadam.pixelboard`).
3. Ensure the Clipboard History manager opens directly from Gboard settings (both main `settings.xml` and `setting_clipboard.xml`), with **NO separate home screen launcher app icon**.

---

## 2. Repositories & Key Locations

### Primary Active Repo (Standalone MPP Patch) — *this repository*
- **Path:** `/Users/sj/Desktop/ws/gboard-clipboard-history`  (was `PixelBoard/gboard-clipboard-history`)
- **GitHub:** `https://github.com/ShubhamJ010/gboard-clipboard-history`
- **Current Release:** `0.0.1-alpha` (Release bundle: `patches-1.0.0.mpp`)

### Reference / "Old" Source of Truth Repo — *sibling checkout*
- **Path:** `/Users/sj/Desktop/ws/PixelBoard/pixelboard-patches`
- **GitHub Upstream:** `https://github.com/Akshayykadam/PixelBoard`
- **Why it matters:** Contains the complete reference implementation for Gboard package renaming, preference injection, manifest attribute mappings, split APK sanitization, and dex class renaming.
- **Key Reference Files:**
  - `pixelboard-patches/patches/src/main/kotlin/com/akshaykadam/pixelboard/patches/gboard/features/packagerename/GboardPackageRenameResourcePatch.kt`: Exact XML attribute mappings, split sanitization, version code logic, and how `settingsDocuments` target package attributes are adjusted (lines 480-482 rewrite `targetPackage`, lines 501-571 validate the settings identity).
  - `pixelboard-patches/patches/src/main/kotlin/com/akshaykadam/pixelboard/patches/gboard/shared/GboardPatchesSettingsPatch.kt`: Reference for how preferences are injected into Gboard's `res/xml/settings.xml`, `settings_legacy.xml`, and `setting_clipboard.xml`.
  - `pixelboard-patches/patches/src/main/kotlin/com/akshaykadam/pixelboard/patches/gboard/features/clipboard/GboardClipboardHistoryProviderPatch.kt`: Reference for registering the read-only clipboard ContentProvider + its signature permission in the manifest.

### Sibling project
- **Path:** `/Users/sj/Desktop/ws/PixelBoard` (the PixelBoard fork, patch tooling, `input/`, `output/`, `scratch/`)

---

## 3. Current State & What Was Achieved

1. **Standalone Patch Implementation:**
   - `ClipboardHistoryPatch.kt`:
     - Injects `GboardClipboardHistoryActivity` into manifest (`exported=false`, no launcher intent filter).
     - Injects `HeaderPreference` with `pref_key_clipboard_history_manager` into `settings.xml`, `settings_legacy.xml`, and `setting_clipboard.xml`.
     - Injects string resources for localized title and summary, plus the vector entry icon.
     - Registers `GboardClipboardHistoryProvider` (`exported=true`,
       authority `com.sj010.gboardclipboard.history`) and the signature permission
       `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY`.
     - Declares `dependsOn(gboardClipboardCarrierPatch)` – without it the extension DEX is never
       merged and the manifest would reference classes that do not exist in the APK.
   - `GboardPackageRenamePatch.kt`:
     - Standalone patch `Package Rename to com.sj010.gboard`.
     - Rewrites manifest authorities, permissions, version code, and removes split APK markers.
     - Rewrites `android:targetPackage` in `res/xml/settings.xml`,
       `res/xml/settings_legacy.xml` and `res/xml/setting_clipboard.xml` (in both the `execute`
       and the `finalize` phase, so it is correct regardless of patch order).
   - `patches/src/main/resources/patches-list.json` / `patches-bundle.json`: the patch metadata is
     now packaged inside the `.mpp` (it was previously only at the repo root, so Morphe Manager
     could not list the patches) and the *Package Rename* patch is listed as well.
   - `build-local.sh`: credential free local build & patch script (kotlinc + d8 + the Morphe
     patcher jar from the sibling `PixelBoard` checkout). `build.sh`/Gradle still needs a
     GitHub token because the `app.morphe.patches` plugin comes from GitHub Packages.

2. **Patching & Device Installation:**
   - Input APK: `../PixelBoard/input/gboard-merged.apk` (Gboard v18.4.1.985164140 arm64-v8a).
   - Patched to `output/Gboard-com.sj010.gboard.apk`.
   - **Installed on connected OnePlus 8T (`192.168.1.80:43159`) successfully!**
   - Verified on device:
     - `pm list packages | grep sj010` → `com.sj010.gboard` installed!
     - `ime list -a` → `com.sj010.gboard/com.android.inputmethod.latin.LatinIME` registered!
     - Gboard settings screen launched and runs without crashing.

---

## 4. Root Cause Analysis (why the `targetPackage` bug happened)

Three independent defects were found; all are fixed now.

1. **Patch order.** Morphe sorts the applied patches by patch name, so
   `Gboard Clipboard History` (index 0) runs **before** `Package Rename to com.sj010.gboard`
   (index 1). The clipboard patch therefore read the *pre-rename* package from
   `AndroidManifest.xml` and wrote `targetPackage="com.google.android.inputmethod.latin"`,
   while the rename patch only touched the manifest.

2. **`document()` DOM writes are not flushed.** `ResourcePatchContext.document(path)` parses the
   decoded preference XML into a DOM, and `Document.close()` is supposed to write it back.
   Measured behaviour: DOM *attribute* mutations of `res/xml/*.xml` never reach the workspace
   file, while DOM *structural* changes (creating and appending the `HeaderPreference`) and
   plain `File.writeText()` edits do. The rename patch therefore performs the
   `targetPackage` rewrite directly on the raw file, and the clipboard patch enforces the
   attribute on the raw file as well.

3. **The extension DEX was never merged.** `gboardClipboardCarrierPatch` is a `bytecodePatch`
   without a name, so it is not a root patch. Morphe only schedules named patches and their
   `dependsOn` graph, so the carrier never ran: the patched APK declared
   `com.sj010.gboardclipboard.extension.GboardClipboardHistoryActivity` in the manifest but the
   class was not in any `classes*.dex` (verified: `grep GboardClipboardHistoryActivity classes*.dex`
   → no match). Fixed with `dependsOn(gboardClipboardCarrierPatch)` on the clipboard patch.

---

## 5. Verification of the Fixed Build

`./build-local.sh --signing-key pixelboard --patch ../PixelBoard/input/gboard-merged.apk` produces
`output/Gboard-com.sj010.gboard.apk`; after decoding it with APKEditor:

| Check | Result |
| :--- | :--- |
| `application-id` | `com.sj010.gboard` |
| `res/xml/settings.xml` | `android:targetPackage="com.sj010.gboard"` |
| `res/xml/settings_legacy.xml` | `android:targetPackage="com.sj010.gboard"` |
| `res/xml/setting_clipboard.xml` | `android:targetPackage="com.sj010.gboard"` |
| Manifest activity | `com.sj010.gboardclipboard.extension.GboardClipboardHistoryActivity`, `exported=false`, no launcher filter |
| Manifest provider | `com.sj010.gboardclipboard.extension.GboardClipboardHistoryProvider`, `exported=true`, authority `com.sj010.gboardclipboard.history` |
| Manifest permission | `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY`, `protectionLevel=signature` |
| DEX | all `com.sj010.gboardclipboard.extension.*` classes present in `classes.dex` |
| Resources | `gboard_clipboard_history_title` / `_summary` strings and `ic_clipboard_history_entry` drawable present |

### 5.1 On-device verification (OnePlus 8T, `192.168.1.80:43159`)

> The device had the old build installed but **disabled** and only present in the work profile
> (User 10). It was updated in place for User 0 and enabled.
> Signing note: the device build was signed with the *PixelBoard* keystore, so the new APK has to
> be signed with the same certificate or Android refuses the update
> (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`) – use `--signing-key pixelboard`.

| Step | Result |
| :--- | :--- |
| `adb install -r -g` | `Success` (in-place update, data preserved) |
| `pm enable com.sj010.gboard` | enabled |
| `ime list -a` | `com.sj010.gboard/com.android.inputmethod.latin.LatinIME` registered |
| Keyboard as active IME | renders fully, suggestions work, no crash in logcat |
| `Gboard Settings` → bottom entry | "Clipboard History" with icon + summary "Search, export, and manage persistent clips" |
| Tap the entry | `START … GboardClipboardHistoryActivity … result code=0` – **the `targetPackage` fix works** |
| Clipboard History UI | title, clip counter, search box, Export / Settings-API / Clear All all render |
| Settings / API dialog | opens, access token generated (`c4c63907…`), companion-app toggle works |
| `content query …/clips?token=…` | returns the captured row |
| `content query …/clips` (no token) | `SecurityException: Unauthorized. Requires permission … or valid query parameter '?token=…'` – correct |
| `content query …/primary?token=…` | returns the live system clipboard |
| Clip list | shows the captured clip with a relative timestamp |
| Search filter | typing `Hello` filters the list correctly |

### 5.2 Two behavioural findings

1. **Gboard's own Clipboard feature has to be enabled.** Gboard only writes to
   `gboard_clipboard.db` when its built-in clipboard is switched on
   (`Gboard Settings → Clipboard` → "Turn on Clipboard"). With it off the database stays empty and
   the manager shows "0 clips captured in Gboard" even though the system clipboard has content.
   The manager should either surface this hint or the README should call it out.
2. **`GboardClipboardBackfill.backfillIfNecessary()` is dead code** – nothing calls it
   (only `readPrimaryClip()` is used, by the provider's `/primary` path). The "Background
   backfill" feature advertised in the README therefore does not exist yet. Either wire it into
   the activity's `onResume()` or remove the claim.

## 6. Immediate Next Step for the Next Session

1. Push the fixes to `ShubhamJ010/gboard-clipboard-history`, tag `1.0.0` and let CI publish
   `patches-1.0.0.mpp` (the `patches-bundle.json` `download_url` already points at that tag).
2. Wire up `backfillIfNecessary()` (see §5.2 item 2) and add the "turn on Gboard's clipboard"
   hint to the manager UI (see §5.2 item 1).
3. Optional follow-ups that are still open compared to the PixelBoard reference implementation:
   - `res/values/strings.xml` still contains `market://details?id=com.google.android.inputmethod.latin`
     and `res/values/arrays.xml` still contains the old package name; the Play Store links should
     point at the renamed package.
   - `res/xml/xml_0x7f170022.xml` contains `<configuration-package>com.google.android.inputmethod.latin</configuration-package>`.
   - The rename patch does not yet rewrite the app label / launcher name for the renamed package.
