plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10"
}

group = "com.example"
version = "1.0.2"

kotlin {
    jvmToolchain(17)
}
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.1.10")
    implementation("org.jetbrains.compose.material:material:1.7.3")
    implementation("org.jetbrains.compose.material3:material3:1.7.3")
    implementation("org.jetbrains.compose.ui:ui:1.7.3")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.7.3")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0")
    implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.0")
    implementation("io.ktor:ktor-client-cio:3.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.facebook:ktfmt:0.53")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs(
            "--add-opens=java.base/java.lang=ALL-UNNAMED",
            "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
            "--add-opens=java.base/java.io=ALL-UNNAMED",
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "-Xmx4g"
        )
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "ComposeViewer"
            packageVersion = "1.0.2"

            macOS {
                bundleID = "com.example.composeviewer"
                dockName = "Compose Viewer"
            }
            
            modules("java.desktop", "java.instrument", "java.logging", "java.prefs", "java.rmi", "java.scripting", "java.sql", "jdk.unsupported", "jdk.crypto.ec")
            
            // Pass JVM args to the packaged app
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))
        }
    }
}

val composeCompiler = configurations.detachedConfiguration(
    dependencies.create("org.jetbrains.kotlin:kotlin-compose-compiler-plugin-embeddable:2.1.10")
)

tasks.withType<JavaExec> {
    systemProperty("compose.compiler.path", composeCompiler.singleFile.absolutePath)
    val jvmArguments = mutableListOf("-Xmx4g")
    if (System.getProperty("os.name").contains("mac", ignoreCase = true)) {
        jvmArguments.add("-Xdock:name=Compose Viewer")
    }
    jvmArgs(jvmArguments)
}

// Task to copy compiler plugin to resources for packaged app
val copyCompilerPlugin by tasks.registering(Copy::class) {
    from(composeCompiler)
    into("src/main/resources")
    rename { "compose-compiler.jar" }
}

// Make processResources depend on this task so the file is ready before packaging
tasks.named("processResources") {
    dependsOn(copyCompilerPlugin)
}





