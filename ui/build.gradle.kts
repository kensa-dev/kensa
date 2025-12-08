import com.github.gradle.node.task.NodeTask
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.tasks

plugins {
    alias(libs.plugins.nodeGradle)
}

node {
    version = libs.versions.node.get()
    download = true
    npmInstallCommand = "ci"
}

tasks {
    register<NodeTask>("viteBuild") {
        script.set(project.file("node_modules/.bin/vite"))
        args = listOf("build")

        inputs.file("vite.config.js")
        inputs.file("package-lock.json")
        inputs.dir("src")
        outputs.dir("${layout.buildDirectory.get()}/js")
        dependsOn("npmInstall")
    }

    register<NodeTask>("viteDev") {
        script.set(project.file("node_modules/.bin/vite"))
        args = listOf("dev")

        inputs.file("vite.config.js")
        inputs.file("package-lock.json")
        inputs.dir("src")

        dependsOn("npmInstall")
    }
}
