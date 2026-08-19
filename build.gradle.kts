plugins {
    id("com.android.application") version "9.2.1" apply false
    id("org.jetbrains.kotlin.android") version "2.3.10" apply false
    // protobuf-gradle-plugin removed: incompatible with AGP 9.0.0
    // Proto compilation is handled manually in app/build.gradle.kts
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
    id("com.google.devtools.ksp") version "2.3.2" apply false
    id("org.sonarqube") version "7.2.2.6593"
}


allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
