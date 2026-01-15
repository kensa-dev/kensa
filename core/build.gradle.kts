import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "A BDD testing framework for Kotlin & Java"

sourceSets {
    create("hooks", Action {
        java.srcDir("src/hooks/java")
        kotlin.srcDir("src/hooks/kotlin")

        compileClasspath += sourceSets["main"].output
    })

    create("example", Action {
        java.srcDir("src/example/java")
        kotlin.srcDir("src/example/kotlin")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    })

    create("agent", Action {
        kotlin.srcDir("src/agent/kotlin")

        compileClasspath += sourceSets["main"].output
    })

    create("testSupport", Action {
        kotlin.srcDir("src/testSupport/kotlin")

        compileClasspath += sourceSets["test"].output
    })
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("hooks") {
    usingSourceSet(sourceSets.getByName("hooks"))
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("agent") {
    usingSourceSet(sourceSets.getByName("agent"))
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("testSupport") {
    usingSourceSet(sourceSets.getByName("testSupport"))
    capability("dev.kensa", "core-test-support", "$version")
}

dependencies {
    compileOnly(project(":antlr"))
    implementation(libs.antlr)
    implementation(libs.kotlinReflect)
    implementation(libs.minimalJson)
    implementation(libs.plantuml)
    implementation(libs.pebble)
    "agentImplementation"(libs.byteBuddyCore)

    testImplementation(platform(libs.junitBom))
    testImplementation(project(":antlr"))
    testImplementation(libs.junitPlatformLauncher)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.junitJupiterParams)

    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.hamcrestCore)
    testImplementation(libs.mockitoKotlin)
    testImplementation(sourceSets["example"].output)

    "testSupportImplementation"(project(":core"))
}

tasks {
    named("agentJar", Jar::class.java) {
        archiveBaseName.set(rootProject.name)
        manifest {
            attributes(
                mapOf(
                    "Premain-Class" to "dev.kensa.agent.KensaAgent",
                    "Agent-Class" to "dev.kensa.agent.KensaAgent",
                    "Boot-Class-Path" to "",
                    "Can-Redefine-Classes" to "true",
                    "Can-Retransform-Classes" to "true",
                    "Can-Set-Native-Method-Prefix" to "true",
                    "Implementation-Title" to "Kensa Agent",
                    "Implementation-Version" to rootProject.version
                )
            )
        }
    }

    named<KotlinCompile>("compileExampleKotlin") {
        compilerOptions {
            freeCompilerArgs.addAll("-Xcontext-parameters")
        }
    }

    named<Jar>("jar") {
        dependsOn(":ui:viteBuild")
        from(project(":antlr").sourceSets["main"].output)
        from(project(":ui").layout.buildDirectory.dir("js").get()) {
            into("/")
        }
    }
}