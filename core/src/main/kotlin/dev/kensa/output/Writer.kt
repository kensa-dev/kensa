package dev.kensa.output

import com.eclipsesource.json.Json
import dev.kensa.Configuration
import dev.kensa.UiMode
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms.toIndexJson
import dev.kensa.output.json.JsonTransforms.toJsonString
import dev.kensa.output.json.JsonTransforms.toJsonWith
import dev.kensa.output.template.FileTemplate.IndexFileTemplate
import dev.kensa.output.template.FileTemplate.TestFileTemplate
import dev.kensa.util.IoUtil
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class ResultWriter(private val configuration: Configuration) {

    init {
        IoUtil.recreate(configuration.outputDir)
    }

    fun write(containers: List<TestContainer>) {
        when (configuration.uiMode) {
            UiMode.Legacy -> writeLegacy(containers)
            UiMode.Modern -> writeModern(containers)
        }
    }

    fun writeLegacy(containers: List<TestContainer>) {
        writeLegacyIndices(containers.sortedBy { it.testClass.name })
        IoUtil.copyResource("/kensa.js", configuration.outputDir)
        IoUtil.copyResource("/favicon.ico", configuration.outputDir)

        println(
            """
                Kensa Output :
                ${configuration.outputDir.resolve("index.html")}
            """.trimIndent()
        )
    }

    fun writeModern(containers: List<TestContainer>) {
        writeModernIndices(containers.sortedBy { it.testClass.name })
//        IoUtil.copyResource("/kensa.js", configuration.outputDir)
//        IoUtil.copyResource("/favicon.ico", configuration.outputDir)

        println(
            """
                Kensa Output :
                ${configuration.outputDir.resolve("index.html")}
            """.trimIndent()
        )
    }

    fun writeTest(container: TestContainer) {
        when (configuration.uiMode) {
            UiMode.Legacy -> writeLegacyTest(container)
            UiMode.Modern -> writeModernTest(container)
        }
    }

    fun writeLegacyTest(container: TestContainer) {
        TestFileTemplate(configuration, container).write()
    }

    fun writeModernTest(container: TestContainer) {
        with(configuration) {
            val json = toJsonWith(renderers)(container)
            val string = toJsonString()(json)

            val resultsPath: Path = outputDir.resolve("results")
            resultsPath.createDirectories()
            resultsPath.resolve("${container.testClass.name}.json").writeText(string)
        }
    }

    private fun writeModernIndices(containers: List<TestContainer>) {
        with(configuration) {
            val json = Json.array().apply {
                containers.forEach { add(toIndexJson(it.testClass.name)(it)) }
            }
            val string = toJsonString()(json)

            val indicesPath: Path = outputDir.resolve("indices.json")
            indicesPath.writeText(string)
        }
    }

    private fun writeLegacyIndices(containers: List<TestContainer>) {
        IndexFileTemplate(configuration).apply {
            containers.forEach { addIndex(it) }
            write()
        }
    }
}