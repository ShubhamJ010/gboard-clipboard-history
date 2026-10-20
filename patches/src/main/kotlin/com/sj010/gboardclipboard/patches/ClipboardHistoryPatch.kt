package com.sj010.gboardclipboard.patches

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_ACTIVITY_CLASS
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PERMISSION_NAME
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PROVIDER_AUTHORITY
import com.sj010.gboardclipboard.patches.Constants.CLIPBOARD_PROVIDER_CLASS
import com.sj010.gboardclipboard.patches.Constants.COMPATIBILITY_GBOARD
import com.sj010.gboardclipboard.patches.Constants.COMPATIBILITY_PIXELBOARD
import com.sj010.gboardclipboard.patches.Constants.COMPATIBILITY_SJ010_GBOARD
import com.sj010.gboardclipboard.patches.Constants.COMPATIBILITY_SJ010_PIXELBOARD
import org.w3c.dom.Document
import org.w3c.dom.Element

private const val HEADER_PREFERENCE_TAG =
    "com.google.android.libraries.inputmethod.settings.widget.HeaderPreference"
private const val CLIPBOARD_PREF_KEY = "pref_key_clipboard_history_manager"
private val GBOARD_SETTINGS_PATHS = listOf(
    "res/xml/settings.xml",
    "res/xml/settings_legacy.xml",
)

val gboardClipboardHistoryPatch = resourcePatch(
    name = "Clipboard History",
    description = "Integrate persistent Clipboard History manager directly inside Keyboard Settings with ContentProvider and search/export.",
) {
    compatibleWith(COMPATIBILITY_GBOARD)
    compatibleWith(COMPATIBILITY_PIXELBOARD)
    compatibleWith(COMPATIBILITY_SJ010_GBOARD)
    compatibleWith(COMPATIBILITY_SJ010_PIXELBOARD)
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

        // 1. Signature permission for ContentProvider security
        ensureManifestPermission(
            manifestDoc,
            manifest,
            CLIPBOARD_PERMISSION_NAME,
            "signature"
        )

        // 2. Read-Only ContentProvider
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

        // 3. Clipboard History Activity - INTERNAL TO SETTINGS (exported=false, NO LAUNCHER ICON)
        val activity = ensureManifestComponent(
            manifestDoc,
            application,
            "activity",
            CLIPBOARD_ACTIVITY_CLASS
        )
        activity.setManifestAndroidAttribute("exported", "false")
        activity.setManifestAndroidAttribute("label", "Clipboard History")

        val settingsActivity = application.childElements("activity").firstOrNull {
            it.manifestAndroidAttribute("name") == "com.google.android.apps.inputmethod.latin.preference.SettingsActivity"
        }
        val settingsTheme = settingsActivity?.manifestAndroidAttribute("theme")
        activity.setManifestAndroidAttribute(
            "theme",
            settingsTheme ?: "@style/Theme.AppCompat.DayNight.NoActionBar"
        )

        // Strip launcher category if previously present
        val launcherFilters = activity.childElements("intent-filter").filter { filter ->
            filter.childElements("category").any {
                it.manifestAndroidAttribute("name") == "android.intent.category.LAUNCHER"
            }
        }.toList()
        launcherFilters.forEach { activity.removeChild(it) }

        Unit
    }
}

context(context: ResourcePatchContext)
private fun applyClipboardSettingsXmlModifications(): Unit = with(context) {
    // 1. Create vector icon
    try {
        val iconFile = get("res/drawable/ic_clipboard_history_entry.xml", false)
        iconFile.parentFile?.mkdirs()
        iconFile.writeText(
            """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?android:attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,2h-4.18C14.4,0.84 13.3,0 12,0c-1.3,0 -2.4,0.84 -2.82,2H5c-1.1,0 -2,0.9 -2,2v16c0,1.1 0.9,2 2,2h14c1.1,0 2,-0.9 2,-2V4C21,2.9 20.1,2 19,2zM12,2c0.55,0 1,0.45 1,1s-0.45,1 -1,1 -1,-0.45 -1,-1 0.45,-1 1,-1zM14,17H7v-2h7V17zM17,13H7v-2h10V13zM17,9H7V7h10V9z"/>
</vector>
"""
        )
    } catch (_: Throwable) {
    }

    // 2. Inject into main Settings screens (settings.xml, settings_legacy.xml)
    GBOARD_SETTINGS_PATHS.forEach { path ->
        try {
            document(path).use { doc ->
                injectIntoSettingsRoot(doc)
                Unit
            }
        } catch (_: Throwable) {
        }
    }

    // 3. Also inject into Gboard -> Settings -> Clipboard (setting_clipboard.xml)
    try {
        document("res/xml/setting_clipboard.xml").use { doc ->
            val root = doc.documentElement
            val targetCategory = root.childElements("androidx.preference.PreferenceCategory").lastOrNull()
                ?: root.childElements("PreferenceCategory").lastOrNull()
                ?: root

            val existing = targetCategory.childElements().firstOrNull {
                it.manifestAndroidAttribute("key") == CLIPBOARD_PREF_KEY
            }
            val pref = existing ?: doc.createElement("androidx.preference.Preference").also(targetCategory::appendChild)
            pref.setManifestAndroidAttribute("key", CLIPBOARD_PREF_KEY)
            pref.setManifestAndroidAttribute("title", "Clipboard History Manager")
            pref.setManifestAndroidAttribute("summary", "Search, export, and manage persistent clips")
            pref.setManifestAndroidAttribute("icon", "@drawable/ic_clipboard_history_entry")

            val intent = pref.childElements("intent").firstOrNull()
                ?: doc.createElement("intent").also(pref::appendChild)
            intent.setManifestAndroidAttribute("action", "android.intent.action.MAIN")
            intent.setManifestAndroidAttribute("targetClass", CLIPBOARD_ACTIVITY_CLASS)
            intent.setManifestAndroidAttribute("targetPackage", "@package")
            Unit
        }
    } catch (_: Throwable) {
    }
}

private fun injectIntoSettingsRoot(doc: Document) {
    val root = doc.documentElement
    val candidateEntries = root.childElements().flatMap { parent ->
        if (parent.tagName == "androidx.preference.PreferenceCategory") {
            parent.childElements()
        } else {
            sequenceOf(parent)
        }
    }.toList()

    val existingEntry = candidateEntries.firstOrNull {
        it.manifestAndroidAttribute("key") == CLIPBOARD_PREF_KEY
    }

    val footerPreference = root.childElements().firstOrNull {
        it.tagName == "com.android.settingslib.widget.FooterPreference"
    }
    val targetCategory = root.childElements("androidx.preference.PreferenceCategory").lastOrNull()

    val prefEntry = existingEntry ?: doc.createElement(HEADER_PREFERENCE_TAG).also { created ->
        if (targetCategory != null) {
            targetCategory.appendChild(created)
        } else if (footerPreference != null) {
            root.insertBefore(created, footerPreference)
        } else {
            root.appendChild(created)
        }
    }

    prefEntry.setManifestAndroidAttribute("persistent", "false")
    prefEntry.setManifestAndroidAttribute("title", "Clipboard History")
    prefEntry.setManifestAndroidAttribute("selectable", "true")
    prefEntry.setManifestAndroidAttribute("key", CLIPBOARD_PREF_KEY)
    prefEntry.setManifestAndroidAttribute("summary", "Search, export, and manage persistent clips")
    prefEntry.setManifestAndroidAttribute("icon", "@drawable/ic_clipboard_history_entry")

    val intent = prefEntry.childElements("intent").firstOrNull()
        ?: doc.createElement("intent").also(prefEntry::appendChild)
    intent.setManifestAndroidAttribute("action", "android.intent.action.MAIN")
    intent.setManifestAndroidAttribute("targetPackage", "@package")
    intent.setManifestAndroidAttribute("targetClass", CLIPBOARD_ACTIVITY_CLASS)
}
