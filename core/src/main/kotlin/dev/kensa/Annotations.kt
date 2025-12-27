package dev.kensa

import dev.kensa.RenderedHintStrategy.NoHint
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.state.SetupStrategy
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.*
import kotlin.reflect.KClass

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

/**
 * Annotation for marking methods or functions as nested.
 * These methods can be called from within the main test methods/functions
 * and are usually used to provide further, more detailed assertions that
 * are not required to be displayed all the time in the test output.
 * The content of these methods/functions can be expanded by clicking or hovering.
 */
@Retention(RUNTIME)
@Target(FUNCTION)
annotation class NestedSentence

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

