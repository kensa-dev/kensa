import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilerExecutionStrategy

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

    implementation(libs.kotlinCompilerEmbeddable)

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
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitPlatformLauncher)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.junitJupiterParams)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.mockitoKotlin)
}

tasks {
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
