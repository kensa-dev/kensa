package dev.kensa.output

import com.eclipsesource.json.Json
import dev.kensa.Configuration
import dev.kensa.Section.Buttons
import dev.kensa.Section.Tabs
import dev.kensa.UiMode
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms.toJsonString
import dev.kensa.output.json.JsonTransforms.toJsonWith
import dev.kensa.output.json.JsonTransforms.toModernIndexJson
import dev.kensa.tabs.TabArtifactManager
import dev.kensa.output.template.FileTemplate.IndexFileTemplate
import dev.kensa.output.template.FileTemplate.TestFileTemplate
import dev.kensa.sentence.Acronym
import dev.kensa.util.IoUtil
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import com.eclipsesource.json.Json.`object` as jsonObject

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
        writeModernConfiguration()
        writeModernHtml()
        IoUtil.copyResource("/kensa2.js", configuration.outputDir)
        IoUtil.copyResource("/logo.svg", configuration.outputDir)

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

    private fun writeModernConfiguration() =
        with(configuration) {
            val json = jsonObject()
                .add("autoOpenTab", autoOpenTab.name)
                .add("autoExpandNotes", autoExpandNotes)
                .add("titleText", titleText)
                .add("issueTrackerUrl", issueTrackerUrl.toString())
                .add("acronyms", acronymsAsJson(dictionary.acronyms))
                .add("flattenPackages", flattenOutputPackages)
                .add("packageDisplayMode", packageDisplay.name)
                .add("sectionOrder", Json.array().apply {
                    sectionOrder.forEach {
                        add((if (it == Buttons) Tabs else it).name)
                    }
                })

            outputDir.resolve("configuration.json").writeText(json.toString())
        }

    private fun acronymsAsJson(acronyms: Collection<Acronym>) = jsonObject().apply {
        acronyms.forEach {
            add(it.acronym, it.meaning)
        }
    }

    private fun writeModernHtml() {
        val html = """
            <!doctype html>
            <html lang="en">
            <head>
                <title>${configuration.titleText}</title>
                <meta charset="UTF-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                <link rel="icon" type="image/svg+xml" href="logo.svg" />
                <title>${configuration.titleText}</title>
            </head>
            <body>
                <div id="root"></div>
                <script src="kensa2.js"></script>
            </body>
            </html>
        """.trimIndent()
        configuration.outputDir.resolve("index.html").writeText(html)
    }

    fun writeModernTest(container: TestContainer) {
        with(configuration) {
            val tabArtifacts = TabArtifactManager().generate(container, outputDir, configuration)

            val json = toJsonWith(renderers) { methodContainer, _, invocationIndex ->
                tabArtifacts[TabArtifactManager.InvocationKey(methodContainer.method.name, invocationIndex)] ?: emptyList()
            }(container)

            val string = toJsonString()(json)

            val resultsPath: Path = outputDir.resolve("results")
            resultsPath.createDirectories()
            resultsPath.resolve("${container.testClass.name}.json").writeText(string)
        }
    }

    private fun writeModernIndices(containers: List<TestContainer>) {
        with(configuration) {
            val json = jsonObject()
                .add(
                    "indices",
                    Json.array().apply {
                        containers.forEach { add(toModernIndexJson(it.testClass.name)(it)) }
                    }
                )
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