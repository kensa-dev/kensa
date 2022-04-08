import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
    antlr
}

repositories {
    mavenCentral()
}

configurations {
    create("tests")
    create("sources")
}

tasks {
    withType(AntlrTask::class) {
        outputDirectory = file("$outputDirectory/dev/kensa/parse")
        arguments = arguments + listOf("-listener", "-no-visitor", "-package", "dev.kensa.parse")
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    register<Jar>("testsJar") {
        archiveClassifier.set("tests")
        from(sourceSets["test"].output)
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withTypeIfPresent<JavaCompile>("compileJava") {
        sourceCompatibility = Versions.testJavaVersion.majorVersion
        targetCompatibility = Versions.testJavaVersion.majorVersion
    }

    withTypeIfPresent<JavaCompile>("compileTestJava") {
        sourceCompatibility = Versions.testJavaVersion.majorVersion
        targetCompatibility = Versions.testJavaVersion.majorVersion
    }

    withTypeIfPresent<KotlinCompile>("compileKotlin") {
        dependsOn("generateGrammarSource")
        kotlinOptions {
            jvmTarget = "11"
            freeCompilerArgs += listOf("-Xjvm-default=all-compatibility", "-opt-in=kotlin.contracts.ExperimentalContracts", "-opt-in=io.kotest.common.ExperimentalKotest")
        }
    }

    withTypeIfPresent<KotlinCompile>("compileTestKotlin") {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}

artifacts {
    add("tests", tasks["testsJar"])
    add("sources", tasks["sourcesJar"])
}

publishing {
    if (version != "DEV-SNAPSHOT") {
        repositories {
            maven {
                url = uri(System.getenv("KENSA_PUBLISH_REPO_URI") ?: "KENSA_PUBLISH_REPO_URI not set")
                credentials {
                    username = System.getenv("KENSA_PUBLISH_REPO_USERNAME")
                    password = System.getenv("KENSA_PUBLISH_REPO_PASSWORD")
                }
            }
        }
    }

    publications {
        register<MavenPublication>("mavenJava") {
            from(components["java"])
            suppressPomMetadataWarningsFor("apiElements")

            artifact(tasks["sourcesJar"])
            artifact(tasks["testsJar"])
        }
    }
}
