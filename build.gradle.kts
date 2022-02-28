import com.moowork.gradle.node.task.NodeTask

group = "dev.kensa"
version = System.getenv("CI_PIPELINE_IID") ?: "DEV-SNAPSHOT"

plugins {
    `kensa-module`
    id("com.github.node-gradle.node") version Versions.moowork
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
}

dependencies {
    antlr("org.antlr:antlr4:${Versions.antlr}")

    api(kotlin("stdlib-jdk8", version = Versions.kotlin))
    api(kotlin("reflect", version = Versions.kotlin))

    implementation("io.kotest:kotest-assertions-core:${Versions.kotest}")
    implementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
    implementation("org.junit.jupiter:junit-jupiter-params:${Versions.junitJupiter}")
    implementation("org.junit.jupiter:junit-jupiter-api:${Versions.junitJupiter}")
    implementation("org.junit.jupiter:junit-jupiter-engine:${Versions.junitJupiter}")
    implementation("org.assertj:assertj-core:${Versions.assertJ}")
    implementation("org.hamcrest:hamcrest-core:${Versions.hamcrest}")
    implementation("org.awaitility:awaitility-kotlin:${Versions.awaitility}")
    implementation("com.eclipsesource.minimal-json:minimal-json:${Versions.minimalJson}")
    implementation("net.sourceforge.plantuml:plantuml:${Versions.plantUml}")
    implementation("io.pebbletemplates:pebble:${Versions.pebble}")

    testImplementation("org.junit.platform:junit-platform-launcher:${Versions.junitPlatform}")
    testImplementation("org.junit.platform:junit-platform-testkit:${Versions.junitPlatform}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockito}")
}