plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.0"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.22")
    implementation("org.jetbrains.compose.material:material:1.6.0")
    implementation("org.jetbrains.compose.material3:material3:1.6.0")
    implementation("org.jetbrains.compose.ui:ui:1.6.0")
    implementation("org.jetbrains.compose.ui:ui-tooling-preview:1.6.0")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.6.0")
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha06")
    implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha06")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
    implementation("com.facebook:ktfmt:0.44")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "ComposeViewer"
            packageVersion = "1.0.0"
        }
    }
}

val composeCompiler = configurations.detachedConfiguration(
    dependencies.create("androidx.compose.compiler:compiler:1.5.8")
)

tasks.withType<JavaExec> {
    systemProperty("compose.compiler.path", composeCompiler.singleFile.absolutePath)
    val jvmArguments = mutableListOf("-Xmx4g")
    if (System.getProperty("os.name").contains("mac", ignoreCase = true)) {
        jvmArguments.add("-Xdock:name=Compose Viewer")
    }
    jvmArgs(jvmArguments)
}
