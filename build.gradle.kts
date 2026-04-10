import org.gradle.api.JavaVersion.VERSION_17
import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jreleaser.model.Active

plugins {
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.jreleaser)
    `maven-publish`
    base
}

group = "dev.kensa"
version = project.properties["releaseVersion"] ?: "SNAPSHOT"

subprojects {
    group = "dev.kensa"
    version = rootProject.version

    repositories {
        mavenCentral()
    }

    if (name != "ui" && name != "ui2" && name != "bom" && name != "antlr") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")

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
                    freeCompilerArgs.addAll(listOf("-opt-in=kotlin.contracts.ExperimentalContracts", "-Xexplicit-backing-fields"))
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
            repositories {
                maven {
                    url = uri(rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.absolutePath)
                }
            }
        }
    }
}

jreleaser {
    gitRootSearch.set(true)
    signing {
        active.set(Active.RELEASE)
        armored.set(true)
    }
    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active.set(Active.RELEASE)
                url.set("https://central.sonatype.com/api/v1/publisher")
                stagingRepositories.add(layout.buildDirectory.dir("staging-deploy").get().asFile.absolutePath)
            }
            nexus2.create("snapshots") {
                active.set(Active.SNAPSHOT)
                snapshotUrl.set("https://central.sonatype.com/repository/maven-snapshots/")
                applyMavenCentralRules.set(true)
                snapshotSupported.set(true)
                closeRepository.set(true)
                releaseRepository.set(true)
                stagingRepositories.add(layout.buildDirectory.dir("staging-deploy").get().asFile.absolutePath)
            }
        }
    }
}
