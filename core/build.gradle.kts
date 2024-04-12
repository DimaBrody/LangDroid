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
}

android {
    namespace = "com.langdroid.core"
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
            groupId = "com.langdroid"
            artifactId = project.name
            version = project.version.toString()
        }
    }
}