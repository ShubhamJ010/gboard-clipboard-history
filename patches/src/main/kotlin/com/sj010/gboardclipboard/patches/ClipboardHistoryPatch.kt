package com.sj010.gboardclipboard.patches

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_ACTIVITY_CLASS
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PERMISSION_NAME
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PROVIDER_AUTHORITY
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PROVIDER_CLASS
import com.sj010.gboardclipboard.patches.Constants.COMPATIBILITY_GBOARD
import com.sj010.gboardclipboard.patches.Constants.COMPATIBILITY_PIXELBOARD

val gboardClipboardHistoryPatch = resourcePatch(
    name = "Clipboard History",
    description = "Add standalone Gboard Clipboard History with exported ContentProvider, native settings entry, and app drawer launcher icon.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    compatibleWith(COMPATIBILITY_PIXELBOARD)
    dependsOn(gboardClipboardCarrierPatch)

    finalize {
        applyManifestModifications()
        applyClipboardSettingsXmlModifications()
    }
}

context(context: ResourcePatchContext)
private fun applyManifestModifications(): Unit = with(context) {
    document("AndroidManifest.xml").use { manifestDoc ->
        val manifest = manifestDoc.documentElement
        val application = manifest.childElements("application").firstOrNull() ?: return@use Unit
        val packageName = manifest.getAttribute("package")

        // 1. Declare Signature Permission for ContentProvider security
        ensureManifestPermission(
            manifestDoc,
            manifest,
            CLIPBOARD_PERMISSION_NAME,
            "signature"
        )

        // 2. Register Read-Only ContentProvider
        val provider = ensureManifestComponent(
            manifestDoc,
            application,
            "provider",
            CLIPBOARD_PROVIDER_CLASS
        )
        provider.setManifestAndroidAttribute("authorities", CLIPBOARD_PROVIDER_AUTHORITY)
        provider.setManifestAndroidAttribute("exported", "true")
        provider.setManifestAndroidAttribute("multiprocess", "false")
        provider.setManifestAndroidAttribute("readPermission", CLIPBOARD_PERMISSION_NAME)

        // 3. Register Standalone Clipboard History Activity with Launcher Intent-Filter
        val activity = ensureManifestComponent(
            manifestDoc,
            application,
            "activity",
            CLIPBOARD_ACTIVITY_CLASS
        )
        activity.setManifestAndroidAttribute("exported", "true")
        activity.setManifestAndroidAttribute("label", "Gboard Clipboard History")
        activity.setManifestAndroidAttribute("theme", "@style/Theme.AppCompat.DayNight.NoActionBar")
        activity.setManifestAndroidAttribute(
            "launchMode",
            if (packageName == "com.google.android.inputmethod.latin") "singleTop" else "singleInstance"
        )

        ensureManifestIntentFilter(
            manifestDoc,
            activity,
            "android.intent.action.MAIN",
            "android.intent.category.LAUNCHER"
        )
        Unit
    }
}

context(context: ResourcePatchContext)
private fun applyClipboardSettingsXmlModifications(): Unit = with(context) {
    try {
        document("res/xml/setting_clipboard.xml").use { settingsDoc ->
            val root = settingsDoc.documentElement
            val preferenceCategory = root.childElements("PreferenceCategory").firstOrNull() ?: root

            val preference = settingsDoc.createElement("Preference").also(preferenceCategory::appendChild)
            preference.setManifestAndroidAttribute("key", "pref_key_clipboard_history_manager")
            preference.setManifestAndroidAttribute("title", "Clipboard History Manager")
            preference.setManifestAndroidAttribute("summary", "Search, export, and manage persistent Gboard clips")

            val intent = settingsDoc.createElement("intent").also(preference::appendChild)
            intent.setManifestAndroidAttribute("targetClass", CLIPBOARD_ACTIVITY_CLASS)
            intent.setManifestAndroidAttribute("targetPackage", "@package")
            Unit
        }
    } catch (_: Throwable) {
    }
}
