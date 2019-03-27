import com.moowork.gradle.node.task.NodeTask

group = "dev.kensa"
version = "DEV-SNAPSHOT"

buildscript {
    repositories {
        mavenCentral()
    }
}

plugins {
    `java-library`
    `maven-publish`
    id("com.moowork.node") version "1.3.1"
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

tasks.register<Jar>("testsJar") {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().allJava, sourceSets.test.get().resources)
}

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allJava, sourceSets.main.get().resources)
}

node {
    version = "10.6.0"
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
    gradleVersion = "5.3"
}

dependencies {
    implementation("com.github.javaparser:javaparser-core:3.6.22")
    implementation("net.sourceforge.plantuml:plantuml:8059")
    implementation("io.pebbletemplates:pebble:3.0.8")

    testCompile("org.mockito:mockito-core:2.24.0")
    testCompile("org.junit.platform:junit-platform-testkit:1.4.0")
    api("org.junit.jupiter:junit-jupiter-engine:5.4.1")
    api("org.junit.jupiter:junit-jupiter-params:5.4.1")
    api("org.junit.jupiter:junit-jupiter-api:5.4.1")
    api("org.junit.platform:junit-platform-launcher:1.3.2")
    api("org.assertj:assertj-core:3.11.1")
    api("org.hamcrest:hamcrest-core:1.3")
    api("com.eclipsesource.minimal-json:minimal-json:0.9.5")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
//
//            artifact(tasks["sourcesJar"])
//            artifact(tasks["testsJar"])
        }
    }
}
