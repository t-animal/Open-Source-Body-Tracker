// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<KtlintExtension> {
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
