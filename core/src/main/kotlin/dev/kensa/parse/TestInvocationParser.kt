package dev.kensa.parse

import dev.kensa.Configuration
import dev.kensa.KensaException
import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken
import dev.kensa.sentence.TemplateSentence
import dev.kensa.state.TestInvocationContext
import dev.kensa.util.NamedValue

class TestInvocationParser(private val configuration: Configuration) {

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

        val namedParameterValues = runCatching {
            parsedMethod.parameters.descriptors
                .filterValues { !it.isParameterizedTestDescription }
                .map { (key, value) ->
                    NamedValue(key, configuration.renderers.renderValue(value.resolveValue(testInvocationContext.arguments)))
                }
        }.getOrElse { e ->
            errors += RenderError("ValueResolution", e.message ?: e.javaClass.name)
            emptyList()
        }

        val highlightedParameterValues = runCatching {
            namedParameterValues.filter { namedValue: NamedValue ->
                parsedMethod.parameters.descriptors[namedValue.name]?.isHighlight ?: false
            }
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
                ?.resolveValue(testInvocationContext.arguments, null)?.toString()
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
