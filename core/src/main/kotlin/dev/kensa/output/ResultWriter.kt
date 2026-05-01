package dev.kensa.output

import com.eclipsesource.json.Json
import dev.kensa.Configuration
import dev.kensa.Section.Tabs
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms.toIndexJson
import dev.kensa.output.json.JsonTransforms.toJsonString
import dev.kensa.output.json.JsonTransforms.toJsonWith
import dev.kensa.sentence.Acronym
import dev.kensa.tabs.TabArtifactManager
import dev.kensa.util.IoUtil
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import com.eclipsesource.json.Json.`object` as jsonObject

class ResultWriter(private val configuration: Configuration) {

    init {
        IoUtil.recreate(configuration.outputDir)
    }

    private val tabArtifactManager = TabArtifactManager()

    fun write(containers: List<TestContainer>) {
        writeIndices(containers.sortedBy { it.testClass.name })
        writeConfiguration()
        writeHtml()
        IoUtil.copyResource("/kensa.js", configuration.outputDir)
        IoUtil.copyResource("/logo.svg", configuration.outputDir)

        println(
            """
                Kensa Output :
                ${configuration.outputDir.resolve("index.html")}
            """.trimIndent()
        )
    }

    fun writeTest(container: TestContainer) {
        with(configuration) {
            val tabArtifacts = tabArtifactManager.generate(container, outputDir, configuration)

            val json = toJsonWith(renderers) { methodContainer, _, invocationIndex ->
                tabArtifacts[TabArtifactManager.InvocationKey(methodContainer.method.name, invocationIndex)] ?: emptyList()
            }(container)

            val string = toJsonString()(json)

            val resultsPath: Path = outputDir.resolve("results")
            resultsPath.createDirectories()
            resultsPath.resolve("${container.testClass.name}.json").writeText(string)
        }
    }

    private fun writeConfiguration() =
        with(configuration) {
            val kensaVersion = ResultWriter::class.java.`package`?.implementationVersion ?: "dev"
            val json = jsonObject()
                .add("autoOpenTab", autoOpenTab.name)
                .add("autoExpandNotes", autoExpandNotes)
                .add("titleText", titleText)
                .add("issueTrackerUrl", issueTrackerUrl.toString())
                .add("acronyms", acronymsAsJson(dictionary.acronyms))
                .add("flattenPackages", flattenOutputPackages)
                .add("packageDisplay", packageDisplay.name)
                .add("packageDisplayRoot", packageDisplayRoot)
                .add("kensaVersion", kensaVersion)
                .add("generatedAt", Instant.now().toString())
                .add("sectionOrder", Json.array().apply {
                    sectionOrder.forEach {
                        add((if (it == Tabs) Tabs else it).name)
                    }
                })

            outputDir.resolve("configuration.json").writeText(json.toString())
        }

    private fun acronymsAsJson(acronyms: Collection<Acronym>) = jsonObject().apply {
        acronyms.forEach {
            add(it.acronym, it.meaning)
        }
    }

    private fun writeHtml() {
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
                <script src="kensa.js"></script>
            </body>
            </html>
        """.trimIndent()
        configuration.outputDir.resolve("index.html").writeText(html)
    }

    private fun writeIndices(containers: List<TestContainer>) {
        with(configuration) {
            val json = jsonObject()
                .add(
                    "indices",
                    Json.array().apply {
                        containers.forEach { add(toIndexJson(it.testClass.name)(it)) }
                    }
                )
            val string = toJsonString()(json)

            val indicesPath: Path = outputDir.resolve("indices.json")
            indicesPath.writeText(string)
        }
    }
}
