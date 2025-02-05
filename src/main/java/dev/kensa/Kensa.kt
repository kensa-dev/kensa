package dev.kensa

import dev.kensa.Kensa.KENSA_DISABLE_OUTPUT
import dev.kensa.Kensa.KENSA_OUTPUT_DIR
import dev.kensa.Kensa.KENSA_OUTPUT_ROOT
import dev.kensa.PackageDisplayMode.HideCommonPackages
import dev.kensa.Section.*
import dev.kensa.render.*
import dev.kensa.render.diagram.directive.UmlDirective
import dev.kensa.sentence.Acronym
import dev.kensa.sentence.Dictionary
import dev.kensa.sentence.ProtectedPhrase
import dev.kensa.sentence.Keyword
import dev.kensa.state.SetupStrategy
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

    @Deprecated("Deprecated", ReplaceWith("withValueRenderer(klass, renderer)", "dev.kensa"))
    fun <T : Any> withRenderer(klass: Class<T>, renderer: Renderer<out T>): Kensa = apply {
        withValueRenderer(klass, renderer)
    }

    @Deprecated("Deprecated", ReplaceWith("withValueRenderer(klass, renderer)", "dev.kensa"))
    fun <T : Any> withRenderer(klass: KClass<T>, renderer: Renderer<out T>): Kensa = apply {
        withValueRenderer(klass, renderer)
    }

    fun <T : Any> withListRendererFormat(format: ListRendererFormat): Kensa = apply {
        configuration.renderers.setListRendererFormat(format)
    }

    fun <T : Any> withValueRenderer(klass: Class<T>, renderer: ValueRenderer<out T>): Kensa = apply {
        configuration.renderers.addValueRenderer(klass, renderer)
    }

    fun <T : Any> withValueRenderer(klass: KClass<T>, renderer: ValueRenderer<out T>): Kensa = apply {
        configuration.renderers.addValueRenderer(klass, renderer)
    }

    fun <T : Any> withInteractionRenderer(klass: Class<T>, renderer: InteractionRenderer<out T>): Kensa = apply {
        configuration.renderers.addInteractionRenderer(klass, renderer)
    }

    fun <T : Any> withInteractionRenderer(klass: KClass<T>, renderer: InteractionRenderer<out T>): Kensa = apply {
        configuration.renderers.addInteractionRenderer(klass, renderer)
    }

    fun withProtectedPhrases(vararg phrases: ProtectedPhrase): Kensa = apply {
        configuration.dictionary.putProtectedPhrases(*phrases)
    }

    fun withAcronyms(vararg acronyms: Acronym): Kensa = apply {
        configuration.dictionary.putAcronyms(*acronyms)
    }

    fun withKeywords(vararg keywords: String): Kensa = apply {
        configuration.dictionary.putKeywords(*keywords)
    }

    fun withKeywords(vararg keywords: Keyword): Kensa = apply {
        configuration.dictionary.putKeywords(*keywords)
    }

    fun withSectionOrder(vararg order: Section): Kensa = apply {
        configuration.sectionOrder = order.toList()
    }

    fun withAutoOpenTab(tab: Tab): Kensa = apply {
        configuration.autoOpenTab = tab
    }

    fun withSetupStrategy(setupStrategy: SetupStrategy): Kensa = apply {
        configuration.setupStrategy = setupStrategy
    }

    fun withTabSize(tabSize: Int): Kensa = apply {
        configuration.tabSize = tabSize
    }
    
    fun withFlattenOutputPackages(value: Boolean): Kensa = apply {
        configuration.flattenOutputPackages = value
    }
    
    fun withPackageDisplayMode(packageDisplayMode: PackageDisplayMode): Kensa = apply {
        configuration.packageDisplayMode = packageDisplayMode
    }
}

enum class PackageDisplayMode {
    Hidden,
    HideCommonPackages,
    ShowFullPackage,
}

enum class Section {
    Buttons,
    Exception,
    Sentences
}

enum class Tab {
    CapturedInteractions,
    Givens,
    Parameters,
    SequenceDiagram,
    None
}

class Configuration {
    val dictionary: Dictionary = Dictionary()
    val renderers: Renderers = Renderers()

    var outputDir: Path = Paths.get(
        System.getProperty(KENSA_OUTPUT_ROOT, System.getProperty("java.io.tmpdir")),
        KENSA_OUTPUT_DIR
    )
    var flattenOutputPackages: Boolean = false
    var isOutputEnabled: Boolean = if (System.getProperties().containsKey(KENSA_DISABLE_OUTPUT)) {
        System.getProperty(KENSA_DISABLE_OUTPUT, "").let { it.isNotBlank() && !it.toBoolean() }
    } else true
    var antlrPredicationMode: PredictionMode = PredictionMode.LL
    var antlrErrorListenerDisabled: Boolean = true
    var umlDirectives: List<UmlDirective> = ArrayList()
    var issueTrackerUrl: URL = defaultIssueTrackerUrl()
    var tabSize: Int = 5
    var autoOpenTab: Tab = Tab.None
    var packageDisplayMode: PackageDisplayMode = HideCommonPackages

    var setupStrategy: SetupStrategy = SetupStrategy.Ungrouped

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

    var protectedPhrases: Set<ProtectedPhrase> = emptySet()
        set(value) = dictionary.putProtectedPhrases(value)

    fun disableOutput() {
        isOutputEnabled = false
    }

    companion object {
        private fun defaultIssueTrackerUrl(): URL = try {
            URI.create("http://empty").toURL()
        } catch (_: MalformedURLException) {
            throw KensaException("Unable to initialise default issue tracker url")
        }
    }
}
