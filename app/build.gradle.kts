plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.play.publisher)
    alias(libs.plugins.aboutlibraries.android)
}

val aboutProjectUrl =
    providers.gradleProperty("aboutProjectUrl")
        .orElse("example.com")
        .get()
        .ifBlank { "example.com" }
val aboutContactEmail =
    providers.gradleProperty("aboutContactEmail")
        .orElse("contact@example.com")
        .get()
        .ifBlank { "contact@example.com" }

fun String.toBuildConfigString(): String = "\"${replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "de.t_animal.opensourcebodytracker"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "de.t_animal.opensourcebodytracker"
        minSdk = 29
        targetSdk = 36
        versionCode = 20260406
        versionName = "2026.04-06"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "ABOUT_PROJECT_URL", aboutProjectUrl.toBuildConfigString())
        buildConfigField("String", "ABOUT_CONTACT_EMAIL", aboutContactEmail.toBuildConfigString())
    }

    signingConfigs {
        create("release") {
            storeFile = System.getenv("KEYSTORE_PATH")?.let { file(it) }
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        create("screenshot") {
            initWith(getByName("debug"))

            applicationIdSuffix = ".screenshot"
            versionNameSuffix = "-screenshot"

            matchingFallbacks += listOf("debug")
            ndk {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
            }
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }

    testOptions {
        animationsDisabled = true
    }

    testBuildType = "screenshot"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

ksp {
    arg("room.generateKotlin", "true")
}

play {
    val credentialsPath = System.getenv("PLAY_SERVICE_ACCOUNT_JSON_PATH")
    if (credentialsPath != null) {
        serviceAccountCredentials.set(file(credentialsPath))
    }
    track.set(System.getenv("PLAY_TRACK") ?: "internal")
    defaultToAppBundles.set(true)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
    implementation(libs.reorderable)
    implementation(libs.aboutlibraries.compose.m3)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.tink.android)
    implementation(libs.zip4j)
    implementation(libs.androidx.work.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.commons.csv)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.uiautomator)
}
