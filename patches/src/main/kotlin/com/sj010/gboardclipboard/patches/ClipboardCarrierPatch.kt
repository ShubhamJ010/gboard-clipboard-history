package com.sj010.gboardclipboard.patches

import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import java.io.ByteArrayInputStream
import java.io.InputStream

private fun findMethodByPrefix(clazz: Class<*>, prefix: String, vararg paramTypes: Class<*>): java.lang.reflect.Method {
    return (clazz.methods + clazz.declaredMethods).firstOrNull { method ->
        method.name.startsWith(prefix) && method.parameterTypes.contentEquals(paramTypes)
    } ?: error("Method starting with '$prefix' not found on $clazz")
}

val gboardClipboardCarrierPatch = bytecodePatch(
    description = "Merge standalone Clipboard History runtime extension DEX into target Gboard APK."
) {
    execute {
        mergeBundledExtension(this, "extensions/gboard-clipboard-extension.rve")
    }
}

internal fun mergeBundledExtension(context: BytecodePatchContext, extension: String) {
    val resourceClassLoader = object {}.javaClass.classLoader
    val runtimeClassLoader = context.javaClass.classLoader
    val extensionStream = resourceClassLoader.getResourceAsStream(extension)
        ?: error("Bundled extension '$extension' not found in patch resources")

    val extensionBytes = extensionStream.use { it.readBytes() }

    val mergeContextClass = Class.forName("app.morphe.patcher.context.MergeContext", true, runtimeClassLoader)
    val mergeMethod = findMethodByPrefix(
        mergeContextClass,
        "mergeExtension",
        InputStream::class.java
    )

    mergeMethod.invoke(context, ByteArrayInputStream(extensionBytes))
}
