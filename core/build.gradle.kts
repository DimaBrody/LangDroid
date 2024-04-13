plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.diffplug.spotless")
    id("library-module-publish")
}

kotlin {
    explicitApi()
    jvm()

    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = JavaVersion.VERSION_11.toString()
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                implementation(libs.tokenizer)
                implementation(libs.openai)
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.gemini)
            }
        }

    }

    publishing {
        publications.withType<MavenPublication>().named("jvm").configure {
            // Assuming the existing publication might not include source JAR
            // Add a sources jar if it's not already added
            artifact(tasks.named("jvmSourcesJar").get()) {
                classifier = "sources"
            }
        }
    }
}

android {
    namespace = "com.langdroid.core"
    defaultConfig {
        minSdk = 21
        compileSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}
