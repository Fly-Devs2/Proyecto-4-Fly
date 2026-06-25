import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices) version "4.5.0" apply false
}

dependencies {
    implementation(projects.composeApp)
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.gitlive.firebase.common)
    implementation(libs.gitlive.firebase.analytics)


}

compose.desktop {
    application {
        mainClass = "ucenfotec.ac.cr.flydevs.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ucenfotec.ac.cr.flydevs"
            packageVersion = "1.0.0"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
