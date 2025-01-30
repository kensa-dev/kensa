package dev.kensa.output.template

import com.eclipsesource.json.Json
import dev.kensa.Tab
import dev.kensa.KensaException
import dev.kensa.Section
import dev.kensa.context.TestContainer
import dev.kensa.output.json.JsonTransforms.rangeTo
import dev.kensa.output.json.JsonTransforms.toIndexJson
import dev.kensa.output.json.JsonTransforms.toJsonString
import dev.kensa.output.json.JsonTransforms.toJsonWith
import dev.kensa.render.Renderers
import dev.kensa.sentence.Acronym
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.pebbletemplates.pebble.template.PebbleTemplate
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import com.eclipsesource.json.Json.`object` as jsonObject

class JsonScript(@Suppress("unused", "for pebble template") val id: String, @Suppress("unused", "for pebble template") val content: String)
class Index(@Suppress("unused", "for pebble template") val content: String)

class Template(private val outputPath: Path, mode: Mode, issueTrackerUrl: URL, tab: Tab, sectionOrder: List<Section>, acronyms: Set<Acronym>) {
    enum class Mode {
        IndexFile, TestFile,
    }

    private val indices: MutableList<Index> = ArrayList()
    private val template: PebbleTemplate = pebbleEngine.getTemplate("pebble-index.html")
    private val scripts: MutableList<JsonScript> = ArrayList()
    private var indexCounter = 1

    fun addIndex(container: TestContainer, factory: (TestContainer, Int) -> Index) {
        indices += factory(container, indexCounter++)
    }

    fun addJsonScript(container: TestContainer, factory: (TestContainer, Int) -> JsonScript) {
        scripts += factory(container, indexCounter)
    }

    fun write() {
        HashMap<String, Any>().apply {
            this["scripts"] = scripts
            this["indices"] = indices
            write(this)
        }
        scripts.clear()
        indices.clear()
    }

    private fun write(context: Map<String, Any>) {
        try {
            template.evaluate(Files.newBufferedWriter(outputPath, UTF_8, CREATE), context)
        } catch (e: IOException) {
            throw KensaException("Unable to write template", e)
        }
    }

    private fun configurationJson(mode: Mode, issueTrackerUrl: URL, sectionOrder: List<Section>, acronyms: Set<Acronym>): JsonScript {
        return JsonScript(
            "config",
            toJsonString()(
                jsonObject()
                    .add("mode", mode.name)
                    .add("issueTrackerUrl", issueTrackerUrl.toString())
                    .add("acronyms", acronymsAsJson(acronyms))
                    .add("sectionOrder", Json.array().apply {
                        sectionOrder.forEach { add(it.name) }
                    })
            )
        )
    }

    private fun acronymsAsJson(collection: Collection<Acronym>) = jsonObject().apply {
        collection.forEach {
            add(it.acronym, it.meaning)
        }
    }

    companion object {
        private val pebbleEngine = PebbleEngine.Builder().autoEscaping(false).loader(ClasspathLoader()).build()

        fun asIndex(): (TestContainer, Int) -> Index {
            fun toIndex() = { content: String -> Index(content) }

            return { container, index ->
                container.transform(toIndexJson("test-result-$index")..toJsonString()..toIndex())
            }
        }


        fun asJsonScript(renderers: Renderers): (TestContainer, Int) -> JsonScript {
            return { container, index ->
                fun toScript() = { content: String -> JsonScript("test-result-$index", content) }

                container.transform(toJsonWith(renderers)..toJsonString()..toScript())
            }
        }
    }

    init {
        scripts.add(configurationJson(mode, issueTrackerUrl, sectionOrder, acronyms))
    }
}