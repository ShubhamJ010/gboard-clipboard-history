# Gboard Clipboard History

A [Morphe](https://github.com/MorpheApp) patch that adds persistent clipboard history management to Google Gboard.

It runs as a standalone patch and does not require other keyboard modifications.

<p align="center">
  <a href="https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History">
    <img src="https://img.shields.io/badge/Add_to-Morphe-1E88E5?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Add to Morphe"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/ShubhamJ010/gboard-clipboard-history/releases/tag/0.0.1-alpha">
    <img src="https://img.shields.io/badge/📦_Morphe_Patch_(.mpp)-v0.0.1--alpha_(676_KB)-2979FF?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Download Morphe Patch (.mpp)"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/ShubhamJ010/gboard-clipboard-history/releases/tag/0.0.1-alpha">
    <img src="https://img.shields.io/badge/📥_Release-0.0.1--alpha-00C853?style=for-the-badge&logo=github&logoColor=white" height="40" alt="Release 0.0.1-alpha"/>
  </a>
</p>

## Import to Morphe Manager

### One-Click Import (Android)

Tap the button below or link on an Android device where Morphe Manager is installed:

[![Add to Morphe](https://img.shields.io/badge/Add%20to-Morphe-1E88E5?style=for-the-badge&logo=android&logoColor=white)](https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History)

**Direct link:** [https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History](https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History)

> [!NOTE]
> If viewing on desktop, clicking the link displays a QR code to scan with your Android phone's camera.

---

### Manual Import

If the link does not open Morphe Manager automatically:

1. Open **Morphe Manager** on your Android device.
2. Tap **Sources** in the navigation bar.
3. Tap the **`+`** button (top right).
4. In the **Remote** field, enter:
   ```
   github.com/ShubhamJ010/gboard-clipboard-history
   ```
5. Tap **Add**.

---

## Features

- **In-app settings integration**: Adds a "Clipboard History" entry to Gboard Settings and to `Gboard Settings -> Clipboard`. There is **no separate launcher icon** – the manager is only reachable from inside the keyboard settings.
- **Coexistence with stock Gboard**: The optional *Package Rename to com.sj010.gboard* patch renames the keyboard package so it can be installed side by side with the official Gboard without a signature conflict.
- **Database reader**: Reads Gboard's internal SQLite database (`gboard_clipboard.db`, `clips` table) directly from the application process.
- **Background backfill**: Captures clips copied while another keyboard or application had focus once Gboard regains active input status.
- **Text export**: Exports stored history to a text file using Android Storage Access Framework.
- **Sensitive field toggle**: Controls whether entries copied from password fields are stored or shown.
- **ContentProvider API**: Exposes a read-only endpoint for automation tools, widgets, and companion apps.

## Repository Structure

```
gboard-clipboard-history/
├── extensions/
│   └── extension/                 # Android runtime extension (.rve DEX)
│       └── src/main/java/com/sj010/gboardclipboard/extension/
│           ├── GboardClipboardHistoryRepository.java   # SQLite reader and writer
│           ├── GboardClipboardHistorySettings.java     # Preference persistence
│           ├── GboardClipboardHistoryProvider.java     # ContentProvider implementation
│           ├── GboardClipboardBackfill.java            # Clipboard listener and backfill
│           └── GboardClipboardHistoryActivity.java     # Management UI
│       └── src/main/res/                               # Vector assets and strings
├── patches/                       # Morphe bytecode and resource patch bundle
│   └── src/main/kotlin/com/sj010/gboardclipboard/patches/
│       ├── Constants.kt                            # Package names, targets, and URIs
│       ├── XmlManifestUtils.kt                     # XML manipulation helpers
│       ├── ClipboardCarrierPatch.kt                # Extension DEX injection
│       ├── ClipboardHistoryPatch.kt                # Manifest, provider and settings injection
│       └── GboardPackageRenamePatch.kt             # Package rename to com.sj010.gboard
│   └── src/main/resources/                         # patches-list.json / patches-bundle.json
├── .github/workflows/build.yml    # CI build workflow
├── build.sh                       # Local build script (requires Morphe Gradle plugin)
├── build-local.sh                 # Credential free local build & patch script
├── patches-bundle.json            # Morphe Manager repository metadata
└── settings.gradle.kts            # Gradle settings and plugin configuration
```

## Building

### `build-local.sh` (no GitHub token required)

The Gradle build resolves the `app.morphe.patches` plugin from GitHub Packages and therefore
needs a `GITHUB_TOKEN`. `build-local.sh` reproduces the same `.mpp` layout with local tooling
only (`kotlinc` + `d8` + the Morphe patcher jar from the sibling `PixelBoard` checkout):

```bash
./build-local.sh                                   # build patches/GboardClipboardHistory.mpp
./build-local.sh --patch /path/to/gboard.apk       # build + patch -> output/Gboard-com.sj010.gboard.apk
./build-local.sh --rebuild-extension --patch ...   # also rebuild the runtime extension DEX
```

### `build.sh` / Gradle

```bash
./gradlew :patches:buildAndroid -Pgpr.user=<user> -Pgpr.key=<token>
```

## Downloads

| Package | Format | Description |
| :--- | :--- | :--- |
| **Gboard Clipboard Edition APK** | Signed APK | Standalone patched keyboard package with clipboard history pre-integrated. |
| **patches-1.0.0.mpp** | Morphe Patch | Patch bundle for use with Morphe Manager or CLI patcher. |

Download both files from the [Releases](https://github.com/ShubhamJ010/gboard-clipboard-history/releases/tag/0.0.1-alpha) page.

## ContentProvider API

Other apps or automation scripts (Tasker, Termux, Automate) can read clipboard history via:

- **URI**: `content://com.sj010.gboardclipboard.history/clips`
- **Permission**: `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY` (signature protection level)
- **Token fallback**: `content://com.sj010.gboardclipboard.history/clips?token=<access token>` – the token is shown in *Clipboard History -> Settings / API* and can be regenerated at any time
- **Columns**: `_id`, `text`, `html_text`, `item_type`, `entity_type`, `timestamp`, `sensitive`, `uri`, `group_id`
- **Extra path**: `content://com.sj010.gboardclipboard.history/primary` returns the current primary clipboard entry

External access is disabled by default and has to be enabled in *Clipboard History -> Settings / API*.

## License

GPL-3.0
