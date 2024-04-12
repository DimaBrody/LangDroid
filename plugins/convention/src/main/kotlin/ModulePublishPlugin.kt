import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import com.android.build.gradle.LibraryExtension
import com.langdroid.plugins.configureAndroidPublishing
import org.gradle.kotlin.dsl.getByType

abstract class ModulePublishPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Ensure the 'maven-publish' plugin is applied
        with(project){
            pluginManager.apply("maven-publish")
//            pluginManager.apply("com.android.library")
//            val extension = extensions.getByType<LibraryExtension>()
//            configureAndroidPublishing(extension)

            afterEvaluate {
                val publishing = project.extensions.getByType(PublishingExtension::class.java)
                publishing.publications {
                    val mavenPublication = create("maven", MavenPublication::class.java) {
                        // Configuration specific to MavenPublication
                    }

                    mavenPublication.apply {
                        // Assumes 'release' component is correctly configured
                        from(project.components.findByName("release"))

                        groupId = "com.langdroid.modules"
                        artifactId = project.name
                        version = project.version.toString()
                    }
                }
            }
        }
    }
}