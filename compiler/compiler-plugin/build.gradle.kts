import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerExecutionStrategy
import org.gradle.api.tasks.testing.Test

sourceSets {
    create("testFixtures", Action {
        kotlin.srcDir("src/testFixtures/kotlin")
    })
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("testFixtures") {
    usingSourceSet(sourceSets.getByName("testFixtures"))
}

dependencies {
    implementation(project(":core"))

    compileOnly(libs.kotlinCompilerEmbeddable)

    "testFixturesImplementation"(project(":core"))
    "testFixturesImplementation"(project(":core")) {
        capabilities {
            requireCapability("dev.kensa:core-hooks")
        }
    }

    testImplementation(project(":core"))
    testImplementation(project(":core")) {
        capabilities {
            requireCapability("dev.kensa:core-hooks")
        }
    }
    testImplementation(sourceSets["testFixtures"].output)
    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(libs.junit6JupiterParams)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.kotlinCompilerEmbeddable)
}

tasks {
    named<Test>("test") {
        dependsOn(named<Jar>("jar"))
        val pluginJarPath = provider {
            named<Jar>("jar").get().archiveFile.get().asFile.absolutePath
        }
        doFirst {
            systemProperty("kensa.plugin.jar", pluginJarPath.get())
        }
    }

    named<KotlinCompile>("compileTestFixturesKotlin") {
        dependsOn(project(":compiler-plugin").tasks.named<Jar>("jar"))

        val pluginJarPath = provider {
            project(":compiler-plugin")
                .tasks.named<Jar>("jar").get()
                .archiveFile.get().asFile.absolutePath
        }
        compilerExecutionStrategy.set(KotlinCompilerExecutionStrategy.IN_PROCESS)

        compilerOptions {
            verbose.set(true)
            freeCompilerArgs.addAll(
                "-Xplugin=${pluginJarPath.get()}"
            )
        }
    }
}
