package dev.kensa.parse

import dev.kensa.Configuration
import dev.kensa.KensaException
import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken
import dev.kensa.sentence.TemplateSentence
import dev.kensa.state.TestInvocationContext
import dev.kensa.util.NamedValue

class TestInvocationParser(private val configuration: Configuration) {

    private companion object {
        // Matches JUnit's default parameterized display name `[{index}] {arguments}`, capturing the arguments segment.
        val DISPLAY_NAME_ARGUMENTS = Regex("^\\[\\d+] (.*)$")
    }

    fun parse(testInvocationContext: TestInvocationContext, methodParser: MethodParser): Pair<ParsedInvocation, List<RenderError>> {
        val parsedMethod = try {
            methodParser.parse(testInvocationContext.method)
        } catch (e: KensaException) {
            throw e
        } catch (e: Exception) {
            throw KensaException("Unable to parse test method", e)
        }
        return render(parsedMethod, testInvocationContext)
    }

    private fun render(parsedMethod: ParsedMethod, testInvocationContext: TestInvocationContext): Pair<ParsedInvocation, List<RenderError>> {
        val errors = mutableListOf<RenderError>()

        val argumentLabels = argumentLabels(testInvocationContext.displayName, parsedMethod.parameters.descriptors.size)

        val namedParameterValues = runCatching {
            parsedMethod.parameters.descriptors
                .filterValues { !it.isParameterizedTestDescription }
                .map { (key, descriptor) ->
                    val value = descriptor.resolveValue(testInvocationContext.arguments)
                    NamedValue(key, labelledValue(value, descriptor, argumentLabels))
                }
        }.getOrElse { e ->
            errors += RenderError("ValueResolution", e.message ?: e.javaClass.name)
            emptyList()
        }

        val highlightedParameterValues = runCatching {
            namedParameterValues
                .filter { namedValue: NamedValue ->
                    parsedMethod.parameters.descriptors[namedValue.name]?.isHighlight ?: false
                }
                .map { namedValue -> NamedValue(namedValue.name, configuration.renderers.renderValue(namedValue.value)) }
        }.getOrElse { e ->
            errors += RenderError("ValueResolution", e.message ?: e.javaClass.name)
            emptyList()
        }

        val highlightedPropertyVals = runCatching {
            highlightedPropertyValues(parsedMethod.properties, testInvocationContext.instance)
        }.getOrElse { e ->
            errors += RenderError("ValueResolution", e.message ?: e.javaClass.name)
            emptySet()
        }

        val highlightedFixtureVals = runCatching {
            testInvocationContext.fixturesAndOutputs.fixtures.highlightedValues()
        }.getOrElse { e ->
            errors += RenderError("ValueResolution", e.message ?: e.javaClass.name)
            emptySet()
        }

        val highlightedOutputVals = runCatching {
            testInvocationContext.fixturesAndOutputs.outputs.highlightedValues()
        }.getOrElse { e ->
            errors += RenderError("ValueResolution", e.message ?: e.javaClass.name)
            emptySet()
        }

        val highlightedValues = LinkedHashSet<NamedValue>()
            .plus(highlightedPropertyVals)
            .plus(highlightedParameterValues)
            .plus(highlightedFixtureVals)
            .plus(highlightedOutputVals)

        val tokenFactory = runCatching {
            TokenRenderer(
                testInvocationContext.instance,
                testInvocationContext.arguments,
                configuration.renderers,
                FixtureAndOutputAccessor(testInvocationContext.fixturesAndOutputs),
                parsedMethod.parameters.descriptors,
                parsedMethod.properties,
                parsedMethod.methods,
                highlightedValues
            )
        }.getOrElse { e ->
            errors += RenderError("SentenceRender", e.message ?: e.javaClass.name)
            null
        }

        // Render sentences
        val sentences = if (tokenFactory != null) {
            renderSentences(parsedMethod.sentences, tokenFactory, errors)
        } else {
            listOf(RenderedSentence(listOf(RenderedToken.ErrorToken("⚠ render error")), 0))
        }

        // Resolve parameterized test description
        val parameterizedTestDescription = runCatching {
            parsedMethod.parameters.descriptors.values.find { it.isParameterizedTestDescription }
                ?.resolveValue(testInvocationContext.arguments, null)?.let { configuration.renderers.renderValue(it) }
        }.getOrElse { e ->
            errors += RenderError("ValueResolution", e.message ?: e.javaClass.name)
            null
        }

        val parsedInvocation = ParsedInvocation(
            parsedMethod.indexInSource,
            parsedMethod.name,
            namedParameterValues,
            sentences,
            highlightedValues,
            parameterizedTestDescription
        )

        return parsedInvocation to errors
    }

    // JUnit's parameterized display name (default `[{index}] {arguments}`) carries the `Named.of("label", value)`
    // label for each argument, but the invocation arguments are already unwrapped to the raw payload. For opaque
    // payloads (lambdas/builders with a default `Object.toString()`) the raw value renders as noise, so fall back to
    // the label parsed positionally from the display name when one is available.
    private fun argumentLabels(displayName: String, parameterCount: Int): List<String>? =
        DISPLAY_NAME_ARGUMENTS.matchEntire(displayName)?.groupValues?.get(1)
            ?.split(", ")
            ?.takeIf { it.size == parameterCount }

    private fun labelledValue(value: Any?, descriptor: ElementDescriptor, labels: List<String>?): Any? {
        if (value == null || labels == null || descriptor !is ElementDescriptor.ParameterElementDescriptor) return value
        val label = labels.getOrNull(descriptor.index) ?: return value
        return if (value.isOpaque() && label != value.toString()) label else value
    }

    // Opaque = a value that renders as noise: it relies on the default `Object.toString()` (`Type@hash`) and is not a
    // collection or array (those are rendered specially by the renderers and must not be replaced by a display label).
    private fun Any.isOpaque(): Boolean =
        this !is Collection<*> && !javaClass.isArray &&
            toString() == "${javaClass.name}@${Integer.toHexString(System.identityHashCode(this))}"

    private fun renderSentences(source: List<TemplateSentence>, renderer: TokenRenderer, errors: MutableList<RenderError>): List<RenderedSentence> =
        source.map { sentence ->
            runCatching { RenderedSentence(renderer.render(sentence.tokens), sentence.lineNumber) }
                .getOrElse { e ->
                    errors += RenderError("SentenceRender", e.message ?: e.javaClass.name)
                    RenderedSentence(listOf(RenderedToken.ErrorToken("Could not render sentence: ${e.message}")), sentence.lineNumber)
                }
        }

    private fun highlightedPropertyValues(fields: Map<String, ElementDescriptor>, testInstance: Any) = fields.values
        .filter(ElementDescriptor::isHighlight)
        .map { NamedValue(highlightOrFieldNameFor(it), configuration.renderers.renderValue(it.resolveValue(testInstance))) }
        .toSet()

    private fun highlightOrFieldNameFor(accessor: ElementDescriptor): String =
        accessor.takeIf { it.isHighlight }?.run {
            highlight?.value?.run { ifEmpty { accessor.name } }
        } ?: accessor.name
}
