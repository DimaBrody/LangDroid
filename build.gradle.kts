import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.kotlin.multiplaform) apply false
    alias(libs.plugins.kotlinx.binary.validator) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.maven.publish) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.google.secrets) apply false
}

buildscript {
    dependencies {
        classpath(libs.android.gradle.build)
//        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.5")
    }
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    tasks.withType<Test> {
        testLogging {
            events(
                TestLogEvent.STARTED,
                TestLogEvent.PASSED,
                TestLogEvent.SKIPPED,
                TestLogEvent.FAILED
            )
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = false
        }
    }
}

