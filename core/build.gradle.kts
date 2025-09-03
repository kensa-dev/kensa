import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

description = "A BDD testing framework for Kotlin & Java"

plugins {
    antlr
}

sourceSets {
    create("example") {
        java.srcDir("src/example/java")
        kotlin.srcDir("src/example/kotlin")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }

    create("agent") {
        kotlin.srcDir("src/agent/kotlin")

        compileClasspath += sourceSets["main"].output
    }
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("agent") {
    usingSourceSet(sourceSets.getByName("agent"))
}

dependencies {
    antlr(libs.antlr)

    implementation(libs.kotlinReflect)
    implementation(libs.minimalJson)
    implementation(libs.plantuml)
    implementation(libs.pebble)
    "agentImplementation"(libs.byteBuddyCore)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitPlatformLauncher)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.junitJupiterParams)

    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.hamcrestCore)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.mockitoKotlin)
    testImplementation(sourceSets["example"].output)
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

    withType<AntlrTask> {
        outputDirectory = file("$outputDirectory/dev/kensa/parse")
        arguments = arguments + listOf("-listener", "-no-visitor", "-package", "dev.kensa.parse")
    }

    withType<KotlinCompile> {
        dependsOn("generateGrammarSource")
    }

    named<Jar>("jar") {
        dependsOn(":ui:viteBuild")

        from(project(":ui").layout.buildDirectory.dir("js").get()) {
            into("/")
        }

        manifest {
            attributes(
                "Premain-Class" to "dev.kensa.agent.NestedSentenceAgent",
                "Can-Redefine-Classes" to "true",
                "Can-Retransform-Classes" to "true",
                "Can-Set-Native-Method-Prefix" to "false"
            )
        }
    }
}