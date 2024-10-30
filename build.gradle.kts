import com.github.gradle.node.task.NodeTask
import org.gradle.api.JavaVersion.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.nodeGradle)
    alias(libs.plugins.nexusPublish)
    antlr
    signing
    `maven-publish`
}

group = "dev.kensa"
version = project.properties["releaseVersion"] ?: "SNAPSHOT"

fun createSourceSet(name: String) {
    sourceSets {
        create(name) {
            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        }
    }
}

createSourceSet("junitIntegrationTest")
val junitIntegrationTestImplementation: Configuration by configurations.getting { extendsFrom(configurations.implementation.get()) }

createSourceSet("javaExampleTest")
val javaExampleTestImplementation: Configuration by configurations.getting { extendsFrom(configurations.implementation.get()) }

createSourceSet("kotlinExampleTest")
val kotlinExampleTestImplementation: Configuration by configurations.getting { extendsFrom(configurations.implementation.get()) }

nexusPublishing {
    val nexusUsername: String? by project
    val nexusPassword: String? by project

    repositories {
        sonatype {
            username = nexusUsername
            password = nexusPassword
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    antlr(libs.antlr)

    api(libs.kotlinStdLib)
    api(libs.kotlinReflect)

    implementation(libs.kotlinCoroutines)
    implementation(libs.junitJupiterParams)
    implementation(libs.junitJupiterApi)
    implementation(libs.junitJupiterEngine)
    implementation(libs.assertJCore)
    implementation(libs.hamcrestCore)
    implementation(libs.awaitilityKotlin)
    implementation(libs.minimalJson)
    implementation(libs.plantuml)
    implementation(libs.pebble)
    implementation(libs.kotestAssertionsCoreJvm)

    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.kotlinCoroutinesTest)

    junitIntegrationTestImplementation(libs.junitPlatformTestKit)
    junitIntegrationTestImplementation(libs.junitPlatformLauncher)
    javaExampleTestImplementation(libs.junitPlatformTestKit)
    javaExampleTestImplementation(libs.junitPlatformLauncher)
    kotlinExampleTestImplementation(libs.junitPlatformTestKit)
    kotlinExampleTestImplementation(libs.junitPlatformLauncher)
}

node {
    version = libs.versions.node.get()
    download = true
}

tasks {
    javadoc {
        options {
            this as StandardJavadocDocletOptions
            addBooleanOption("Xdoclint:none", true)
            addStringOption("Xmaxwarns", "1")
        }
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>()["main"].allSource)
        dependsOn(classes)
    }

    register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        javadoc.get().destinationDir
        dependsOn(javadoc)
    }

    withType<KotlinCompile> {
        dependsOn("generateGrammarSource")
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += listOf("-Xjvm-default=all", "-opt-in=kotlin.contracts.ExperimentalContracts")
        }
    }

    java {
        sourceCompatibility = VERSION_17
        targetCompatibility = VERSION_17
    }

    withType<AntlrTask> {
        outputDirectory = file("$outputDirectory/dev/kensa/parse")
        arguments = arguments + listOf("-listener", "-no-visitor", "-package", "dev.kensa.parse")
    }

    register<Test>("junitIntegrationTest") {
        useJUnitPlatform {
            exclude("dev/kensa/example/**")
        }
        description = "Runs JUnit integration tests."
        group = "verification"

        testClassesDirs = sourceSets["junitIntegrationTest"].output.classesDirs
        classpath = sourceSets["junitIntegrationTest"].runtimeClasspath
        shouldRunAfter("test")
    }


    register<Test>("kotlinExampleTest") {
        useJUnitPlatform {
            exclude("dev/kensa/example/**")
        }
        description = "Runs Kotlin example tests."
        group = "verification"

        testClassesDirs = sourceSets["kotlinExampleTest"].output.classesDirs
        classpath = sourceSets["kotlinExampleTest"].runtimeClasspath
        shouldRunAfter("junitIntegrationTest")
    }

    register<Test>("javaExampleTest") {
        useJUnitPlatform {
            exclude("dev/kensa/example/**")
        }
        description = "Runs Java example tests."
        group = "verification"

        testClassesDirs = sourceSets["javaExampleTest"].output.classesDirs
        classpath = sourceSets["javaExampleTest"].runtimeClasspath
        shouldRunAfter("junitIntegrationTest")
    }

    check { dependsOn("junitIntegrationTest", "kotlinExampleTest", "javaExampleTest") }

    withType<Test> {
        useJUnitPlatform {
            exclude("dev/kensa/example/**")
        }
    }

    register<NodeTask>("webpack") {
        script = project.file("node_modules/.bin/webpack")
        inputs.file("webpack.config.js")
        inputs.file("package-lock.json")
        inputs.dir("src/ui")
        outputs.dir("$buildDir/resources/main")
        dependsOn("npmInstall")
    }

    register<NodeTask>("startUiDevServer") {
        script = project.file("node_modules/.bin/webpack-dev-server")
        args = listOf("--mode", "development")
        inputs.file("webpack.config.js")
        inputs.file("package-lock.json")
        inputs.dir("src/ui")
        outputs.dir("$buildDir/resources/main")
        dependsOn("npmInstall")
    }

    processResources {
        dependsOn("webpack")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "kensa"
            pom.withXml {
                asNode().appendNode("name", "kensa")
                asNode().appendNode("description", description)
                asNode().appendNode("url", "https://kensa.dev")
                asNode().appendNode("developers")
                    .appendNode("developer").appendNode("name", "Paul Brooks").parent()
                    .appendNode("email", "paul@kensa.dev")
                asNode().appendNode("scm")
                    .appendNode("url", "git@github.com:kensa-dev/kensa.git").parent()
                    .appendNode("connection", "scm:git:git@github.com:kensa-dev/kensa.git").parent()
                    .appendNode("developerConnection", "scm:git:git@github.com:kensa-dev/kensa.git")
                asNode().appendNode("licenses").appendNode("license")
                    .appendNode("name", "Apache License, Version 2.0").parent()
                    .appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.html")
            }
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}

if (project.findProperty("sign") == "true") {
    signing {
        val signingKeyId: String? by project
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}