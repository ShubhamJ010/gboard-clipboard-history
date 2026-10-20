package com.sj010.gboardclipboard.patches

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
private const val CLIPBOARD_ACTIVITY_CLASS =
    "com.sj010.gboardclipboard.extension.GboardClipboardHistoryActivity"
private const val GBOARD_SETTINGS_ACTIVITY_CLASS =
    "com.google.android.apps.inputmethod.latin.preference.SettingsActivity"
private const val HEADER_PREFERENCE_TAG =
    "com.google.android.libraries.inputmethod.settings.widget.HeaderPreference"
private const val CLIPBOARD_PREF_KEY = "pref_key_clipboard_history_manager"

private val GBOARD_SETTINGS_PATHS = listOf(
    "res/xml/settings.xml",
    "res/xml/settings_legacy.xml",
)

val clipboardHistoryPatch = resourcePatch(
    name = "Gboard Clipboard History",
    description = "Provides persistent, unlimited clipboard history management integrated natively into Gboard settings.",
) {
    compatibleWith(
        COMPATIBILITY_GBOARD,
        COMPATIBILITY_PIXELBOARD,
        COMPATIBILITY_SJ010_GBOARD,
        COMPATIBILITY_SJ010_PIXELBOARD,
    )

    execute {
        applyClipboardHistoryManifestModifications()
        applyClipboardSettingsXmlModifications()
    }
}

context(context: ResourcePatchContext)
private fun applyClipboardHistoryManifestModifications(): Unit = with(context) {
    document("AndroidManifest.xml").use { doc ->
        val manifest = doc.documentElement
        val application = manifest.childElements("application").firstOrNull() ?: return@use

        // Find or create GboardClipboardHistoryActivity declaration
        val activity = application.childElements("activity").firstOrNull {
            it.manifestAndroidAttribute("name") == CLIPBOARD_ACTIVITY_CLASS
        } ?: doc.createElement("activity").also(application::appendChild)

        activity.setManifestAndroidAttribute("name", CLIPBOARD_ACTIVITY_CLASS)
        activity.setManifestAndroidAttribute("label", "Clipboard History")
        // Integrate into Gboard: Not exported outside app, no launcher entry
        activity.setManifestAndroidAttribute("exported", "false")

        // Inherit Gboard's native settings theme if available
        val settingsActivity = application.childElements("activity").firstOrNull {
            it.manifestAndroidAttribute("name") == GBOARD_SETTINGS_ACTIVITY_CLASS
        }
        val settingsTheme = settingsActivity?.manifestAndroidAttribute("theme")
        if (!settingsTheme.isNullOrBlank()) {
            activity.setManifestAndroidAttribute("theme", settingsTheme)
        }

        // Clean any residual launcher filters so it never shows as a separate home app icon
        val launcherFilters = activity.childElements("intent-filter").filter { filter ->
            val hasLauncher = filter.childElements("category").any {
                it.manifestAndroidAttribute("name") == "android.intent.category.LAUNCHER"
            }
            val hasMain = filter.childElements("action").any {
                it.manifestAndroidAttribute("name") == "android.intent.action.MAIN"
            }
            hasLauncher && hasMain
        }.toList()
        launcherFilters.forEach { activity.removeChild(it) }

        Unit
    }
}

context(context: ResourcePatchContext)
private fun applyClipboardSettingsXmlModifications(): Unit = with(context) {
    val packageName = try {
        document("AndroidManifest.xml").use { it.documentElement.getAttribute("package") }
    } catch (_: Throwable) {
        ""
    }

    // 0. Inject string resources in res/values*/strings.xml
    try {
        val resDir = get("res", false)
        if (resDir.exists() && resDir.isDirectory) {
            val stringFiles = resDir.walkTopDown()
                .filter { it.isFile && it.name == "strings.xml" }
                .toList()

            for (file in stringFiles) {
                var text = file.readText()
                if (!text.contains("name=\"gboard_clipboard_history_title\"")) {
                    val endIdx = text.lastIndexOf("</resources>")
                    if (endIdx >= 0) {
                        text = text.substring(0, endIdx) +
                            "    <string name=\"gboard_clipboard_history_title\">Clipboard History</string>\n" +
                            "    <string name=\"gboard_clipboard_history_summary\">Search, export, and manage persistent clips</string>\n" +
                            text.substring(endIdx)
                        file.writeText(text)
                    }
                }
            }
        }
    } catch (_: Throwable) {
    }

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
                injectIntoSettingsRoot(doc, packageName)
                Unit
            }
        } catch (_: Throwable) {
        }
    }

    // 3. Also inject into Gboard -> Settings -> Clipboard (setting_clipboard.xml)
    try {
        document("res/xml/setting_clipboard.xml").use { doc ->
            val root = doc.documentElement
            val existing = root.childElements().firstOrNull {
                it.manifestAndroidAttribute("key") == CLIPBOARD_PREF_KEY
            }
            val pref = existing ?: doc.createElement(HEADER_PREFERENCE_TAG).also(root::appendChild)
            pref.setManifestAndroidAttribute("persistent", "false")
            pref.setManifestAndroidAttribute("selectable", "true")
            pref.setManifestAndroidAttribute("key", CLIPBOARD_PREF_KEY)
            pref.setManifestAndroidAttribute("title", "@string/gboard_clipboard_history_title")
            pref.setManifestAndroidAttribute("summary", "@string/gboard_clipboard_history_summary")
            pref.setManifestAndroidAttribute("icon", "@drawable/ic_clipboard_history_entry")

            val intent = pref.childElements("intent").firstOrNull()
                ?: doc.createElement("intent").also(pref::appendChild)
            intent.setManifestAndroidAttribute("action", "android.intent.action.MAIN")
            if (packageName.isNotBlank()) {
                intent.setManifestAndroidAttribute("targetPackage", packageName)
            }
            intent.setManifestAndroidAttribute("targetClass", CLIPBOARD_ACTIVITY_CLASS)
            Unit
        }
    } catch (_: Throwable) {
    }
}

private fun injectIntoSettingsRoot(doc: Document, packageName: String) {
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
    prefEntry.setManifestAndroidAttribute("title", "@string/gboard_clipboard_history_title")
    prefEntry.setManifestAndroidAttribute("summary", "@string/gboard_clipboard_history_summary")
    prefEntry.setManifestAndroidAttribute("selectable", "true")
    prefEntry.setManifestAndroidAttribute("key", CLIPBOARD_PREF_KEY)
    prefEntry.setManifestAndroidAttribute("icon", "@drawable/ic_clipboard_history_entry")

    val intent = prefEntry.childElements("intent").firstOrNull()
        ?: doc.createElement("intent").also(prefEntry::appendChild)
    intent.setManifestAndroidAttribute("action", "android.intent.action.MAIN")
    if (packageName.isNotBlank()) {
        intent.setManifestAndroidAttribute("targetPackage", packageName)
    }
    intent.setManifestAndroidAttribute("targetClass", CLIPBOARD_ACTIVITY_CLASS)
}

private fun Element.childElements(tagName: String? = null): Sequence<Element> = sequence {
    val children: NodeList = childNodes
    for (i in 0 until children.length) {
        val node = children.item(i)
        if (node is Element && (tagName == null || node.tagName == tagName || node.localName == tagName)) {
            yield(node)
        }
    }
}

private fun Element.manifestAndroidAttribute(name: String): String? {
    val attr = getAttributeNodeNS(ANDROID_NS, name)
        ?: getAttributeNode("android:$name")
        ?: getAttributeNode(name)
    return attr?.value
}

private fun Element.setManifestAndroidAttribute(name: String, value: String) {
    if (hasAttributeNS(ANDROID_NS, name)) {
        setAttributeNS(ANDROID_NS, "android:$name", value)
    } else {
        setAttribute("android:$name", value)
    }
}
