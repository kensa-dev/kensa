import com.moowork.gradle.node.task.NodeTask

group = "dev.kensa"
version = System.getenv("CI_PIPELINE_IID") ?: "DEV-SNAPSHOT"

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    `java-library`
    `maven-publish`
    id("com.moowork.node") version Versions.moowork
}

repositories {
    mavenCentral()
}

val mainJavaVersion by extra(JavaVersion.VERSION_1_8)
val testJavaVersion by extra(JavaVersion.VERSION_11)

afterEvaluate {
    tasks {
        compileJava {
            options.compilerArgs.addAll(
                    listOf("--release", mainJavaVersion.majorVersion)
            )
        }
        compileTestJava {
            options.encoding = "UTF-8"
            sourceCompatibility = testJavaVersion.majorVersion
            targetCompatibility = testJavaVersion.majorVersion

            options.compilerArgs.addAll(listOf(
                    "-Xlint", // Enables all recommended warnings.
                    "-Xlint:-overrides", // Disables "method overrides" warnings.
                    "-parameters" // Generates metadata for reflection on method parameters.
            ))
        }
    }
}

tasks.test {
    useJUnitPlatform {
        exclude("dev/kensa/acceptance/example/**")
    }
}

val sourcesJar by tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

node {
    version = Versions.node
    download = true
}

tasks.register<NodeTask>("webpack") {
    setScript(project.file("node_modules/.bin/webpack"))
    inputs.file("webpack.config.js")
    inputs.file("package-lock.json")
    inputs.dir("src/ui")
    outputs.dir("$buildDir/resources/main")
    dependsOn("npmInstall")
}

tasks.processResources {
    dependsOn("webpack")
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = Versions.gradleWrapper
}

dependencies {
    api("org.junit.platform:junit-platform-launcher:${Versions.junitPlatform}")
    api("org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}")
    api("org.junit.jupiter:junit-jupiter-params:${Versions.junitJupiter}")
    api("org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}")
    api("org.assertj:assertj-core:${Versions.assertJ}")
    api("org.hamcrest:hamcrest-core:${Versions.hamcrest}")
    api("com.eclipsesource.minimal-json:minimal-json:${Versions.minimalJson}")

    implementation("com.github.javaparser:javaparser-core:${Versions.javaParser}")
    implementation("net.sourceforge.plantuml:plantuml:${Versions.plantUml}")
    implementation("io.pebbletemplates:pebble:${Versions.pebble}")

    testCompile("org.mockito:mockito-core:${Versions.mockito}")
    testCompile("org.junit.platform:junit-platform-testkit:${Versions.junitPlatform}")
}

publishing {
    if (version != "DEV-SNAPSHOT") {
        repositories {
            maven {
                url = uri(System.getenv("KENSA_PUBLISH_REPO_URI"))
                credentials {
                    username = System.getenv("KENSA_PUBLISH_REPO_USERNAME")
                    password = System.getenv("KENSA_PUBLISH_REPO_PASSWORD")
                }
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifact(sourcesJar)
        }
    }
}
