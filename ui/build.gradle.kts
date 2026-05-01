import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.task.NodeTask

plugins {
    alias(libs.plugins.nodeGradle)
}

node {
    version = libs.versions.node.get()
    download = true
}

tasks {
    register<NpmTask>("viteTest") {
        args.set(listOf("run", "test"))

        inputs.file("vitest.config.ts")
        inputs.file("package-lock.json")
        inputs.dir("src")
        dependsOn("npmInstall")
    }

    register<NodeTask>("viteBuild") {
        script.set(project.file("node_modules/.bin/vite"))
        args = listOf("build")

        inputs.file("vite.config.ts")
        inputs.file("package-lock.json")
        inputs.dir("src")
        outputs.dir("${layout.buildDirectory.get()}/js")
        dependsOn("npmInstall", "viteTest")
    }

    register<NodeTask>("viteDev") {
        script.set(project.file("node_modules/.bin/vite"))
        args = listOf("dev")

        inputs.file("vite.config.ts")
        inputs.file("package-lock.json")
        inputs.dir("src")

        dependsOn("npmInstall")
    }
}
