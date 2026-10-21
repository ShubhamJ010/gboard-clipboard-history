# Gboard Clipboard History

A [Morphe](https://github.com/MorpheApp) patch that adds persistent clipboard history management to Google Gboard.

It runs as a standalone patch and does not require other keyboard modifications. It is also compatible with other popular keyboard mods like [PixelBoard](https://github.com/Akshayykadam/PixelBoard), proving that the patch can compose cleanly with external modifications.

<p align="center">
  <a href="https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History">
    <img src="https://img.shields.io/badge/Add_to-Morphe-1E88E5?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Add to Morphe"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium%3A%2F%2Fapp%2F%7B%22id%22%3A%22com.sj010.gboard%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FShubhamJ010%2Fgboard-clipboard-history%22%2C%22author%22%3A%22ShubhamJ010%22%2C%22name%22%3A%22Gboard%20Clipboard%20History%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22Gboard-Clipboard-History.%2A%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22includeZips%5C%22%3Afalse%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D">
    <img src="https://img.shields.io/badge/Obtainium-Gboard_APK-blue?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Add Gboard to Obtainium"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium%3A%2F%2Fapp%2F%7B%22id%22%3A%22com.akshaykadam.pixelboard%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FShubhamJ010%2Fgboard-clipboard-history%22%2C%22author%22%3A%22ShubhamJ010%22%2C%22name%22%3A%22PixelBoard%20Clipboard%20History%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22PixelBoard-Clipboard-History.%2A%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22includeZips%5C%22%3Afalse%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D">
    <img src="https://img.shields.io/badge/Obtainium-PixelBoard_APK-blue?style=for-the-badge&logo=android&logoColor=white" height="40" alt="Add PixelBoard to Obtainium"/>
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/ShubhamJ010/gboard-clipboard-history/releases/tag/0.0.3-alpha">
    <img src="https://img.shields.io/badge/📥_Release-0.0.3--alpha-00C853?style=for-the-badge&logo=github&logoColor=white" height="40" alt="Release 0.0.3-alpha"/>
  </a>
</p>

## Installation & Updates

### 1. Pre-built APKs via Obtainium (Recommended)

You can track and install automatic updates for pre-built APKs directly from GitHub releases via [Obtainium](https://github.com/ImranR98/Obtainium):

| Keyboard Variant | Package | One-Click Add to Obtainium |
| :--- | :--- | :--- |
| **Gboard Clipboard History** (Standalone) | `com.sj010.gboard` | [![Obtainium](https://img.shields.io/badge/Obtainium-Add%20to%20Obtainium-blue?style=for-the-badge&logo=android&color=blue)](https://apps.obtainium.imranr.dev/redirect?r=obtainium%3A%2F%2Fapp%2F%7B%22id%22%3A%22com.sj010.gboard%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FShubhamJ010%2Fgboard-clipboard-history%22%2C%22author%22%3A%22ShubhamJ010%22%2C%22name%22%3A%22Gboard%20Clipboard%20History%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22Gboard-Clipboard-History.%2A%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22includeZips%5C%22%3Afalse%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D) |
| **PixelBoard Clipboard History** (M3 Themed) | `com.akshaykadam.pixelboard` | [![Obtainium](https://img.shields.io/badge/Obtainium-Add%20to%20Obtainium-blue?style=for-the-badge&logo=android&color=blue)](https://apps.obtainium.imranr.dev/redirect?r=obtainium%3A%2F%2Fapp%2F%7B%22id%22%3A%22com.akshaykadam.pixelboard%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FShubhamJ010%2Fgboard-clipboard-history%22%2C%22author%22%3A%22ShubhamJ010%22%2C%22name%22%3A%22PixelBoard%20Clipboard%20History%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22PixelBoard-Clipboard-History.%2A%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22includeZips%5C%22%3Afalse%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D) |

---

### 2. Patch Bundle via Morphe Manager

#### One-Click Import (Android)

Tap the button below or link on an Android device where Morphe Manager is installed:

[![Add to Morphe](https://img.shields.io/badge/Add%20to-Morphe-1E88E5?style=for-the-badge&logo=android&logoColor=white)](https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History)

**Direct link:** [https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History](https://morphe.software/add-source?github=ShubhamJ010/gboard-clipboard-history&name=Gboard%20Clipboard%20History)

> [!NOTE]
> If viewing on desktop, clicking the link displays a QR code to scan with your Android phone's camera.

#### Manual Import

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
│   ├── build-gboard-clipboard.sh                  # Build Gboard + Clipboard History APK
│   ├── build-gboard-clipboard-pixelboard.sh       # Build PixelBoard + Clipboard History APK
│   ├── build-pixelboard-clipboard.sh              # PoC: Direct patch of released PixelBoard APK
│   ├── build-all.sh                               # Build the bundle and APKs
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

### Patched APKs

```bash
./scripts/build-all.sh                                  # bundle + all APK targets
```

| Script | Input | Output | Package | Description |
| :--- | :--- | :--- | :--- | :--- |
| `build-gboard-clipboard.sh` | Gboard base | `output/Gboard-Clipboard-History.apk` | `com.sj010.gboard` | Gboard + Clipboard History (coexists with stock Gboard) |
| `build-gboard-clipboard-pixelboard.sh` | Gboard base + PixelBoard `.mpp` | `output/PixelBoard-Clipboard-History.apk` | `com.akshaykadam.pixelboard` | Gboard + PixelBoard theming + Clipboard History |
| `build-pixelboard-clipboard.sh` | released PixelBoard APK | `output/PixelBoard-clipboard-poc.apk` | `com.akshaykadam.pixelboard` | **PoC**: demonstrates directly patching a pre-built PixelBoard release APK |

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
| `input/PixelBoard-v18.4.1-Stable-V7.apk` | Latest released PixelBoard APK, downloaded from the PixelBoard releases (used for the PoC script). |

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
./scripts/install.sh output/Gboard-Clipboard-History.apk
./scripts/install.sh output/PixelBoard-Clipboard-History.apk --uninstall
```

`--uninstall` removes the previous package first, which is required when the signing
certificate differs (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).

## Downloads

Every release provides the patch bundle and pre-built APKs:

| Asset | Format | Obtainium | Description |
| :--- | :--- | :--- | :--- |
| **patches-0.0.3-alpha.mpp** | Morphe Patch | — | Standalone patch bundle for use with Morphe Manager or CLI patcher. Targets Gboard and PixelBoard. |
| **Gboard-Clipboard-History.apk** | Signed APK | [Add to Obtainium](https://apps.obtainium.imranr.dev/redirect?r=obtainium%3A%2F%2Fapp%2F%7B%22id%22%3A%22com.sj010.gboard%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FShubhamJ010%2Fgboard-clipboard-history%22%2C%22author%22%3A%22ShubhamJ010%22%2C%22name%22%3A%22Gboard%20Clipboard%20History%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22Gboard-Clipboard-History.%2A%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22includeZips%5C%22%3Afalse%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D) | Standalone Gboard + Clipboard History (package `com.sj010.gboard`, installs side-by-side with stock Gboard). |
| **PixelBoard-Clipboard-History.apk** | Signed APK | [Add to Obtainium](https://apps.obtainium.imranr.dev/redirect?r=obtainium%3A%2F%2Fapp%2F%7B%22id%22%3A%22com.akshaykadam.pixelboard%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2FShubhamJ010%2Fgboard-clipboard-history%22%2C%22author%22%3A%22ShubhamJ010%22%2C%22name%22%3A%22PixelBoard%20Clipboard%20History%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22PixelBoard-Clipboard-History.%2A%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22includeZips%5C%22%3Afalse%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D) | Gboard with PixelBoard theming + Clipboard History (package `com.akshaykadam.pixelboard`). |

Download the files from the [Releases](https://github.com/ShubhamJ010/gboard-clipboard-history/releases/tag/0.0.3-alpha) page.

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

## Acknowledgements

Special thanks and sincere credit to:
- **[Akshay Kadam](https://github.com/Akshayykadam)** and the **[PixelBoard](https://github.com/Akshayykadam/PixelBoard)** project:
  PixelBoard's groundbreaking work on Gboard reverse engineering, bytecode patching patterns, Material 3 design, and build tooling made this clipboard history patch possible.
- **[Morphe](https://github.com/MorpheApp)** for the extensible bytecode and resource patching framework.

## License

GPL-3.0
