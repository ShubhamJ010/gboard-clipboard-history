# Gboard Clipboard History

A standalone, modular [Morphe](https://github.com/MorpheApp) patch that unlocks a full-featured Clipboard History manager directly in Google Gboard.

Unlike multi-feature mod suites, **Gboard Clipboard History** is dedicated solely to clipboard history with zero bloat and no external mod dependencies.

---

## 🌟 Key Features

- 📱 **Dual UI Access**:
  - **App Drawer Launcher Icon**: Direct home screen launcher icon (`Clipboard History`) to view, search, and manage clips without digging through settings.
  - **In-Keyboard Settings**: Native entry in `Gboard Settings -> Clipboard -> Clipboard History Manager`.
- 🔍 **Search & Quick Copy**: Instantly filter through copied text and tap any clip to copy it straight back into the active clipboard.
- 💾 **Unlimited History & SQLite Reader**: Accesses Gboard's internal SQLite database (`gboard_clipboard.db` -> `clips` table) directly.
- 🔄 **Cross-Keyboard Backfill**: Catches clips copied while other input methods or apps were active.
- 📁 **SAF File Export**: Export full clipboard history to a `.txt` file using Android Storage Access Framework.
- 🔒 **Privacy & Sensitive Clips**: Toggle visibility for passwords and sensitive input fields.
- ⚡ **Companion App & Widget API**: Exported read-only `ContentProvider` allowing third-party tools, Tasker, KWGT, or desktop clipboard sync services to read history securely.

---

## 📁 Repository Structure

```
gboard-clipboard-history/
├── extension/                     # Android Runtime Extension (.rve DEX)
│   └── src/main/java/com/sj010/gboardclipboard/extension/
│       ├── GboardClipboardHistoryRepository.java   # SQLite database reader/writer
│       ├── GboardClipboardHistorySettings.java     # Token & preference persistence
│       ├── GboardClipboardHistoryProvider.java     # content:// ContentProvider API
│       ├── GboardClipboardBackfill.java            # Primary clip backfill observer
│       └── GboardClipboardHistoryActivity.java     # Clean Material UI Activity
│   └── src/main/res/                               # Vector icons & strings
├── patches/                       # Morphe Bytecode & Resource Patch Bundle
│   └── src/main/kotlin/com/sj010/gboardclipboard/patches/
│       ├── Constants.kt                            # Package names, targets & URIs
│       ├── XmlManifestUtils.kt                     # DOM manipulation helpers
│       ├── ClipboardCarrierPatch.kt                # Injects runtime extension DEX
│       └── ClipboardHistoryPatch.kt                # Manifest & settings XML patch
├── .github/workflows/build.yml    # Automated CI/CD release pipeline
├── build.sh                       # Local build & patch runner
├── patches-bundle.json            # Morphe Manager repository metadata
└── settings.gradle.kts            # Gradle settings & Morphe plugin setup
```

---

## 🚀 How to Build & Patch

### Method 1: Using Morphe Manager (Recommended)
1. Install [Morphe Manager](https://github.com/MorpheApp/morphe-manager) on your Android device.
2. In Morphe Manager sources, add this repository URL or download the compiled `.mpp` patch file from GitHub Releases.
3. Select your installed or downloaded stock Gboard APK.
4. Apply the **Gboard Clipboard History** patch and install.

---

### Method 2: Command Line (Linux / macOS)

1. **Prerequisites**: Java 21+ and a stock Gboard APK.
2. Place your stock Gboard APK in `input/gboard.apk`.
3. Run the automated patch script:
   ```bash
   ./build.sh
   ```
4. Output will be generated at `output/GboardClipboard.apk`.

To recompile the patch bundle from source:
```bash
./build.sh --rebuild
```

---

## 🔌 Companion App Integration API

External applications (widgets, automation tools, desktop sync) can query clipboard history via Android's `ContentProvider` mechanism:

### Provider Details
- **Authority**: `content://com.sj010.gboardclipboard.history`
- **Endpoints**:
  - `content://com.sj010.gboardclipboard.history/clips` (all history)
  - `content://com.sj010.gboardclipboard.history/clips/{id}` (single clip)
  - `content://com.sj010.gboardclipboard.history/primary` (system primary clip)

### Authentication
External access is disabled by default. Enable it inside the **Clipboard History** settings screen. Once enabled, authenticate either via:
1. **Signature Permission**: Declare `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY` if signed with the same key.
2. **Access Token**: Pass the token as a query parameter:
   ```
   content://com.sj010.gboardclipboard.history/clips?token=YOUR_API_TOKEN
   ```

### Kotlin Example
```kotlin
val uri = Uri.parse("content://com.sj010.gboardclipboard.history/clips?token=$userToken")
val cursor = context.contentResolver.query(uri, null, null, null, null)

cursor?.use {
    val textIdx = it.getColumnIndex("text")
    val timeIdx = it.getColumnIndex("timestamp")
    while (it.moveToNext()) {
        val text = it.getString(textIdx)
        val timestamp = it.getLong(timeIdx)
        println("Clip: $text (Copied at $timestamp)")
    }
}
```

---

## 📄 License
This project is licensed under the [GNU General Public License v3.0 (GPLv3)](LICENSE).
