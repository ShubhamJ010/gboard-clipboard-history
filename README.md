# Gboard Clipboard History

A [Morphe](https://github.com/MorpheApp) patch that adds persistent clipboard history management to Google Gboard.

It runs as a standalone patch and does not require other keyboard modifications.

<p align="center">
  <a href="https://github.com/ShubhamJ010/gboard-clipboard-history/releases">
    <img src="https://img.shields.io/badge/📥_Download_APK_(Clipboard_Edition)-v18.4.1_V7_(126_MB)-00C853?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Download APK (Clipboard Edition)"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/ShubhamJ010/gboard-clipboard-history/releases">
    <img src="https://img.shields.io/badge/📦_Morphe_Patch_(.mpp)-v18.4.1_V7_(689_KB)-2979FF?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Download Morphe Patch (.mpp)"/>
  </a>
</p>

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
├── extension/                     # Android runtime extension (.rve DEX)
│   └── src/main/java/com/sj010/gboardclipboard/extension/
│       ├── GboardClipboardHistoryRepository.java   # SQLite reader and writer
│       ├── GboardClipboardHistorySettings.java     # Preference persistence
│       ├── GboardClipboardHistoryProvider.java     # ContentProvider implementation
│       ├── GboardClipboardBackfill.java            # Clipboard listener and backfill
│       └── GboardClipboardHistoryActivity.java     # Management UI
│   └── src/main/res/                               # Vector assets and strings
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
| **GboardClipboardHistory.mpp** | Morphe Patch | Patch bundle for use with Morphe Manager or CLI patcher. |

Download both files from the [Releases](https://github.com/ShubhamJ010/gboard-clipboard-history/releases) page.

## Building and Patching

### With Morphe Manager (Android)

1. Open Morphe Manager on your device.
2. Add this repository URL to sources, or download the `.mpp` patch file from the Releases page.
3. Select your installed or downloaded Gboard APK.
4. Apply the patch and install the modified APK.

### Command Line

Prerequisites: Java 21+ and a stock Gboard APK.

1. Copy the target APK to `input/gboard.apk`.
2. Run the patch script:
   ```bash
   ./build.sh
   ```
3. The patched package will be written to `output/GboardClipboard.apk`.

To recompile the `.mpp` patch bundle from source:
```bash
./build.sh --rebuild
```

## ContentProvider API

External applications (widgets, automation tasks, desktop sync tools) can query history through Android's ContentProvider interface.

### Endpoints

- `content://com.sj010.gboardclipboard.history/clips` (all history)
- `content://com.sj010.gboardclipboard.history/clips/{id}` (single entry)
- `content://com.sj010.gboardclipboard.history/primary` (active system clipboard entry)

### Authentication

External access is disabled by default and must be toggled on in the Clipboard History settings. Callers can authenticate in two ways:

1. **Signature permission**: Granted automatically if the calling app shares the same signing key:
   `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY`
2. **Access token**: Passed as a query parameter with the request:
   `content://com.sj010.gboardclipboard.history/clips?token=YOUR_API_TOKEN`

### Query Example (Kotlin)

```kotlin
val uri = Uri.parse("content://com.sj010.gboardclipboard.history/clips?token=$userToken")
val cursor = context.contentResolver.query(uri, null, null, null, null)

cursor?.use {
    val textIdx = it.getColumnIndex("text")
    val timeIdx = it.getColumnIndex("timestamp")
    while (it.moveToNext()) {
        val text = it.getString(textIdx)
        val timestamp = it.getLong(timeIdx)
        println("Clip: $text (Copied: $timestamp)")
    }
}
```

## License

This project is licensed under the [GNU General Public License v3.0 (GPLv3)](LICENSE).
