plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.diffplug.spotless")
    id("maven-publish")
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
        all {
            languageSettings {
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines.core)
                api(project(":core"))
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

android {
    namespace = "com.langdroid.summary"
    defaultConfig {
        minSdk = 21
        compileSdk = 34
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components.findByName("release"))
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
//            from(components["release"])
        }
    }
}