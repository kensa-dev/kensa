package dev.kensa.parse

import dev.kensa.Configuration
import dev.kensa.KensaException
import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.TemplateSentence
import dev.kensa.state.TestInvocationContext
import dev.kensa.util.NamedValue

class TestInvocationParser(private val configuration: Configuration) {

    fun parse(testInvocationContext: TestInvocationContext, methodParser: MethodParser): ParsedInvocation =
        try {
            val parsedMethod = methodParser.parse(testInvocationContext.method)

            val namedParameterValues = parsedMethod.parameters.descriptors
                .filterValues { !it.isParameterizedTestDescription }
                .map { (key, value) ->
                    NamedValue(key, configuration.renderers.renderValue(value.resolveValue(testInvocationContext.arguments)))
                }

            val highlightedParameterValues = namedParameterValues.filter { namedValue: NamedValue ->
                parsedMethod.parameters.descriptors[namedValue.name]?.isHighlight ?: false
            }

            val highlightedValues = LinkedHashSet<NamedValue>()
                .plus(highlightedPropertyValues(parsedMethod.properties, testInvocationContext.instance))
                .plus(highlightedParameterValues)

            val tokenFactory = TokenRenderer(
                testInvocationContext.instance,
                testInvocationContext.arguments,
                configuration.renderers,
                FixtureAndOutputAccessor(testInvocationContext.fixturesAndOutputs),
                parsedMethod.parameters.descriptors,
                parsedMethod.properties,
                parsedMethod.methods,
                highlightedValues
            )

            val sentences = renderSentences(parsedMethod.sentences, tokenFactory)

//                sentences.forEach { println(it.squashedTokens) }

            val parameterizedTestDescription: String? = parsedMethod.parameters.descriptors.values.find { it.isParameterizedTestDescription }?.resolveValue(testInvocationContext.arguments, null)?.toString()

            ParsedInvocation(parsedMethod.indexInSource, parsedMethod.name, namedParameterValues, sentences, highlightedValues, parameterizedTestDescription)
        } catch (e: Exception) {
            throw KensaException("Unable to parse test invocation ", e)
        }

    private fun renderSentences(source: List<TemplateSentence>, renderer: TokenRenderer): List<RenderedSentence> =
        source.map { sentence -> RenderedSentence(renderer.render(sentence.tokens)) }

    private fun highlightedPropertyValues(fields: Map<String, ElementDescriptor>, testInstance: Any) = fields.values
        .filter(ElementDescriptor::isHighlight)
        .map { NamedValue(highlightOrFieldNameFor(it), configuration.renderers.renderValue(it.resolveValue(testInstance))) }
        .toSet()

    private fun highlightOrFieldNameFor(accessor: ElementDescriptor): String =
        accessor.takeIf { it.isHighlight }?.run {
            highlight?.value?.run { ifEmpty { accessor.name } }
        } ?: accessor.name
}