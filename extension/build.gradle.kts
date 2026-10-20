extension {
    name = "extensions/gboard-clipboard-extension.rve"
}

android {
    namespace = "com.sj010.gboardclipboard.extension"

    defaultConfig {
        minSdk = 26
        targetSdk = 35
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
