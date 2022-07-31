import com.moowork.gradle.node.task.NodeTask
import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "dev.kensa"
version = project.properties["releaseVersion"] ?: "SNAPSHOT"

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.nodeGradle)
    alias(libs.plugins.nexusStaging)
    antlr
    signing
    `maven-publish`
}

nexusStaging {
    serverUrl = "https://s01.oss.sonatype.org/service/local/"
}

repositories {
    mavenCentral()
}

dependencies {
    antlr(libs.antlr)

    api(libs.kotlinStdLib)
    api(libs.kotlinReflect)

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

    testImplementation(libs.junitPlatformLauncher)
    testImplementation(libs.junitPlatformTestKit)
    testImplementation(libs.mockitoKotlin)
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
            jvmTarget = "11"
            freeCompilerArgs += listOf("-Xjvm-default=all", "-opt-in=kotlin.contracts.ExperimentalContracts")
        }
    }

    java {
        sourceCompatibility = VERSION_11
        targetCompatibility = VERSION_11
    }

    withType(AntlrTask::class) {
        outputDirectory = file("$outputDirectory/dev/kensa/parse")
        arguments = arguments + listOf("-listener", "-no-visitor", "-package", "dev.kensa.parse")
    }

    withType(Test::class) {
        useJUnitPlatform {
            exclude("dev/kensa/acceptance/example/**")
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
        setArgs(listOf("--mode", "development"))
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
    val nexusUsername: String? by project
    val nexusPassword: String? by project

    repositories {
        maven {
            name = "SonatypeStaging"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
        maven {
            name = "SonatypeSnapshot"
            setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            credentials {
                username = nexusUsername
                password = nexusPassword
            }
        }
    }
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