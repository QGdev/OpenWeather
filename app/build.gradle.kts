import org.gradle.process.ExecOperations

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "fr.qgdev.openweather"
    compileSdk = 37

    defaultConfig {
        applicationId = "fr.qgdev.openweather"
        minSdk = 30
        targetSdk = 36
        versionCode = 10
        versionName = "0.9.5"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            versionNameSuffix = "-beta"
            isDebuggable = false
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            versionNameSuffix = "-beta_debug"
            isDebuggable = true
        }
        create("nightly") {
            versionNameSuffix = "-alpha"
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }
    compileSdkMinor = 0
    buildToolsVersion = "37.0.0"
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.8")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.8")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    // Compose BOM keeps every Compose artifact on one consistent version set.
    // 2026.06.01 pins ui/foundation 1.11.4 and material3 1.4.0 - the versions
    // previously declared here by hand, so adopting it changes no behaviour.
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui-android")
    implementation("androidx.compose.foundation:foundation-layout-android")
    implementation("androidx.compose.material3:material3-android")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.foundation:foundation-android")
    implementation("androidx.compose.ui:ui-tooling-preview-android")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.datastore:datastore:1.2.1")
    implementation("androidx.datastore:datastore-core:1.2.1")
    implementation("com.google.protobuf:protobuf-javalite:4.35.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.11.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20260522")
    debugImplementation(platform("androidx.compose:compose-bom:2026.06.01"))
    debugImplementation("androidx.compose.ui:ui-tooling")
}

// Manual protobuf compilation to replace protobuf-gradle-plugin (incompatible with AGP 9.0.0)
// Must match the protobuf-javalite runtime version declared above, otherwise the generated
// sources and the runtime they compile against can drift apart.
val protocVersion = "4.35.1"
val protoSrcDir = layout.projectDirectory.dir("src/main/proto")
val protoGeneratedDir = layout.buildDirectory.dir("generated/source/proto/main/java")

// Determine the correct protoc artifact classifier for the current OS/arch
fun protocArtifact(): String {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    val os = when {
        osName.contains("linux") -> "linux"
        osName.contains("mac") || osName.contains("darwin") -> "osx"
        osName.contains("win") -> "windows"
        else -> error("Unsupported OS: $osName")
    }
    val arch = when {
        osArch == "amd64" || osArch == "x86_64" -> "x86_64"
        osArch == "aarch64" || osArch == "arm64" -> "aarch_64"
        else -> error("Unsupported architecture: $osArch")
    }
    return "com.google.protobuf:protoc:$protocVersion:$os-$arch@exe"
}

val protocExe: Configuration by configurations.creating
dependencies {
    protocExe(protocArtifact())
}

abstract class GenerateProtoTask : DefaultTask() {

    @get:Inject
    abstract val execOps: ExecOperations

    @get:InputDirectory
    abstract val protoDir: DirectoryProperty

    @get:InputFile
    abstract val protocPath: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outDir = outputDir.get().asFile
        outDir.mkdirs()

        val protoc = protocPath.get().asFile
        protoc.setExecutable(true)

        val inputDir = protoDir.get().asFile
        val protoFiles = inputDir.listFiles()?.filter { it.extension == "proto" }?.map { it.absolutePath } ?: emptyList()

        execOps.exec {
            executable = protoc.absolutePath
            args(
                "--java_out=lite:${outDir.absolutePath}",
                "--proto_path=${inputDir.absolutePath}",
            )
            args(protoFiles)
        }
    }
}

val generateProto by tasks.registering(GenerateProtoTask::class) {
    protoDir.set(protoSrcDir)
    protocPath.set(layout.file(provider { protocExe.singleFile }))
    outputDir.set(protoGeneratedDir)
}

// Register generated proto sources for all variants using the Variant API (AGP 9.0.0+)
androidComponents {
    onVariants { variant ->
        variant.sources.java?.addGeneratedSourceDirectory(
            generateProto,
            GenerateProtoTask::outputDir
        )
    }
}

tasks.configureEach {
    if (name.startsWith("compile") && (name.endsWith("JavaWithJavac") || name.endsWith("Kotlin"))) {
        dependsOn(generateProto)
    }
}
