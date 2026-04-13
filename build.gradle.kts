plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.play.publisher) apply false
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.aboutlibraries.android) apply false
}

subprojects {
    fun Project.applyAndConfigureKtlint() {
        apply(plugin = "org.jlleitschuh.gradle.ktlint")

        extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
            outputToConsole.set(true)
            ignoreFailures.set(false)

            filter {
                exclude("**/build/**")
                exclude("**/generated/**")
            }
        }

        tasks.matching { it.name == "check" }.configureEach {
            dependsOn("ktlintCheck")
        }
    }

    // AGP 9+ can use "built-in Kotlin" where the Kotlin Gradle plugin IDs are not applied.
    // Apply ktlint based on Android plugin presence too so we still get lint tasks.
    plugins.withId("com.android.application") {
        applyAndConfigureKtlint()
    }
    plugins.withId("com.android.library") {
        applyAndConfigureKtlint()
    }

    plugins.withId("org.jetbrains.kotlin.android") {
        applyAndConfigureKtlint()
    }
    plugins.withId("org.jetbrains.kotlin.jvm") {
        applyAndConfigureKtlint()
    }
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        applyAndConfigureKtlint()
    }
}
