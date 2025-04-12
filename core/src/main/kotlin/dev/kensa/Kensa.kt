package dev.kensa

import dev.kensa.PackageDisplayMode.HideCommonPackages
import dev.kensa.Section.*
import dev.kensa.render.InteractionRenderer
import dev.kensa.render.ListRendererFormat
import dev.kensa.render.Renderers
import dev.kensa.render.ValueRenderer
import dev.kensa.render.diagram.directive.UmlDirective
import dev.kensa.sentence.Acronym
import dev.kensa.sentence.Dictionary
import dev.kensa.sentence.Keyword
import dev.kensa.sentence.ProtectedPhrase
import dev.kensa.state.SetupStrategy
import org.antlr.v4.runtime.atn.PredictionMode
import java.net.URI
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.reflect.KClass

private const val KENSA_OUTPUT_ROOT = "kensa.output.root"
private const val KENSA_DISABLE_OUTPUT = "kensa.disable.output"
private const val KENSA_OUTPUT_DIR = "kensa-output"

fun interface KensaConfigurationProvider {
    operator fun invoke() : Configuration
}

object StaticKensaConfigurationProvider : KensaConfigurationProvider {
    override fun invoke(): Configuration = Kensa.configuration
}

object ThreadLocalKensaConfigurationProvider : KensaConfigurationProvider {
    private val holder = ThreadLocal<Configuration>()

    override fun invoke(): Configuration = holder.get()

    fun set(configuration: Configuration) {
        holder.set(configuration)
    }

    fun remove() {
        holder.remove()
    }
}

fun testConfiguration(block: Configuration.() -> Unit) = ThreadLocalKensaConfigurationProvider().apply(block)

object Kensa {
    internal val configuration: Configuration = Configuration()

    @JvmStatic
    fun configure() = KensaConfigurator(configuration)
    fun konfigure(block: Configuration.() -> Unit) {
        configuration.apply(block)
    }
}

class KensaConfigurator(private val configuration: Configuration) {

    fun withIssueTrackerUrl(url: URL): KensaConfigurator = apply { configuration.issueTrackerUrl = url }
    fun withOutputDir(dir: String): KensaConfigurator = withOutputDir(Paths.get(dir))

    fun withOutputDir(dir: Path): KensaConfigurator = apply {
        require(dir.isAbsolute) { "OutputDir must be absolute." }
        configuration.outputDir = if (dir.endsWith(KENSA_OUTPUT_DIR)) dir else dir.resolve(KENSA_OUTPUT_DIR)
    }

    fun withOutputDisabled(): KensaConfigurator = apply { configuration.isOutputEnabled = false }

    fun withListRendererFormat(format: ListRendererFormat): KensaConfigurator = apply { configuration.renderers.setListRendererFormat(format) }

    fun <T : Any> withValueRenderer(klass: Class<T>, renderer: ValueRenderer<out T>): KensaConfigurator = apply { configuration.renderers.addValueRenderer(klass, renderer) }

    fun <T : Any> withValueRenderer(klass: KClass<T>, renderer: ValueRenderer<out T>): KensaConfigurator = apply { configuration.renderers.addValueRenderer(klass, renderer) }

    fun <T : Any> withInteractionRenderer(klass: Class<T>, renderer: InteractionRenderer<out T>): KensaConfigurator = apply { configuration.renderers.addInteractionRenderer(klass, renderer) }

    fun <T : Any> withInteractionRenderer(klass: KClass<T>, renderer: InteractionRenderer<out T>): KensaConfigurator = apply { configuration.renderers.addInteractionRenderer(klass, renderer) }

    fun withProtectedPhrases(vararg phrases: ProtectedPhrase): KensaConfigurator = apply { configuration.dictionary.putProtectedPhrases(*phrases) }

    fun withAcronyms(vararg acronyms: Acronym): KensaConfigurator = apply { configuration.dictionary.putAcronyms(*acronyms) }

    fun withKeywords(vararg keywords: String): KensaConfigurator = apply { configuration.dictionary.putKeywords(*keywords) }

    fun withKeywords(vararg keywords: Keyword): KensaConfigurator = apply { configuration.dictionary.putKeywords(*keywords) }

    fun withSectionOrder(vararg order: Section): KensaConfigurator = apply { configuration.sectionOrder = order.toList() }

    fun withAutoOpenTab(tab: Tab): KensaConfigurator = apply { configuration.autoOpenTab = tab }

    fun withSetupStrategy(setupStrategy: SetupStrategy): KensaConfigurator = apply { configuration.setupStrategy = setupStrategy }

    fun withTabSize(tabSize: Int): KensaConfigurator = apply { configuration.tabSize = tabSize }

    fun withFlattenOutputPackages(value: Boolean): KensaConfigurator = apply { configuration.flattenOutputPackages = value }

    fun withPackageDisplayMode(packageDisplayMode: PackageDisplayMode): KensaConfigurator = apply { configuration.packageDisplayMode = packageDisplayMode }

    /**
     * Sets the packages to scan for fixture containers.
     *
     * @param packageNames The names of the packages to scan for fixture containers
     * @return This configurator
     */
    fun withFixturePackages(vararg packageNames: String): KensaConfigurator = apply { configuration.fixturePackages = packageNames.toList() }

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

class Configuration(
    val dictionary: Dictionary = Dictionary(),
    val renderers: Renderers = Renderers(),
    var outputDir: Path = Path(System.getProperty(KENSA_OUTPUT_ROOT, System.getProperty("java.io.tmpdir")), KENSA_OUTPUT_DIR),
    var flattenOutputPackages: Boolean = false,
    var isOutputEnabled: Boolean = if (System.getProperties().containsKey(KENSA_DISABLE_OUTPUT)) {
        System.getProperty(KENSA_DISABLE_OUTPUT, "").let { it.isNotBlank() && !it.toBoolean() }
    } else true,
    var antlrPredicationMode: PredictionMode = PredictionMode.LL,
    var antlrErrorListenerDisabled: Boolean = true,
    var umlDirectives: List<UmlDirective> = ArrayList(),
    var issueTrackerUrl: URL = URI.create("http://empty").toURL(),
    var tabSize: Int = 5,
    var autoOpenTab: Tab = Tab.None,
    var packageDisplayMode: PackageDisplayMode = HideCommonPackages,
    var setupStrategy: SetupStrategy = SetupStrategy.Ungrouped,
    var fixturePackages: List<String> = emptyList(),

    private var _sectionOrder: List<Section> = listOf(Buttons, Sentences, Exception),
) {

    var sectionOrder: List<Section>
        get() = _sectionOrder
        set(order) {
            if (order.size != 3 || order.groupingBy { it }.eachCount().any { it.value > 1 }) {
                throw IllegalArgumentException("Invalid section order specified")
            }
            _sectionOrder = order
        }

    var keywords: Set<String> = emptySet()
        set(value) = dictionary.putKeywords(value)

    var acronyms: Set<Acronym> = emptySet()
        set(value) = dictionary.putAcronyms(value)

    var protectedPhrases: Set<ProtectedPhrase> = emptySet()
        set(value) = dictionary.putProtectedPhrases(value)
}
