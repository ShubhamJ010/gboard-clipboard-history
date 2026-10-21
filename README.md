# Gboard Clipboard History

A [Morphe](https://github.com/MorpheApp) patch that adds persistent clipboard history management to Google Gboard.

It runs as a standalone patch and does not require other keyboard modifications.

<p align="center">
  <a href="https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History">
    <img src="https://img.shields.io/badge/Add_to-Morphe-1E88E5?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Add to Morphe"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/ShubhamJ010/gboard-clipboard-history/releases/tag/0.0.3-alpha">
    <img src="https://img.shields.io/badge/📦_Morphe_Patch_(.mpp)-v0.0.3--alpha-2979FF?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Download Morphe Patch (.mpp)"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/ShubhamJ010/gboard-clipboard-history/releases/tag/0.0.3-alpha">
    <img src="https://img.shields.io/badge/📥_Release-0.0.3--alpha-00C853?style=for-the-badge&logo=github&logoColor=white" height="40" alt="Release 0.0.3-alpha"/>
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

- **In-app settings integration**: Placed cleanly inside `Gboard Settings -> Clipboard`. There is **no separate launcher icon** and no root settings clutter – the manager is only reachable from inside the keyboard clipboard settings.
- **PixelBoard Material 3 UI**: Clean Material 3 design with Monet dynamic theming (Android 12+), vector back navigation, relative timestamps, search, and image thumbnail previews.
- **Foreground clipboard capture**: Hooks Gboard's active input view (`onStartInputView`) to capture primary clips and backfill missing entries.
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
│           ├── GboardClipboardSyncRuntime.java         # Foreground input view sync
│           └── GboardClipboardHistoryActivity.java     # PixelBoard Material 3 UI
│       └── src/main/res/                               # Vector assets and strings
├── patches/                       # Morphe bytecode and resource patch bundle
│   └── src/main/kotlin/com/sj010/gboardclipboard/patches/
│       ├── Constants.kt                            # Package names, targets, and URIs
│       ├── XmlManifestUtils.kt                     # XML manipulation helpers
│       ├── ClipboardCarrierPatch.kt                # Extension DEX injection
│       ├── ClipboardHistoryPatch.kt                # Manifest, provider and settings injection
│       ├── GboardClipboardSyncPatch.kt             # onStartInputView foreground hook
│       └── GboardPackageRenamePatch.kt             # Package rename to com.sj010.gboard
│   └── src/main/resources/                         # patches-list.json / patches-bundle.json
├── scripts/                       # All build / install scripts
│   ├── lib/common.sh                              # Shared tool, input and patcher helpers
│   ├── build-mpp.sh                               # Build patches/GboardClipboardHistory.mpp
│   ├── build-gradle.sh                            # Gradle build of the bundle (needs GITHUB_TOKEN)
│   ├── build-gboard-clipboard.sh                  # App 1: Gboard + Clipboard History
│   ├── build-gboard-clipboard-pixelboard.sh       # App 2: Gboard + PixelBoard + Clipboard History
│   ├── build-pixelboard-clipboard.sh              # App 3: released PixelBoard + Clipboard History
│   ├── build-all.sh                               # Build the bundle and all three APKs
│   └── install.sh                                 # Install a built APK on a device
├── input/                         # Build inputs (git ignored, auto downloaded)
│   ├── gboard-merged.apk                          # Gboard v18.4.1 base
│   ├── patches-1.2.0.mpp                          # PixelBoard patch bundle
│   └── PixelBoard-v18.4.1-Stable-V7.apk           # Latest released PixelBoard APK
├── .github/workflows/build.yml    # CI build workflow
├── patches-bundle.json            # Morphe Manager repository metadata
└── settings.gradle.kts            # Gradle settings and plugin configuration
```

## Building

All build logic lives in `scripts/`. Every script is self contained and can be run from the
repository root.

### Patch bundle

```bash
./scripts/build-mpp.sh                                  # credential free: kotlinc + d8 + Morphe patcher
./scripts/build-mpp.sh --rebuild-extension              # also rebuild the runtime extension DEX
./scripts/build-gradle.sh                               # Gradle build (needs GITHUB_TOKEN)
```

Output: `patches/GboardClipboardHistory.mpp`.

### The three patched APKs

```bash
./scripts/build-all.sh                                  # bundle + all three APKs
```

| Script | Input | Output | Package |
| :--- | :--- | :--- | :--- |
| `build-gboard-clipboard.sh` | Gboard base | `output/Gboard-com.sj010.gboard.apk` | `com.sj010.gboard` |
| `build-gboard-clipboard-pixelboard.sh` | Gboard base + PixelBoard `.mpp` | `output/Gboard-PixelBoard-akshaykadam.apk` | `com.akshaykadam.pixelboard` |
| `build-pixelboard-clipboard.sh` | released PixelBoard APK | `output/PixelBoard-clipboard-sj010.apk` | `com.akshaykadam.pixelboard` |

The first two scripts run the patcher twice on purpose: the package rename has to be finished
before the clipboard history patch runs, because the provider authority and the signature
permission are derived from the final host package name.

### Build inputs

Inputs live in `input/` (git ignored, they are too large for GitHub). The scripts download what
is missing:

| Input | Source |
| :--- | :--- |
| `input/gboard-merged.apk` | Gboard v18.4.1 base. Provide it manually, or drop the raw splits into `input/splits/` and they get merged with APKEditor. Override with `GBOARD_APK=<path>`. |
| `input/patches-1.2.0.mpp` | PixelBoard patch bundle, downloaded from the [PixelBoard](https://github.com/Akshayykadam/PixelBoard) repository. |
| `input/PixelBoard-v18.4.1-Stable-V7.apk` | Latest released PixelBoard APK, downloaded from the PixelBoard releases. |

Tooling is resolved automatically: Java 21+, `kotlinc`, `d8`, `android.jar`, `APKEditor.jar` and
the Morphe patcher jar (downloaded from the PixelBoard repository when the sibling checkout is
missing).

### Signing

`SIGNING_KEY` selects the keystore:

| Value | Keystore | Certificate |
| :--- | :--- | :--- |
| `pixelboard` (default) | sibling `PixelBoard/tools/patcher-data/pixelboard.keystore` | `CN=PixelBoard` – matches builds installed earlier |
| `clipboard` | `tools/patcher-data/clipboard.keystore` | `CN=sj010` – generated on first use |

### Install

```bash
./scripts/install.sh output/Gboard-com.sj010.gboard.apk
./scripts/install.sh output/Gboard-PixelBoard-akshaykadam.apk --uninstall
```

`--uninstall` removes the previous package first, which is required when the signing
certificate differs (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).

## Downloads

Every release provides the patch bundle and the three patched APKs:

| Asset | Format | Description |
| :--- | :--- | :--- |
| **patches-0.0.3-alpha.mpp** | Morphe Patch | Standalone patch bundle for use with Morphe Manager or CLI patcher. Targets Gboard and PixelBoard. |
| **Gboard-com.sj010.gboard.apk** | Signed APK | Gboard + Clipboard History, package `com.sj010.gboard`. |
| **Gboard-PixelBoard-akshaykadam.apk** | Signed APK | Gboard + PixelBoard + Clipboard History, package `com.akshaykadam.pixelboard`. |
| **PixelBoard-clipboard-sj010.apk** | Signed APK | Released PixelBoard APK + Clipboard History, package `com.akshaykadam.pixelboard`. |

Download the files from the [Releases](https://github.com/ShubhamJ010/gboard-clipboard-history/releases/tag/0.0.3-alpha) page.

> The two `com.akshaykadam.pixelboard` APKs share the same package name, so only one of them can be
> installed at a time.

## ContentProvider API

Other apps or automation scripts (Tasker, Termux, Automate) can read clipboard history via:

- **URI**: `content://com.sj010.gboardclipboard.history/clips`
- **Permission**: `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY` (signature protection level)
- **Token fallback**: `content://com.sj010.gboardclipboard.history/clips?token=<access token>` – the token is shown in *Clipboard History -> Settings / API* and can be regenerated at any time
- **Columns**: `_id`, `text`, `html_text`, `item_type`, `entity_type`, `timestamp`, `sensitive`, `uri`, `group_id`
- **Extra path**: `content://com.sj010.gboardclipboard.history/primary` returns the current primary clipboard entry

External access is disabled by default and has to be enabled in *Clipboard History -> Settings / API*.

### Authority per host package

The provider authority and the signature permission live in global namespaces, so two patched
keyboards installed side by side would clash on them. Every host package therefore gets its own
scoped variant, while the reference build `com.sj010.gboard` keeps the documented names:

| Host package | Authority | Permission |
| :--- | :--- | :--- |
| `com.sj010.gboard` | `com.sj010.gboardclipboard.history` | `com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY` |
| `com.akshaykadam.pixelboard` | `com.akshaykadam.pixelboard.gboardclipboard.history` | `com.akshaykadam.pixelboard.permission.READ_CLIPBOARD_HISTORY` |

The runtime reads the authority and the permission back from the package manager, so the URI shown
in *Clipboard History -> Settings / API* always matches the manifest of the installed build.

## License

GPL-3.0
