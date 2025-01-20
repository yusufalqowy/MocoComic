import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.androidx.navigation.safeargs)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.ktlint)
}

val props =
    Properties().apply {
        load(FileInputStream(File(rootProject.rootDir, "local.properties")))
    }

android {
    namespace = "yu.desk.mococomic"
    compileSdk = 35

    defaultConfig {
        applicationId = "yu.desk.mococomic"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
    flavorDimensions += "env"
    productFlavors {
        create("dev") {
            dimension = "env"
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "Moco Comic Dev")
        }
        create("prod") {
            dimension = "env"
            resValue("string", "app_name", "Moco Comic")
        }
        create("stag") {
            dimension = "env"
            applicationIdSuffix = ".stag"
            resValue("string", "app_name", "Moco Comic Stag")
        }
    }
}

tasks.getByPath("preBuild").dependsOn("ktlintFormat")

ktlint {
    version = "1.5.0"
    android = true
    ignoreFailures = false
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.HTML)
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.annotation)

    implementation(libs.androidx.window)
    implementation(libs.androidx.window.core.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Splash screen
    implementation(libs.androidx.core.splashscreen)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coil
    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)

    // Networking
    implementation(libs.gson)
    implementation(libs.retrofit)

    // Network Interceptor
    debugImplementation(libs.chucker.library)
    releaseImplementation(libs.chucker.library.no.op)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Shimmer Loading
    implementation(libs.shimmer)

    // Android Browser
    implementation(libs.androidx.browser)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}