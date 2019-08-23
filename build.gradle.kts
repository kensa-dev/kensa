import com.moowork.gradle.node.task.NodeTask

group = "dev.kensa"
version = System.getenv("CI_PIPELINE_IID") ?: "DEV-SNAPSHOT"

plugins {
    `kensa-module`
    id("com.moowork.node") version Versions.moowork
}

node {
    version = Versions.node
    download = true
}

tasks {
    withType(Test::class) {
        useJUnitPlatform {
            exclude("dev/kensa/acceptance/example/**")
        }
    }

    register<NodeTask>("webpack") {
        setScript(project.file("node_modules/.bin/webpack"))
        inputs.file("webpack.config.js")
        inputs.file("package-lock.json")
        inputs.dir("src/ui")
        outputs.dir("$buildDir/resources/main")
        dependsOn("npmInstall")
    }

    register<NodeTask>("startUiDevServer") {
        setScript(project.file("node_modules/.bin/webpack-dev-server"))
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

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = Versions.gradleWrapper
    }
}

dependencies {
    api("org.junit.jupiter:junit-jupiter-params:${Versions.junitJupiter}")
    api("org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}")
    api("org.assertj:assertj-core:${Versions.assertJ}")
    api("org.hamcrest:hamcrest-core:${Versions.hamcrest}")

    implementation("org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}")
    implementation("com.eclipsesource.minimal-json:minimal-json:${Versions.minimalJson}")
    implementation("com.github.javaparser:javaparser-core:${Versions.javaParser}")
    implementation("net.sourceforge.plantuml:plantuml:${Versions.plantUml}")
    implementation("io.pebbletemplates:pebble:${Versions.pebble}")

    testCompile("org.junit.platform:junit-platform-launcher:${Versions.junitPlatform}")
    testCompile("org.junit.platform:junit-platform-testkit:${Versions.junitPlatform}")
    testCompile("org.mockito:mockito-core:${Versions.mockito}")
}