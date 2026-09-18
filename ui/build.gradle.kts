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
        inputs.file("index.html")
        inputs.file("../core/src/main/kotlin/dev/kensa/output/ResultWriter.kt")
        inputs.file("../cli/internal/shell/embed/index.html")
        dependsOn("npmInstall")
    }

    // vite transpiles through esbuild, which strips types without checking them, so nothing else in the
    // build would catch a type error.
    register<NodeTask>("typeCheck") {
        script.set(project.file("node_modules/.bin/tsc"))
        args = listOf("--noEmit")

        inputs.file("tsconfig.json")
        inputs.file("tsconfig.node.json")
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
        dependsOn("npmInstall", "typeCheck", "viteTest")
    }

    register<NodeTask>("viteEmbedBuild") {
        script.set(project.file("node_modules/.bin/vite"))
        args = listOf("build", "--config", "vite.embed.config.ts")

        inputs.file("vite.embed.config.ts")
        inputs.dir("src/embed")
        inputs.dir("src/util")
        outputs.file("${layout.buildDirectory.get()}/js/kensa-embed.js")
        dependsOn("viteBuild")
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
