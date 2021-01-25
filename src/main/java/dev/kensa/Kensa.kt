package dev.kensa

import dev.kensa.Kensa.KENSA_DISABLE_OUTPUT
import dev.kensa.Kensa.KENSA_OUTPUT_DIR
import dev.kensa.Kensa.KENSA_OUTPUT_ROOT
import dev.kensa.Section.*
import dev.kensa.output.OutputStyle
import dev.kensa.output.template.Template
import dev.kensa.render.Renderer
import dev.kensa.render.Renderers
import dev.kensa.render.diagram.directive.UmlDirective
import dev.kensa.sentence.Acronym
import dev.kensa.sentence.Dictionary
import org.antlr.v4.runtime.atn.PredictionMode
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass

object Kensa {

    internal const val KENSA_OUTPUT_ROOT = "kensa.output.root"
    internal const val KENSA_DISABLE_OUTPUT = "kensa.disable.output"
    internal const val KENSA_OUTPUT_DIR = "kensa-output"

    @JvmStatic
    val configuration = Configuration()

    @JvmStatic
    fun configure() = Kensa

    fun konfigure(init: Configuration.() -> Unit) {
        configuration.apply(init)
    }

    fun withIssueTrackerUrl(url: String): Kensa = apply {
        try {
            withIssueTrackerUrl(URL(url))
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("Invalid Issue Tracker URL specified.", e)
        }
    }

    fun withIssueTrackerUrl(url: URL): Kensa = apply {
        configuration.issueTrackerUrl = url
    }

    fun withOutputDir(dir: String): Kensa = withOutputDir(Paths.get(dir))

    fun withOutputDir(dir: Path): Kensa = apply {
        require(dir.isAbsolute) { "OutputDir must be absolute." }
        configuration.outputDir = if (dir.endsWith(KENSA_OUTPUT_DIR)) dir else dir.resolve(KENSA_OUTPUT_DIR)
    }

    fun withOutputDisabled(): Kensa = apply {
        configuration.isOutputEnabled = false
    }

    fun <T : Any> withRenderer(klass: Class<T>, renderer: Renderer<out T>): Kensa = apply {
        configuration.renderers.add(klass, renderer)
    }

    fun <T : Any> withRenderer(kClass: KClass<T>, renderer: Renderer<out T>): Kensa = apply {
        configuration.renderers.add(kClass, renderer)
    }

    fun withOutputStyle(outputStyle: OutputStyle): Kensa = apply {
        configuration.outputStyle = outputStyle
    }

    fun withAcronyms(vararg acronyms: Acronym): Kensa = apply {
        configuration.dictionary.putAcronyms(*acronyms)
    }

    fun withKeywords(vararg keywords: String): Kensa = apply {
        configuration.dictionary.putKeywords(*keywords)
    }

    fun withSectionOrder(vararg order: Section): Kensa = apply {
        configuration.sectionOrder = order.toList()
    }

    fun withTabSize(tabSize: Int): Kensa = apply {
        configuration.tabSize = tabSize
    }
}

enum class Section {
    Buttons,
    Exception,
    Sentences
}

class Configuration(
    val dictionary: Dictionary = Dictionary(),
    var outputDir: Path = Paths.get(
        System.getProperty(KENSA_OUTPUT_ROOT, System.getProperty("java.io.tmpdir")),
        KENSA_OUTPUT_DIR
    ),
    var isOutputEnabled: Boolean = if(System.getProperties().containsKey(KENSA_DISABLE_OUTPUT)) {
        System.getProperty(KENSA_DISABLE_OUTPUT, "").let { it.isNotBlank() && !it.toBoolean() }
    } else true,
    val renderers: Renderers = Renderers(),
    var antlrPredicationMode: PredictionMode = PredictionMode.SLL,
    var antlrErrorListenerDisabled: Boolean = true,
    var umlDirectives: List<UmlDirective> = ArrayList(),
    var outputStyle: OutputStyle = OutputStyle.MultiFile,
    var issueTrackerUrl: URL = defaultIssueTrackerUrl(),
    var tabSize: Int = 5
) {

    var sectionOrder: List<Section> = listOf(Buttons, Sentences, Exception)
        set(order) {
            if (order.size != 3 || order.groupingBy { it }.eachCount().any { it.value > 1 }) {
                throw IllegalArgumentException("Invalid section order specified")
            }
            field = order
        }

    var keywords: Set<String> = emptySet()
        set(value) = dictionary.putKeywords(value)

    var acronyms: Set<Acronym> = emptySet()
        set(value) = dictionary.putAcronyms(value)

    fun disableOutput() {
        isOutputEnabled = false
    }

    fun createTemplate(path: String, mode: Template.Mode): Template =
        Template(outputDir.resolve(path), mode, issueTrackerUrl, sectionOrder)

    companion object {
        private fun defaultIssueTrackerUrl(): URL = try {
            URI.create("http://empty").toURL()
        } catch (ignored: MalformedURLException) {
            throw KensaException("Unable to initialise default issue tracker url")
        }
    }
}
