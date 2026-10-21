# Handoff: Bring `com.sj010.gboard` Clipboard History up to the old PixelBoard quality

> **Paste this whole file into the new session.** It is self-contained.
> Everything below was established by inspecting the code, building, and testing on the real
> device (OnePlus 8T). Trust the "Gotchas" section — each item cost real debugging time.

---

## 1. Goal

The standalone project `/Users/sj/Desktop/ws/gboard-clipboard-history` currently ships a
**rough, hand-rolled clipboard history screen**. The user explicitly says it looks like
**garbage** compared to the old PixelBoard implementation.

**Task:** rebuild the Clipboard History feature so it has the *look, structure and behaviour*
of the old project, while keeping the standalone project's architecture (it is a single
`.mpp` patch for stock Gboard, not the full PixelBoard fork).

Non-goal: do **not** port PixelBoard's whole settings framework. See §5.

---

## 2. Where everything is

| What | Path |
| :--- | :--- |
| **Active project (do the work here)** | `/Users/sj/Desktop/ws/gboard-clipboard-history` |
| Old reference implementation | `/Users/sj/Desktop/ws/PixelBoard/pixelboard-patches` |
| Morphe patcher + stock APK | `/Users/sj/Desktop/ws/PixelBoard/tools/patcher.jar`, `/Users/sj/Desktop/ws/PixelBoard/input/gboard-merged.apk` |
| Test device | OnePlus 8T, `adb connect 192.168.1.80:43159` |

Old project reference files (read these — they are the spec for the target look):

| File | What to copy from it |
| :--- | :--- |
| `pixelboard-patches/extensions/extension/src/main/java/com/akshaykadam/pixelboard/extension/settings/GboardClipboardHistoryFeature.java` | **The screen definition.** 419 lines. Sections, rows, empty/error states, entry previews, image handling. This is the single most important file to read. |
| `.../extension/clipboard/GboardClipboardSyncRuntime.java` | Foreground clipboard sync + back-fill scheduling. **Completely missing from the standalone repo.** Fixes Samsung One UI / Vivo OriginOS OEM clipboard suppression (old project's Issue #17). |
| `.../extension/clipboard/GboardClipboardBackfill.java` | Mature back-fill: distinguishes image clips from text, conservative, never duplicates, never logs clip content. The standalone copy is a stub (see §7). |
| `.../extension/clipboard/GboardClipboardHistoryProvider.java` | Provider with `/clips`, `/clips/{id}`, `/primary`, projection + sort handling, two-tier auth. |
| `.../extension/clipboard/GboardClipboardHistoryRepository.java` | The SQLite layer. Already ~90% identical in the standalone repo. |
| `.../extension/settings/GboardSettingsText.java` + `src/main/settings-text/gboard_settings_text.xml` | Localization: resolves strings in the **keyboard's** language, not the system locale. 41 clipboard strings. |
| `.../extension/src/main/res/values/colors.xml` + `values-night/colors.xml` | Light/dark colour tokens (the old project's "look"). |
| `.../extension/build.gradle.kts` (lines 50-215) | The `settings-text` → generated resources + `GboardSettingsTextCatalog.java` pipeline. |

---

## 3. What already works — do NOT redo this

The plumbing is **solid and verified on device**. Commits: `943c4e4`, `0344153`, `243d2b7`, `8bfa98a`.

- ✅ `com.sj010.gboard` package rename patch works, Gboard installs side by side with stock.
- ✅ `Clipboard History` entry is injected into Gboard's `settings.xml`, `settings_legacy.xml`
  and `setting_clipboard.xml`, and opens `GboardClipboardHistoryActivity` (`exported=false`,
  **no launcher icon** — keep it that way).
- ✅ The runtime extension DEX is merged into the APK (the carrier patch is a `dependsOn` of
  the clipboard patch).
- ✅ The read-only ContentProvider is registered
  (`content://com.sj010.gboardclipboard.history/clips`, `…/primary`) with a signature-level
  permission `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY` and per-install
  access-token auth. Both auth paths are verified.
- ✅ The repository reads Gboard's `gboard_clipboard.db` `clips` table correctly.
- ✅ `build-local.sh` builds + patches without a GitHub token.

Verified on device: entry renders, activity opens without crashing, provider returns rows,
token auth works, unauthorized access throws `SecurityException`.

---

## 4. The gap — why it looks like garbage

### 4.1 The current screen (`GboardClipboardHistoryActivity.java`, 430 lines)

It is a raw `LinearLayout` built in Java with **hard-coded colours**:

```java
root.setBackgroundColor(0xFF121212);      // always dark
header.setBackgroundColor(0xFF1E1E1E);
b.setBackgroundColor(0xFF333333);
```

Consequences and gaps:

1. **No light mode, no theme.** Hard-coded `#FF121212`/`#FF1E1E1E` look wrong in light mode and
   do not match Gboard's own settings styling, even though the activity *inherits Gboard's
   settings theme* (`style_0x7f150431`).
2. **No section structure.** Everything is one flat screen: a header, a search `EditText`, three
   buttons, a `ListView`. The old project has *Recent entries* / *External access* / *Danger
   zone* sections with headers and descriptions.
3. **Two hard-coded English strings** (`gboard_clipboard_history_title`, `…_summary`) injected
   into `res/values*/strings.xml` by the patch. The old project has **41** localized strings.
4. **No localization at all** — never speaks the user's language, let alone the keyboard's.
5. **Settings/API is a raw `AlertDialog`** with a `ScrollView` and two `Switch`es, instead of
   proper settings rows.
6. **No image clip support.** The old project detects a clip with empty text + a `uri` and opens
   it via a content URI grant; the standalone shows the raw URI in a toast.
7. **No auto-refresh.** The list only reloads in `onResume()`.
8. **No empty/error state design.** The empty state is a plain centred `TextView`.
9. **No truncation disclosure.** The old project shows "showing 10 of N" when it lists only the
   most recent entries.
10. **No hint when Gboard's own clipboard is off** (the #1 support question — see §7).
11. **"Background backfill" is advertised but does not exist** —
    `GboardClipboardBackfill.backfillIfNecessary()` is **never called** (dead code).
12. **No foreground clipboard sync** — on Samsung One UI / Vivo OriginOS the OEM suppresses
    `OnPrimaryClipChangedListener` broadcasts to third-party keyboards, so the history stays
    empty. The old project fixed this with `GboardClipboardSyncRuntime`.

### 4.2 The target look (old project)

- Gboard-native settings **sub-screen** with a toolbar, header badge, header title/summary.
- `StatusBlock`s for empty / warning / error states.
- `Section`s: *Recent entries* (with a capture-window note), *External access* (description +
  rows), *Danger zone*.
- Row types: `NavigationRow` (with thumbnail), `ToggleRow`, `CopyRow`, `CommandRow`,
  `DetailRow`, `DangerRow` (with confirm dialog).
- Light/dark themed colour tokens.
- 5 s auto-refresh (`RefreshPolicy.fullScreen(5000)`).
- Entry preview truncated to 72 chars with an ellipsis, relative timestamp, image entries
  labelled and openable.
- 41 localized strings, resolved in the **keyboard's** language.

---

## 5. Recommended architecture (read before writing code)

### 5.1 Do NOT port PixelBoard's settings framework

It is **6 717 lines** (`GboardPatchesSettingsActivity` alone is 4 629 lines) and is coupled to
PixelBoard's other features (speech services, writing tools, emoji, accessibility…). Porting it
into a standalone clipboard patch is a rewrite, not a port.

### 5.2 Do this instead: render the screen as a native androidx.preference screen

Gboard's settings already use **androidx.preference** (the injected
`com.google.android.libraries.inputmethod.settings.widget.HeaderPreference` extends
`androidx.preference.Preference`). So:

- Replace the `<intent>` on the injected preference with a **fragment reference**, e.g.
  `android:fragment="com.sj010.gboardclipboard.extension.ui.ClipboardHistoryFragment"`,
  and make the fragment a `PreferenceFragmentCompat` subclass.
- The screen then renders **inside Gboard's own settings**, inheriting Gboard's theme,
  typography, dividers, scroll behaviour and light/dark support **for free** — which is exactly
  the "look" the old project achieves, with ~300–500 lines instead of 6 717.
- Use `PreferenceCategory` headers for *Recent entries* / *External access* / *Danger zone*,
  `SwitchPreferenceCompat` for the toggles, `Preference` subclasses for copy/command rows, and
  a `RecyclerView`-backed `Preference` for the clip list (or a `ListView` inside a custom
  preference, but a `RecyclerView` adapter is what the old project uses for thumbnails).

**Validate this route first** (§6 step 1) — if the fragment route does not work on Gboard 18.4.1,
fall back to a dedicated Activity that resolves colours from **Gboard theme attributes**
(`?attr/colorSurface`, `?attr/colorOnSurface`, `?android:attr/textColorPrimary`, …) instead of
hard-coding them, plus a `values-night` variant. That alone removes most of the "garbage" look.

### 5.3 Localization

Copy the 41 clipboard strings from the old project's
`src/main/settings-text/gboard_settings_text.xml` into the standalone repo, and copy the
`GboardSettingsText` resolution logic (resolve in the **keyboard's** language, not the system
locale).

> **Constraint:** the standalone repo's `build-local.sh` compiles the extension to a **DEX only**
> — it does **not** package extension resources (the Gradle `extension { }` plugin does).
> So `R.string.*` / `R.color.*` are **not available in local builds**.
>
> Two options:
> a) Extend `build-local.sh` to also build + package the extension resources into the `.rve`
>    (needs `aapt2` + the resource table) — the honest fix.
> b) Keep injecting strings from the **patch** side (`ClipboardHistoryPatch.kt` already writes
>    `res/values*/strings.xml`), and have the extension resolve them by **name** through
>    `getIdentifier()` at runtime, with a hard-coded English fallback. This keeps the local
>    build working but is uglier.
>
> Prefer (a); fall back to (b) if (a) turns into a rabbit hole.

---

## 6. Task list (ordered — do them in this order)

**Phase 0 — setup (5 min)**
0. `cd /Users/sj/Desktop/ws/gboard-clipboard-history`, branch from `main`.
1. Start the Mac `caffeinate` and the device anti-sleep (see §8).

**Phase 1 — make the screen native (the big win)**
2. **Spike first:** inject the preference with `android:fragment=` pointing at an empty
   `PreferenceFragmentCompat` and confirm it renders inside Gboard's settings on the device.
   *If this fails, stop and fall back to 5.2's Activity fallback before writing any UI.*
3. Rebuild the screen as a preference screen with the three sections.
4. Replace every hard-coded colour/string with a theme attribute or a localized string.
5. Add the empty state, the error state and the "showing N of M" disclosure.
6. Add image-clip support: empty text + non-empty `uri` → open the content URI with a read
   grant (`FLAG_GRANT_READ_URI_PERMISSION`).
7. Add 5 s auto-refresh while the screen is visible.
8. Add the "Gboard's clipboard is off" hint (see §7).

**Phase 2 — runtime gaps**
9. Port `GboardClipboardSyncRuntime` (foreground sync + back-fill scheduling) from the old
   project → fixes Samsung/Vivo OEM clipboard suppression.
10. Wire `GboardClipboardBackfill.backfillIfNecessary()` into the read path (it is currently
    dead code) and port the old project's image-vs-text `PrimaryClip` snapshot.
11. Port the improved provider: add `/clips/{id}`, honour `projection` and `sortOrder`.

**Phase 3 — polish**
12. Localize all strings (§5.3).
13. Long-press a clip → details dialog (the standalone already has one; restyle it).
14. Keep the SAF text export; make sure it respects the new theme.

**Phase 4 — verify**
15. Rebuild, patch, install, and run the full checklist in §9.

---

## 7. Gotchas (hard-won — do not rediscover these)

1. **`document()` DOM writes do not persist.** `ResourcePatchContext.document(path)` +
   `close()` does **not** write DOM *attribute* mutations of `res/xml/*.xml` back to the
   workspace file (measured: the file was byte-identical after `close()`). Structural changes
   (creating/appending an element) and plain `File.writeText()` **do** work.
   → For preference XML edits, **write the raw file**, not the DOM.
2. **Morphe sorts patches by name.** `Gboard Clipboard History` runs **before**
   `Package Rename to com.sj010.gboard`. Do not rely on that order — make each patch
   order-independent (write in `execute` *and* `finalize`).
3. **An unnamed `bytecodePatch` never runs.** Morphe only schedules named patches and their
   `dependsOn` graph. The extension carrier must stay a `dependsOn` of the clipboard patch.
4. **Signing.** The device build is signed with the **PixelBoard** keystore. A different
   certificate → `INSTALL_FAILED_UPDATE_INCOMPATIBLE`. Always build with
   `./build-local.sh --signing-key pixelboard …`.
5. **The device state is odd.** `com.sj010.gboard` was installed but **disabled** and only
   present in the **work profile (User 10)**. After installing:
   `adb shell pm enable com.sj010.gboard` and
   `adb shell ime enable com.sj010.gboard/com.android.inputmethod.latin.LatinIME`.
6. **Gboard's own clipboard must be ON.** Gboard only writes `gboard_clipboard.db` when its
   built-in clipboard is enabled (`Gboard Settings → Clipboard` → "Turn on Clipboard"). With it
   off, the manager shows "0 clips captured in Gboard" even though the system clipboard has
   content — this was the *actual* reason the list looked empty during testing. The new UI
   should detect this (DB empty **and** system clipboard non-empty) and show a "Turn on
   Gboard's clipboard" hint.
7. **`cmd clipboard` is not implemented** on this device's Android build — you cannot set the
   clipboard from adb. Copy text through the UI instead (focus a field, `input text`, then
   `input keycombination 113 29` (Ctrl+A) and `113 31` (Ctrl+C)).
8. **`adb root` does not work** (production build), so you cannot read the app's private
   database directly. Verify through the exported ContentProvider instead:
   `adb shell content query --uri "content://com.sj010.gboardclipboard.history/clips?token=<token>"`.
9. **The Clipboard History activity is `exported=false`**, so `adb shell am start -n …` fails
   with a SecurityException. Navigate through Gboard's settings instead.

---

## 8. Build, install and verify commands

```bash
cd /Users/sj/Desktop/ws/gboard-clipboard-history

# anti-sleep (Mac + device)
caffeinate -dimsu -t 7200 &
adb shell svc power stayon true
adb shell settings put global stay_on_while_plugged_in 3

# build the .mpp and patch stock Gboard
./build-local.sh --signing-key pixelboard \
    --patch ../PixelBoard/input/gboard-merged.apk

# install (in-place update, keeps data)
adb install -r -g output/Gboard-com.sj010.gboard.apk
adb shell pm enable com.sj010.gboard
adb shell ime enable com.sj010.gboard/com.android.inputmethod.latin.LatinIME

# open the screen the way a user would
adb shell am start -n com.sj010.gboard/com.google.android.apps.inputmethod.latin.preference.SettingsActivity
# then scroll to the bottom and tap "Clipboard History"

# verify the provider
TOKEN=<token shown in Clipboard History -> Settings / API>
adb shell content query --uri "content://com.sj010.gboardclipboard.history/clips?token=$TOKEN"
adb shell content query --uri "content://com.sj010.gboardclipboard.history/primary?token=$TOKEN"
adb shell content query --uri "content://com.sj010.gboardclipboard.history/clips"   # must throw SecurityException

# decode the patched APK to inspect resources
java -Xmx4g -jar ../PixelBoard/scratch/APKEditor.jar d \
    -i output/Gboard-com.sj010.gboard.apk -o build/verify/decoded -f
```

Screenshots: `adb shell screencap -p /sdcard/x.png && adb pull /sdcard/x.png /tmp/x.png`,
then read `/tmp/x.png`.

---

## 9. Definition of done

- [ ] The Clipboard History screen renders **inside Gboard's settings** with Gboard's own theme
      (light **and** dark), not a hand-styled black activity.
- [ ] Three sections: *Recent entries* / *External access* / *Danger zone*.
- [ ] Clip list shows text preview (truncated), relative timestamp, and opens image clips.
- [ ] "Showing N of M" disclosure when the list is truncated.
- [ ] Empty state, error state, and the "turn on Gboard's clipboard" hint.
- [ ] Toggles for *Allow companion apps* and *Include sensitive clips* work and persist.
- [ ] Access token can be copied and regenerated.
- [ ] Provider URI + permission + endpoints are visible and copyable.
- [ ] "Clear all" asks for confirmation and clears the DB.
- [ ] Auto-refresh every ~5 s while visible.
- [ ] All strings localized (at least `en` + the languages the old project ships).
- [ ] No hard-coded colours left in the extension code.
- [ ] Foreground clipboard sync works (Samsung/Vivo clip capture fixed).
- [ ] `backfillIfNecessary()` is actually called; the README claim is true.
- [ ] The entry still has **no launcher icon** and the activity is still `exported=false`.
- [ ] Verified on the device: entry → activity opens, list shows a real captured clip, provider
      returns it, unauthorized access is rejected, no `FATAL` in logcat.
- [ ] Commit + push to `ShubhamJ010/gboard-clipboard-history`; update `HANDOFF.md`.

---

## 10. Current state of the repo

```
8bfa98a docs: record on-device verification and two behavioural findings
243d2b7 build: add --signing-key option to build-local.sh
0344153 docs: add handoff document for the moved project
943c4e4 fix: correct settings targetPackage, merge extension DEX, register clipboard provider
57c9324 Add Package Rename to com.sj010.gboard patch
```

Working tree was clean at the time of writing.
The `output/Gboard-com.sj010.gboard.apk` in the repo is the build that is currently installed
on the device and was verified end to end.
