package com.sj010.gboardclipboard.patches

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_ACTIVITY_CLASS
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PERMISSION_NAME
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PROVIDER_AUTHORITY
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PROVIDER_CLASS
import com.sj010.gboardclipboard.patches.Constants.COMPATIBILITY_GBOARD

internal val gboardClipboardHistoryPatch = resourcePatch(
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
        provider.setManifestAndroidAttribute("authorities", CLIPBOARD_PROVIDER_AUTHORITY)
        provider.setManifestAndroidAttribute("exported", "true")
        provider.setManifestAndroidAttribute("grantUriPermissions", "false")

        // 3. Register Standalone UI Activity with Launcher Intent-Filter
        val activity = ensureManifestComponent(
            manifestDoc,
            application,
            "activity",
            CLIPBOARD_ACTIVITY_CLASS
        )
        activity.setManifestAndroidAttribute("label", "Clipboard History")
        activity.setManifestAndroidAttribute("exported", "true")
        activity.setManifestAndroidAttribute("theme", "@android:style/Theme.DeviceDefault.NoActionBar")

        ensureManifestIntentFilter(
            manifestDoc,
            activity,
            "android.intent.action.MAIN",
            "android.intent.category.LAUNCHER"
        )
    }
}

context(context: ResourcePatchContext)
private fun applyClipboardSettingsXmlModifications() = with(context) {
    // Attempt to inject a preference in Gboard's native Clipboard settings screen
    val xmlPath = "res/xml/setting_clipboard.xml"
    runCatching {
        document(xmlPath).use { settingsDoc ->
            val root = settingsDoc.documentElement
            val packageName = document("AndroidManifest.xml").use { it.documentElement.getAttribute("package") }

            val existing = root.childElements().firstOrNull {
                it.manifestAndroidAttribute("key") == "pref_gboard_clipboard_history_manager"
            }
            if (existing != null) return@use

            val pref = settingsDoc.createElement("Preference").also { root.appendChild(it) }
            pref.setManifestAndroidAttribute("key", "pref_gboard_clipboard_history_manager")
            pref.setManifestAndroidAttribute("title", "Clipboard History Manager")
            pref.setManifestAndroidAttribute("summary", "View captured clips, export history, and configure companion API")
            pref.setManifestAndroidAttribute("persistent", "false")
            pref.setManifestAndroidAttribute("selectable", "true")

            val intent = settingsDoc.createElement("intent").also { pref.appendChild(it) }
            intent.setManifestAndroidAttribute("action", "android.intent.action.MAIN")
            if (packageName.isNotBlank()) {
                intent.setManifestAndroidAttribute("targetPackage", packageName)
            }
            intent.setManifestAndroidAttribute("targetClass", CLIPBOARD_ACTIVITY_CLASS)
        }
    }
}
