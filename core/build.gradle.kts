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
}

dependencies {
    antlr(libs.antlr)

    implementation(libs.kotlinReflect)
    implementation(libs.minimalJson)
    implementation(libs.plantuml)
    implementation(libs.pebble)

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitPlatformLauncher)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.junitJupiterParams)

    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.hamcrestCore)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.mockitoKotlin)
    testImplementation(sourceSets["example"].output)
}

tasks {
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
    }
}