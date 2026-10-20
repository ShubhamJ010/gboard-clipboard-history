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

- **Launcher icon**: Opens the clipboard viewer directly from the app drawer without navigating through system or keyboard settings.
- **In-app settings integration**: Adds a "Clipboard History Manager" entry to `Gboard Settings -> Clipboard`.
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
│       └── ClipboardHistoryPatch.kt                # Manifest and layout injection
├── .github/workflows/build.yml    # CI build workflow
├── build.sh                       # Local build script
├── patches-bundle.json            # Morphe Manager repository metadata
└── settings.gradle.kts            # Gradle settings and plugin configuration
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
- **Permission**: `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY` (normal protection level)
- **Columns**: `_id`, `text`, `timestamp`, `is_pinned`

## License

GPL-3.0
