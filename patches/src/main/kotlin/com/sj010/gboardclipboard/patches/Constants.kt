package com.sj010.gboardclipboard.patches

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

internal object Constants {
    const val GBOARD_PACKAGE_NAME = "com.google.android.inputmethod.latin"
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

    val COMPATIBILITY_GBOARD = Compatibility(
        name = "Gboard",
        packageName = GBOARD_PACKAGE_NAME,
        apkFileType = "APK",
        appIconColor = 0x2979FF,
        targets = listOf(
            AppTarget(version = "18.4.1", isExperimental = false),
            AppTarget(version = "18.3.1", isExperimental = false),
            AppTarget(version = "18.2.0", isExperimental = false),
            AppTarget(version = "18.1.0", isExperimental = false),
            AppTarget(version = "18.0.0", isExperimental = false),
        ),
    )
}
