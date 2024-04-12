plugins {
    `kotlin-dsl`
}

group = "com.langroid.plugins"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradle.build)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("library-module-publish") {
            id = "library-module-publish"
            implementationClass = "ModulePublishPlugin"
        }
    }
}