package com.langdroid.plugins

import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

internal fun Project.configureAndroidPublishing(
    commonExtension: CommonExtension<*, *, *, *, *>,
) {
    commonExtension.apply {
        (this as LibraryExtension).publishing {
            singleVariant("release") {
                withSourcesJar()
            }
        }
    }
}