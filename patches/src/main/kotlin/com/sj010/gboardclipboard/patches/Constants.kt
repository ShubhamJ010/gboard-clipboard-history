package com.sj010.gboardclipboard.patches

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    const val GBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin"
    const val PIXELBOARD_PACKAGE_NAME = "com.akshaykadam.pixelboard"
    const val SJ010_GBOARD_PACKAGE_NAME = "com.sj010.gboard"
    const val SJ010_PIXELBOARD_PACKAGE_NAME = "com.sj010.pixelboard"
    const val PATCH_AUTHOR = "sj010"
    const val PATCH_AUTHOR_URL = "https://github.com/sj010"
    const val PATCH_REPOSITORY_URL = "https://github.com/sj010/gboard-clipboard-history"
    const val PATCH_VERSION = "1.0.0"

    const val CLIPBOARD_PERMISSION_NAME =
        "com.sj010.gboardclipboard.permission.READ_CLIPBOARD_HISTORY"
    const val CLIPBOARD_PROVIDER_CLASS =
        "com.sj010.gboardclipboard.extension.GboardClipboardHistoryProvider"
    const val CLIPBOARD_PROVIDER_AUTHORITY =
        "com.sj010.gboardclipboard.history"
    const val CLIPBOARD_ACTIVITY_CLASS =
        "com.sj010.gboardclipboard.extension.GboardClipboardHistoryActivity"

    val TARGET_VERSIONS = listOf(
        AppTarget(version = "18.4.1.985164140-release-arm64-v8a", isExperimental = false),
        AppTarget(version = "18.3.1.977415014-release-arm64-v8a", isExperimental = false),
        AppTarget(version = "18.3.1.977415014-beta-arm64-v8a", isExperimental = false),
        AppTarget(version = "18.0.3.954559732-release-arm64-v8a", isExperimental = false),
        AppTarget(version = "18.4.1", isExperimental = false),
        AppTarget(version = "18.3.1", isExperimental = false),
        AppTarget(version = "18.2.0", isExperimental = false),
        AppTarget(version = "18.1.0", isExperimental = false),
        AppTarget(version = "18.0.3", isExperimental = false),
        AppTarget(version = "18.0.0", isExperimental = false),
    )

    val COMPATIBILITY_GBOARD = Compatibility(
        name = "Gboard",
        packageName = GBOARD_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2979FF,
        signatures = setOf(
            "7ce83c1b71f3d572fed04c8d40c5cb10ff75e6d87d9df6fbd53f0468c2905053",
            "f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83",
        ),
        targets = TARGET_VERSIONS,
    )

    val COMPATIBILITY_PIXELBOARD = Compatibility(
        name = "PixelBoard",
        packageName = PIXELBOARD_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2979FF,
        targets = TARGET_VERSIONS,
    )
}
