import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm) apply false
//    alias(libs.plugins.nexusPublish)
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

    if (name != "ui") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")

        plugins.withId("org.jetbrains.kotlin.jvm") {
            configure<JavaPluginExtension> {
                sourceCompatibility = VERSION_17
                targetCompatibility = VERSION_17
            }
        }

        tasks {
            withType<KotlinCompile> {
                compilerOptions {
                    jvmTarget.set(JVM_17)
                    freeCompilerArgs.addAll(listOf("-Xjvm-default=all", "-opt-in=kotlin.contracts.ExperimentalContracts"))
                }
            }

            withType<Test> {
                useJUnitPlatform()

                systemProperty("junit.jupiter.execution.parallel.enabled", "true")
                systemProperty("junit.jupiter.execution.parallel.config.strategy", "dynamic")
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
//                    artifact(tasks["javadocJar"])
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


//fun createSourceSet(name: String) {
//    sourceSets {
//        create(name) {
//            compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
//            runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
//        }
//    }
//}
//
//createSourceSet("junitIntegrationTest")
//val junitIntegrationTestImplementation: Configuration by configurations.getting { extendsFrom(configurations.implementation.get()) }
//
//createSourceSet("javaExampleTest")
//val javaExampleTestImplementation: Configuration by configurations.getting { extendsFrom(configurations.implementation.get()) }
//
//createSourceSet("kotlinExampleTest")
//val kotlinExampleTestImplementation: Configuration by configurations.getting { extendsFrom(configurations.implementation.get()) }
//
//nexusPublishing {
//    val nexusUsername: String? by project
//    val nexusPassword: String? by project
//
//    repositories {
//        sonatype {
//            username = nexusUsername
//            password = nexusPassword
//            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
//            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
//        }
//    }
//}

//dependencies {
//    api(libs.kotlinStdLib)
//    api(libs.kotlinReflect)
//
//    implementation(libs.kotlinCoroutines)
//    implementation(libs.junitJupiterParams)
//    implementation(libs.junitJupiterApi)
//    implementation(libs.junitJupiterEngine)
//    implementation(libs.junitPlatformLauncher)
//    implementation(libs.assertJCore)
//    implementation(libs.hamcrestCore)
//    implementation(libs.awaitilityKotlin)
//    implementation(libs.kotestAssertionsCoreJvm)
//
//    testImplementation(libs.mockitoKotlin)
//    testImplementation(libs.kotlinCoroutinesTest)
//
//    junitIntegrationTestImplementation(libs.junitPlatformTestKit)
//    junitIntegrationTestImplementation(libs.junitPlatformLauncher)
//    javaExampleTestImplementation(libs.junitPlatformTestKit)
//    javaExampleTestImplementation(libs.junitPlatformLauncher)
//    kotlinExampleTestImplementation(libs.junitPlatformTestKit)
//    kotlinExampleTestImplementation(libs.junitPlatformLauncher)
//}

//tasks {
//    javadoc {
//        options {
//            this as StandardJavadocDocletOptions
//            addBooleanOption("Xdoclint:none", true)
//            addStringOption("Xmaxwarns", "1")
//        }
//    }
//
//    register<Jar>("sourcesJar") {
//        archiveClassifier.set("sources")
//        from(project.the<SourceSetContainer>()["main"].allSource)
//        dependsOn(classes)
//    }
//
//    register<Jar>("javadocJar") {
//        archiveClassifier.set("javadoc")
//        javadoc.get().destinationDir
//        dependsOn(javadoc)
//    }
//
//
//    register<Test>("junitIntegrationTest") {
//        useJUnitPlatform {
//            exclude("dev/kensa/example/**")
//        }
//        description = "Runs JUnit integration tests."
//        group = "verification"
//
//        testClassesDirs = sourceSets["junitIntegrationTest"].output.classesDirs
//        classpath = sourceSets["junitIntegrationTest"].runtimeClasspath
//        shouldRunAfter("test")
//    }
//
//
//    register<Test>("kotlinExampleTest") {
//        useJUnitPlatform {
//            exclude("dev/kensa/example/**")
//        }
//        description = "Runs Kotlin example tests."
//        group = "verification"
//
//        testClassesDirs = sourceSets["kotlinExampleTest"].output.classesDirs
//        classpath = sourceSets["kotlinExampleTest"].runtimeClasspath
//        shouldRunAfter("junitIntegrationTest")
//    }
//
//    register<Test>("javaExampleTest") {
//        useJUnitPlatform {
//            exclude("dev/kensa/example/**")
//        }
//        description = "Runs Java example tests."
//        group = "verification"
//
//        testClassesDirs = sourceSets["javaExampleTest"].output.classesDirs
//        classpath = sourceSets["javaExampleTest"].runtimeClasspath
//        shouldRunAfter("junitIntegrationTest")
//    }
//
//    check { dependsOn("junitIntegrationTest", "kotlinExampleTest", "javaExampleTest") }
//
//    withType<Test> {
//        useJUnitPlatform {
//            exclude("dev/kensa/example/**")
//        }
//    }
//}

//publishing {
//    publications {
//        create<MavenPublication>("mavenJava") {
//            artifactId = "kensa"
//            pom.withXml {
//                asNode().appendNode("name", "kensa")
//                asNode().appendNode("description", description)
//                asNode().appendNode("url", "https://kensa.dev")
//                asNode().appendNode("developers")
//                    .appendNode("developer").appendNode("name", "Paul Brooks").parent()
//                    .appendNode("email", "paul@kensa.dev")
//                asNode().appendNode("scm")
//                    .appendNode("url", "git@github.com:kensa-dev/kensa.git").parent()
//                    .appendNode("connection", "scm:git:git@github.com:kensa-dev/kensa.git").parent()
//                    .appendNode("developerConnection", "scm:git:git@github.com:kensa-dev/kensa.git")
//                asNode().appendNode("licenses").appendNode("license")
//                    .appendNode("name", "Apache License, Version 2.0").parent()
//                    .appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.html")
//            }
//            from(components["java"])
//
//            artifact(tasks["sourcesJar"])
//            artifact(tasks["javadocJar"])
//        }
//    }
//}

//if (project.findProperty("sign") == "true") {
//    signing {
//        val signingKeyId: String? by project
//        val signingKey: String? by project
//        val signingPassword: String? by project
//        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
//        sign(publishing.publications["mavenJava"])
//    }
//}