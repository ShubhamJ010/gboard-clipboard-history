group = "com.sj010.gboardclipboard"

patches {
    about {
        name = "Gboard Clipboard History"
        description = "Standalone clipboard history manager for Gboard with search, export, launcher icon, and ContentProvider API."
        source = "https://github.com/sj010/gboard-clipboard-history"
        author = "sj010"
        contact = "https://github.com/sj010/gboard-clipboard-history/issues"
        website = "https://github.com/sj010/gboard-clipboard-history"
        license = "GPLv3"
    }
}

val syncExtensionTask = project(":extensions:extension").tasks.named("syncExtension")

tasks.named("processResources") {
    dependsOn(syncExtensionTask)
    from(syncExtensionTask.map { it.outputs.files.singleFile }) {
        into("extensions")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
