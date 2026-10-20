package com.sj010.gboardclipboard.patches

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_ACTIVITY_CLASS
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PERMISSION_NAME
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PROVIDER_AUTHORITY
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PROVIDER_CLASS
import com.sj010.gboardclipboard.patches.Constants.COMPATIBILITY_GBOARD

val gboardClipboardHistoryPatch = resourcePatch(
    name = "Clipboard History",
    description = "Add standalone Gboard Clipboard History with exported ContentProvider, native settings entry, and app drawer launcher icon.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    dependsOn(gboardClipboardCarrierPatch)

    finalize {
        applyManifestModifications()
        applyClipboardSettingsXmlModifications()
    }
}

context(context: ResourcePatchContext)
private fun applyManifestModifications() = with(context) {
    document("AndroidManifest.xml").use { manifestDoc ->
        val manifest = manifestDoc.documentElement
        val application = manifest.childElements("application").firstOrNull() ?: return@use
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
        provider.setAttribute("android:authorities", CLIPBOARD_PROVIDER_AUTHORITY)
        provider.setAttribute("android:exported", "true")
        provider.setAttribute("android:multiprocess", "false")
        provider.setAttribute("android:readPermission", CLIPBOARD_PERMISSION_NAME)

        // 3. Register Standalone Clipboard History Activity with Launcher Intent-Filter
        val activity = ensureManifestComponent(
            manifestDoc,
            application,
            "activity",
            CLIPBOARD_ACTIVITY_CLASS
        )
        activity.setAttribute("android:exported", "true")
        activity.setAttribute("android:label", "Gboard Clipboard History")
        activity.setAttribute("android:theme", "@style/Theme.AppCompat.DayNight.NoActionBar")
        activity.setAttribute(
            "android:launchMode",
            if (packageName == "com.google.android.inputmethod.latin") "singleTop" else "singleInstance"
        )

        val intentFilter = ensureElement(manifestDoc, activity, "intent-filter")
        ensureAction(manifestDoc, intentFilter, "android.intent.action.MAIN")
        ensureCategory(manifestDoc, intentFilter, "android.intent.category.LAUNCHER")
    }
}

context(context: ResourcePatchContext)
private fun applyClipboardSettingsXmlModifications() = with(context) {
    document("res/xml/setting_clipboard.xml").use { settingsDoc ->
        val root = settingsDoc.documentElement
        val preferenceCategory = root.childElements("PreferenceCategory").firstOrNull() ?: root

        val preference = ensureElement(settingsDoc, preferenceCategory, "Preference")
        preference.setAttribute("android:key", "pref_key_clipboard_history_manager")
        preference.setAttribute("android:title", "Clipboard History Manager")
        preference.setAttribute("android:summary", "Search, export, and manage persistent Gboard clips")

        val intent = ensureElement(settingsDoc, preference, "intent")
        intent.setAttribute("android:targetClass", CLIPBOARD_ACTIVITY_CLASS)
        intent.setAttribute("android:targetPackage", "@package")
    }
}
