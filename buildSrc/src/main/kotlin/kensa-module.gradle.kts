plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

configurations {
    create("tests")
    create("sources")
}

tasks {
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
//        systemProperty("junit.jupiter.execution.parallel.enabled", true)
//        systemProperty("junit.jupiter.execution.parallel.config.fixed.parallelism", 10)
//        systemProperty("junit.jupiter.testinstance.lifecycle.default", "per_class")
//        systemProperty("junit.jupiter.execution.parallel.mode.default", "concurrent")
    }

    withTypeIfPresent<JavaCompile>("compileJava") {
        options.compilerArgs.addAll(listOf("--release", Versions.mainJavaVersion.majorVersion))
    }

    withTypeIfPresent<JavaCompile>("compileTestJava") {
        sourceCompatibility = Versions.testJavaVersion.majorVersion
        targetCompatibility = Versions.testJavaVersion.majorVersion
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

            artifact(tasks["sourcesJar"])
            artifact(tasks["testsJar"])
        }
    }
}