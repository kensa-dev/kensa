import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.file.Paths

description = "Kensa for JUnit5"

private val uiBuildDir = Paths.get(project(":ui").layout.buildDirectory.get().toString(), "js")

sourceSets {
    create("testSupport", Action {
        kotlin.srcDir("src/testSupport/kotlin")
        java.srcDir("src/testSupport/java")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    })

    create("kotlinTest", Action {
        kotlin.srcDir("src/kotlinTest/kotlin")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    })

    create("javaTest", Action {
        java.srcDir("src/javaTest/java")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    })

    create("javaExample", Action {
        java.srcDir("src/javaExample/java")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    })

    create("kotlinExample", Action {
        kotlin.srcDir("src/kotlinExample/kotlin")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    })
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("testSupport") {
    usingSourceSet(sourceSets.getByName("testSupport"))
    capability("dev.kensa", "framework-junit-test-support", "$version")
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("javaExample") {
    usingSourceSet(sourceSets.getByName("javaExample"))
    capability("dev.kensa", "framework-junit-java-example", "$version")
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("kotlinExample") {
    usingSourceSet(sourceSets.getByName("kotlinExample"))
    capability("dev.kensa", "framework-junit-kotlin-example", "$version")
}

// Wire the Kotlin compiler plugin JAR into only the kotlinExample compilation
plugins.withId("org.jetbrains.kotlin.jvm") {
    tasks.named("compileKotlinExampleKotlin", KotlinCompile::class.java).configure {
        compilerOptions.freeCompilerArgs.addAll(
            listOf(
                "-Xplugin=${project(":compiler-plugin").tasks.named<Jar>("jar").get().archiveFile.get().asFile.absolutePath}",
                "-P",
                "plugin:dev.kensa.compiler-plugin:enabled=true",
                "-P",
                "plugin:dev.kensa.compiler-plugin:debug=true"
            )
        )

        dependsOn(project(":compiler-plugin").tasks.named("jar"))
    }
}

dependencies {
    api(project(":core"))
    compileOnly(project(":antlr"))
    implementation(libs.antlr)
    implementation(libs.kotlinReflect)

    implementation(platform(libs.junitBom))
    implementation(libs.junitJupiterParams)
    implementation(libs.junitJupiterApi)
    implementation(libs.junitJupiterEngine)
    // TODO - Remove when package stuff not reliant on JUnit
    implementation(libs.junitPlatformLauncher)

    testRuntimeOnly(sourceSets["javaExample"].output)
    testRuntimeOnly(sourceSets["kotlinExample"].resources)
    testRuntimeOnly(project(":assertions-hamcrest"))
    testRuntimeOnly(project(":assertions-hamkrest"))

    "testSupportApi"(project(":core"))
    "testSupportApi"(project(":core")) {
        capabilities {
            requireCapability("dev.kensa:core-test-support")
        }
    }
    "testSupportApi"(platform(libs.kotestBom))
    "testSupportApi"(libs.kotestAssertionsCoreJvm)
    "testSupportApi"(libs.kotestAssertionsJson)
    "testSupportApi"(libs.jsoup)
    "testSupportApi"(libs.minimalJson)
    "testSupportApi"(platform(libs.junitBom))
    "testSupportApi"(libs.junitJupiterEngine)
    "testSupportApi"(libs.junitJupiterApi)
    "testSupportApi"(libs.junitJupiterParams)
    "testSupportApi"(libs.junitPlatformTestKit)
    "testSupportApi"(libs.junitPlatformEngine)

    "javaTestImplementation"(project(":framework-junit")) {
        capabilities {
            requireCapability("dev.kensa:framework-junit-test-support")
        }
    }
    "javaTestImplementation"(project(":framework-junit")) {
        capabilities {
            requireCapability("dev.kensa:framework-junit-java-example")
        }
    }

    "kotlinTestImplementation"(project(":framework-junit")) {
        capabilities {
            requireCapability("dev.kensa:framework-junit-test-support")
        }
    }
    "kotlinTestImplementation"(project(":framework-junit")) {
        capabilities {
            requireCapability("dev.kensa:framework-junit-kotlin-example")
        }
    }

    "javaExampleImplementation"(project(":framework-junit")) {
        capabilities {
            requireCapability("dev.kensa:framework-junit-test-support")
        }
    }
    "javaExampleImplementation"(project(":assertions-hamcrest"))
    "javaExampleImplementation"(project(":core"))
    "javaExampleImplementation"(platform(libs.junitBom))
    "javaExampleImplementation"(libs.junitJupiterApi)
    "javaExampleImplementation"(libs.junitJupiterParams)
    "javaExampleRuntimeOnly"(files(uiBuildDir))
    "javaExampleRuntimeOnly"(project(":core")) {
        capabilities {
            requireCapability("dev.kensa:core-agent")
        }
    }

    "kotlinExampleImplementation"(project(":framework-junit")) {
        capabilities {
            requireCapability("dev.kensa:framework-junit-test-support")
        }
    }
    "kotlinExampleImplementation"(project(":core")) {
        capabilities {
            requireCapability("dev.kensa:core-hooks")
        }
    }
    "kotlinExampleImplementation"(project(":assertions-hamkrest"))
    "kotlinExampleImplementation"(project(":core"))
    "kotlinExampleImplementation"(platform(libs.junitBom))
    "kotlinExampleImplementation"(libs.junitJupiterApi)
    "kotlinExampleImplementation"(libs.junitJupiterParams)
    "kotlinExampleRuntimeOnly"(files(uiBuildDir))
}

tasks {
    register<Test>("javaTest") {
        group = "verification"
        description = "Runs the Java tests."
        dependsOn(":core:agentJar")

        testClassesDirs = sourceSets["javaTest"].output.classesDirs
        classpath = sourceSets["javaTest"].runtimeClasspath

        jvmArgs("""-javaagent:${project(":core").tasks.named<Jar>("agentJar").get().archiveFile.get().asFile.absolutePath}""")
    }

    register<Test>("kotlinTest") {
        group = "verification"
        description = "Runs the Kotlin tests."

        testClassesDirs = sourceSets["kotlinTest"].output.classesDirs
        classpath = sourceSets["kotlinTest"].runtimeClasspath
    }

    named("check") {
        dependsOn("javaTest", "kotlinTest")
    }
}
