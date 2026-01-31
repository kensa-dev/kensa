package dev.kensa

import dev.kensa.RenderedHintStrategy.NoHint
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderedValueStyle.Default
import dev.kensa.state.SetupStrategy
import dev.kensa.tabs.InvocationIdentifierProvider
import dev.kensa.tabs.KensaTabRenderer
import dev.kensa.tabs.NoInvocationIdentifierProvider
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass


enum class KensaTabVisibility {
    Always,
    OnlyOnFailure
}

/**
 * Declares a custom per-invocation tab for the Modern UI report.
 *
 * ### Usage
 * Annotate a marker interface (or other class type) and have your test class implement it:
 *
 * ```kotlin
 * @KensaTab(name = "Logs", renderer = MyLogsRenderer::class)
 * interface WithLogs
 *
 * class MyTest : WithLogs { /* ... */ }
 * ```
 *
 * During report generation, Kensa discovers all KensaTab annotations present on interfaces
 * implemented by the test class. For each test invocation, Kensa will:
 *
 * 1. Instantiate the specified [renderer] (Kotlin `object` singletons are supported, otherwise a no-arg
 *    constructor is required).
 * 2. Call the renderer for the current invocation.
 * 3. If the renderer returns a non-blank string, write it to a file in the report output directory.
 * 4. Add an entry to the invocation JSON (`customTabContents`) that points the UI at the generated file.
 *
 * Returning `null` or a blank string from the renderer omits the tab for that invocation.
 *
 * ### Fields
 * - [id]: Optional stable identifier used for URL/path generation and for `autoOpenTab`.
 *   If empty, Kensa derives an id from [name]. Prefer setting this explicitly if you want to keep the
 *   tab id stable when renaming [name].
 * - [name]: Human-readable label displayed in the UI tab button.
 * - [renderer]: A class implementing [KensaTabRenderer] that produces the tab content for an invocation.
 * - [identifierProvider]: A class implementing [InvocationIdentifierProvider] that provides a per-invocation identifier used to correlate external data (e.g. logs) to this invocation. If not specified, [NoInvocationIdentifierProvider] is used.
 * - [visibility]: Controls when the tab should be generated for an invocation.
 */
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@Repeatable
annotation class KensaTab(
    val id: String = "",
    val name: String,
    val renderer: KClass<out KensaTabRenderer>,
    val identifierProvider: KClass<out InvocationIdentifierProvider> = NoInvocationIdentifierProvider::class,
    val visibility: KensaTabVisibility = KensaTabVisibility.Always
)

@Retention(RUNTIME)
@Target(CLASS, FUNCTION)
annotation class Notes(val value: String)

/**
 * Annotation used to specify emphasis in the test output of the attached component.
 *
 * Choos from a selection of the helpers found in the [Bulma CSS library](https://bulma.io/documentation/helpers/).
 *
 * @property textStyles Array of text styles to apply. Default is [TextStyle.TextWeightNormal].
 * @property textColour Colour of the text. Default is [Colour.Default].
 * @property backgroundColor Background colour for the annotation. Default is [Colour.Default].
 */
@Retention(RUNTIME)
@Target(FUNCTION, FIELD, VALUE_PARAMETER)
annotation class Emphasise(val textStyles: Array<TextStyle> = [TextStyle.TextWeightNormal], val textColour: Colour = Colour.Default, val backgroundColor: Colour = Colour.Default)

/**
 * Annotation for specifying that the value of a function (return value), field, parameter
 * or property should be highlighted throughout all the test output.
 *
 * @property value The highlighted text to be displayed. Default is an empty string.
 */
@Retention(RUNTIME)
@Target(FUNCTION, FIELD, VALUE_PARAMETER, PROPERTY_GETTER)
annotation class Highlight(val value: String = "")

/**
 * Annotation used to specify the issue/ticket number in a tracking system to which this test or test class refers.
 * The values specified here end up as links in the test output and are appended to the value specified
 * in the [dev.kensa.Configuration.issueTrackerUrl]
 *
 * @property value Array of issue identifiers.
 */
@Retention(RUNTIME)
@Target(ANNOTATION_CLASS, CLASS, FUNCTION)
annotation class Issue(vararg val value: String)

@Target(FUNCTION)
@Retention(RUNTIME)
@Deprecated(
    message = "Use ExpandableSentence instead",
    replaceWith = ReplaceWith("ExpandableSentence", "dev.kensa.ExpandableSentence")
)
annotation class NestedSentence

/**
 * Annotation for marking methods or functions as expandable.
 * These methods can be called from within the main test methods/functions
 * and are usually used to provide further, more detailed assertions that
 * are not required to be displayed all the time in the test output.
 * The content of these methods/functions can be expanded by clicking or hovering.
 */
@Retention(RUNTIME)
@Target(FUNCTION)
annotation class ExpandableSentence


/**
 * Annotation that specifies a container for rendered values.
 * This annotation causes Kensa to look for further rendering annotations in the class
 * of the annotated field or parameter.
 */
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER)
annotation class RenderedValueContainer

/**
 * Annotation used to mark specific fields or methods (return values) as renderable in the
 * test output. Without this annotation, the name of the field/method would be rendered in the output.
 * Annotated fields/methods have their actual final values rendered in the output.
 */
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER)
annotation class RenderedValue

/**
 * Annotation for marking fields, parameters, or methods as expandable rendered values.
 * This is typically used for complex objects or collections that should be displayed
 * with more detail in the test output, such as in a tabular format.
 *
 * @property renderAs The style to use when rendering the expanded value. Defaults to [RenderedValueStyle.Default].
 * @property headers Optional headers to be used if the [RenderedValueStyle.Tabular] style is selected.
 */
@Retention(RUNTIME)
@Target(FIELD, VALUE_PARAMETER, FUNCTION, PROPERTY_GETTER)
annotation class ExpandableRenderedValue(val renderAs: RenderedValueStyle = Default, val headers: Array<String> = [])

/**
 * Defines the available styles for rendering an [ExpandableRenderedValue].
 */
enum class RenderedValueStyle {
    /**
     * The default rendering style, usually a list or standard object representation.
     */
    Default,

    /**
     * Renders the value in a tabular format, using the provided headers.
     */
    Tabular
}

/**
 * Strategies for resolving the primary display text of a rendered token.
 */
enum class RenderedValueStrategy {
    /** Uses the identifier name from the source code (e.g., the variable name). */
    UseIdentifierName, 
    
    /** Uses the result of the object's [Any.toString] method. */
    UseToString, 
    
    /** Resolves the value by reading a specific property path on the object. */
    UseProperty, 
    
    /** Resolves the value by invoking a specific method on the object. */
    UseMethod
}

/**
 * Strategies for resolving the technical hint (metadata) displayed in a popup on the report.
 */
enum class RenderedHintStrategy {
    /** Extracts the hint by reading a specific property path on the object. */
    HintFromProperty, 
    
    /** Extracts the hint by invoking a specific method on the object. */
    HintFromMethod, 
    
    /** Disables the hint for this directive. */
    NoHint
}

/**
 * Annotation used to enrich rendered values with additional technical metadata (hints).
 * 
 * When Kensa encounters a field or property of the specified [type], it will use the 
 * provided strategies to determine both the display text and the technical hint 
 * (e.g., a JSON path or XPath) for the generated report.
 *
 * @property type The class type this directive applies to.
 * @property valueStrategy The strategy used to resolve the display text.
 * @property valueParam The property name or method name used if the strategy requires one.
 * @property hintStrategy The strategy used to resolve the technical hint popup content.
 * @property hintParam The property name or method name used to extract the hint.
 */
@Repeatable
@Target(CLASS, PROPERTY, FUNCTION)
@Retention(RUNTIME)
annotation class RenderedValueWithHint(
    val type: KClass<*>,
    val valueStrategy: RenderedValueStrategy = UseIdentifierName,
    val valueParam: String = "",
    val hintStrategy: RenderedHintStrategy = NoHint,
    val hintParam: String = ""
)

/**
 * Internal representation of a [RenderedValueWithHint] directive used by the parsing engine.
 */
data class RenderingDirective(
    val valueStrategy: RenderedValueStrategy,
    val valueParam: String,
    val hintStrategy: RenderedHintStrategy,
    val hintParam: String
)

/**
 * A lookup map associating a target class with its specific rendering instructions.
 */
typealias RenderingDirectives = Map<KClass<*>, RenderingDirective>

/**
 * Annotation used to specify other source files to parse.  Useful to point to shared nested sentences.
 */
@Retention(RUNTIME)
@Target(CLASS)
annotation class Sources(vararg val value: KClass<*>)

@Retention(RUNTIME)
@Target(VALUE_PARAMETER)
annotation class ParameterizedTestDescription

/**
 * Specifies which [dev.kensa.Tab] to automatically open in the test output UI.
 */
@Retention(RUNTIME)
@Target(FUNCTION, CLASS)
annotation class AutoOpenTab(val value: Tab)

/**
 * Used to specify how the test setup (usually Steps or Givens) is displayed in the sequence diagram.
 * One of [dev.kensa.state.SetupStrategy].
 */
@Retention(RUNTIME)
@Target(FUNCTION, CLASS)
annotation class UseSetupStrategy(val value: SetupStrategy)

