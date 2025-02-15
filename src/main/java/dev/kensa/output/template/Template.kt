package dev.kensa.output.template

import com.eclipsesource.json.Json
import dev.kensa.Configuration
import dev.kensa.KensaException
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms.andThen
import dev.kensa.output.json.JsonTransforms.toIndexJson
import dev.kensa.output.json.JsonTransforms.toJsonString
import dev.kensa.output.json.JsonTransforms.toJsonWith
import dev.kensa.output.template.FileTemplate.Mode.IndexFile
import dev.kensa.output.template.FileTemplate.Mode.TestFile
import dev.kensa.sentence.Acronym
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.pebbletemplates.pebble.template.PebbleTemplate
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.newBufferedWriter
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import kotlin.io.path.createDirectories
import com.eclipsesource.json.Json.`object` as jsonObject

class JsonScript(@Suppress("unused", "for pebble template") val id: String, @Suppress("unused", "for pebble template") val content: String)
class Index(@Suppress("unused", "for pebble template") val content: String)

sealed class FileTemplate(mode: Mode, configuration: Configuration, private val templateOutputPath: Path) {

    enum class Mode { IndexFile, TestFile }

    private val template: PebbleTemplate = pebbleEngine.getTemplate("pebble-index.html")
    private val kensaScript: String = configuration.outputDir.resolve("kensa.js").toString()

    private val templateMap = mutableMapOf<String, MutableList<Any>>("scripts" to mutableListOf<Any>(configuration.asJson(mode)))

    protected fun add(key: String, value: Any) {
        templateMap.getOrPut(key) { mutableListOf<Any>() }.add(value)
    }

    fun write() {
        try {
            template.evaluate(
                newBufferedWriter(templateOutputPath, UTF_8, CREATE),
                mapOf("kensaScript" to kensaScript) + templateMap
            )
        } catch (e: IOException) {
            throw KensaException("Unable to write template", e)
        }
    }

    class IndexFileTemplate(configuration: Configuration) : FileTemplate(IndexFile, configuration, configuration.outputDir.resolve("index.html")) {

        private var indexCounter = 1

        fun addIndex(container: TestContainer) {
            add("indices", container.transform(toIndexJson("test-result-$indexCounter++").andThen(toJsonString()).andThen(::Index)))
        }
    }

    class TestFileTemplate(configuration: Configuration, container: TestContainer) : FileTemplate(TestFile, configuration, container.deriveTestPath(configuration.outputDir, configuration.flattenOutputPackages)) {

        init {
            add("scripts", container.transform(toJsonWith(configuration.renderers).andThen(toJsonString().andThen { content: String -> JsonScript("test-result-1", content) })))
        }

        companion object {

            private fun TestContainer.deriveTestPath(outputDir: Path, flattenOutputPackages: Boolean): Path =
                if (flattenOutputPackages) {
                    outputDir.resolve("${testClass.name}.html")
                } else {
                    createDirectoriesAndCalculatePath(outputDir, testClass.name)
                }

            private fun createDirectoriesAndCalculatePath(outputDir: Path, fullClassName: String): Path {
                val parts = fullClassName.split(".")

                return outputDir.resolve(parts.dropLast(1).joinToString(separator = "/")).run {
                    createDirectories()

                    resolve("${parts.last()}.html")
                }
            }
        }
    }

    companion object {
        private val pebbleEngine = PebbleEngine.Builder().autoEscaping(false).loader(ClasspathLoader()).build()
        private fun Configuration.asJson(mode: Mode): JsonScript =
            JsonScript(
                "config",
                toJsonString()(
                    jsonObject()
                        .add("mode", mode.name)
                        .add("issueTrackerUrl", issueTrackerUrl.toString())
                        .add("acronyms", acronymsAsJson(acronyms))
                        .add("flattenPackages", flattenOutputPackages)
                        .add("sectionOrder", Json.array().apply {
                            sectionOrder.forEach { add(it.name) }
                        })
                )
            )

        private fun acronymsAsJson(acronyms: Collection<Acronym>) = jsonObject().apply {
            acronyms.forEach {
                add(it.acronym, it.meaning)
            }
        }
    }
}