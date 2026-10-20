package com.sj010.gboardclipboard.patches

import org.w3c.dom.Attr
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

internal const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

internal fun Element.childElements(name: String? = null): Sequence<Element> =
    (0 until childNodes.length)
        .asSequence()
        .map { childNodes.item(it) }
        .filter { it.nodeType == Node.ELEMENT_NODE && (name == null || it.nodeName == name) }
        .filterIsInstance<Element>()

internal fun Element.manifestAndroidAttribute(localName: String): String? {
    val namespaced = getAttributeNS(ANDROID_NS, localName)
    if (namespaced.isNotBlank()) return namespaced
    return getAttribute("android:$localName").takeIf { it.isNotBlank() }
}

internal fun Element.setManifestAndroidAttribute(localName: String, value: String) {
    try {
        setAttributeNS(ANDROID_NS, "android:$localName", value)
    } catch (_: Throwable) {
        setAttribute("android:$localName", value)
    }
}

internal fun ensureManifestComponent(
    document: Document,
    application: Element,
    tagName: String,
    className: String,
): Element {
    val component = application.childElements(tagName).firstOrNull {
        it.manifestAndroidAttribute("name") == className
    } ?: document.createElement(tagName).also(application::appendChild)
    component.setManifestAndroidAttribute("name", className)
    return component
}

internal fun ensureManifestPermission(
    document: Document,
    manifest: Element,
    permissionName: String,
    protectionLevel: String = "signature",
) {
    val permission = manifest.childElements("permission").firstOrNull {
        it.manifestAndroidAttribute("name") == permissionName
    } ?: document.createElement("permission").also { created ->
        val application = manifest.childElements("application").firstOrNull()
        if (application == null) {
            manifest.appendChild(created)
        } else {
            manifest.insertBefore(created, application)
        }
    }
    permission.setManifestAndroidAttribute("name", permissionName)
    permission.setManifestAndroidAttribute("protectionLevel", protectionLevel)
}

internal fun ensureManifestIntentFilter(
    document: Document,
    component: Element,
    actionName: String,
    categoryName: String? = null,
) {
    val intentFilter = component.childElements("intent-filter").firstOrNull {
        it.childElements("action").any { action ->
            action.manifestAndroidAttribute("name") == actionName
        }
    } ?: document.createElement("intent-filter").also(component::appendChild)

    val action = intentFilter.childElements("action").firstOrNull {
        it.manifestAndroidAttribute("name") == actionName
    } ?: document.createElement("action").also(intentFilter::appendChild)
    action.setManifestAndroidAttribute("name", actionName)

    if (categoryName != null) {
        val category = intentFilter.childElements("category").firstOrNull {
            it.manifestAndroidAttribute("name") == categoryName
        } ?: document.createElement("category").also(intentFilter::appendChild)
        category.setManifestAndroidAttribute("name", categoryName)
    }
}
