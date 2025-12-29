import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.nexusPublish)
    signing
    `maven-publish`
}

group = "dev.kensa"
version = project.properties["releaseVersion"] ?: "SNAPSHOT"

subprojects {
    group = "dev.kensa"
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    if (name != "ui" && name != "bom" && name != "antlr") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        var javaVersion = if(name == "adoptabot") VERSION_21 else VERSION_17
        var kotlinJvmTarget = if(name == "adoptabot") JVM_21 else JVM_17

        plugins.withId("org.jetbrains.kotlin.jvm") {
            configure<JavaPluginExtension> {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }
        }

        tasks {
            withType<KotlinCompile> {
                compilerOptions {
                    jvmTarget.set(kotlinJvmTarget)
                    freeCompilerArgs.addAll(listOf("-Xjvm-default=all", "-opt-in=kotlin.contracts.ExperimentalContracts", "-Xnon-local-break-continue"))
                }
            }

            withType<Test> {
                useJUnitPlatform()

                systemProperty("junit.jupiter.execution.parallel.enabled", "true")
                systemProperty("junit.jupiter.execution.parallel.config.strategy", "dynamic")

                jvmArgumentProviders.add(CommandLineArgumentProvider { listOf("-Djava.awt.headless=true") })
            }

            if (names.contains("javadoc")) {
                val javadoc = named<Javadoc>("javadoc") {
                    options {
                        this as StandardJavadocDocletOptions
                        addBooleanOption("Xdoclint:none", true)
                        addStringOption("Xmaxwarns", "1")
                    }
                }

                register<Jar>("javadocJar") {
                    archiveClassifier.set("javadoc")
                    javadoc.get().destinationDir
                    dependsOn(javadoc)
                }
            }

            register<Jar>("sourcesJar") {
                group = "build"
                archiveClassifier.set("sources")
                from(project.the<SourceSetContainer>()["main"].allSource)
                dependsOn(named("classes"))
            }

            withType<Jar> {
                archiveBaseName.set("${rootProject.name}-${project.name}")
            }
        }

        publishing {
            publications {
                create<MavenPublication>("mavenJava") {
                    artifactId = "${rootProject.name}-${project.name}"
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
                    suppressAllPomMetadataWarnings()
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
    }
}

nexusPublishing {
    val nexusUsername: String? by project
    val nexusPassword: String? by project

    repositories {
        sonatype {
            username = nexusUsername
            password = nexusPassword
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}
