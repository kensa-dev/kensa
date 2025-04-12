import com.github.gradle.node.task.NodeTask
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.tasks

plugins {
    alias(libs.plugins.nodeGradle)
}

node {
    version = libs.versions.node.get()
    download = true
}

tasks {
    register<NodeTask>("webpack") {
        script = project.file("node_modules/.bin/webpack")
        inputs.file("webpack.config.js")
        inputs.file("package-lock.json")
        outputs.dir("${layout.buildDirectory.get()}/js")
        dependsOn("npmInstall")
    }

    register<NodeTask>("startUiDevServer") {
        script = project.file("node_modules/.bin/webpack-dev-server")
        args = listOf("--mode", "development")
        inputs.file("webpack.config.js")
        inputs.file("package-lock.json")
        inputs.dir("src/ui")
        outputs.dir("${layout.buildDirectory.get()}/js")
        dependsOn("npmInstall")
    }
}
