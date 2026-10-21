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

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

// The Gradle build packages the metadata from src/main/resources. The copies in
// the repository root are the ones the shell scripts use and the ones that get
// reviewed, so copy them over before the bundle is assembled. A metadata file
// that exists in only one place would ship a bundle nobody looked at.
val metadataRoot = rootProject.projectDir
val metadataResources = File(projectDir, "src/main/resources")
val metadataNames = listOf("patches-list.json", "patches-bundle.json")

tasks.matching { it.name == "buildAndroid" }.configureEach {
    doFirst {
        metadataNames.forEach { name ->
            val source = File(metadataRoot, name)
            val target = File(metadataResources, name)
            if (!source.exists()) return@forEach
            val bytes = source.readBytes()
            if (!target.exists() || !bytes.contentEquals(target.readBytes())) {
                target.parentFile.mkdirs()
                target.writeBytes(bytes)
                logger.lifecycle("synced $name into patches/src/main/resources")
            }
        }
    }
}
