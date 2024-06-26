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
                jvmTarget = JavaVersion.VERSION_1_8.toString()
            }
        }
    }


    sourceSets {
        all {
            languageSettings {
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }

        val commonMain by getting {
            dependencies {
                api(project(":core"))
                implementation(libs.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))

                implementation(libs.coroutines.test)
                implementation(libs.flows.test)
                implementation(libs.mockk.test)
            }
        }

        val jvmMain by getting {
            dependsOn(commonMain)
        }

        val androidMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(libs.androidx.lifecycle.livedata)
            }
        }
    }
}

dependencies {
    api(project(":core"))
}

android {
    namespace = "com.langdroid.summary"
    defaultConfig {
        minSdk = 21
        compileSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

