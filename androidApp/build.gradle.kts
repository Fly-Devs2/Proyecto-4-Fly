import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleServices)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
dependencies {
    implementation(projects.composeApp)
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.auth.credentials)
    implementation(libs.androidx.auth.credentials.play)
    implementation(libs.androidx.auth.googleid)

    implementation(libs.compose.uiToolingPreview)
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.gitlive.firebase.common)
    implementation(libs.gitlive.firebase.analytics)
    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
    debugImplementation(libs.compose.uiTooling)

}

android {
    namespace = "ucenfotec.ac.cr.flydevs"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "ucenfotec.ac.cr.flydevs"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
