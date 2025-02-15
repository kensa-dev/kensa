package dev.kensa.parse

import dev.kensa.Kensa.configuration
import dev.kensa.KensaException
import dev.kensa.parse.Accessor.ValueAccessor
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceToken
import dev.kensa.sentence.TokenType.*
import dev.kensa.state.TestInvocationContext
import dev.kensa.util.NamedValue

class TestInvocationParser {

    fun parse(context: TestInvocationContext, methodParser: MethodParser): ParsedInvocation =
        try {
            val parsedMethod = methodParser.parse(context.method)

            val namedParameterValues = parsedMethod.parameters.descriptors
                .filterValues { !it.isParameterizedTestDescription }
                .map { entry ->
                    NamedValue(entry.key, configuration.renderers.renderValue(entry.value.valueOfIn(context.arguments)))
                }

            val highlightedParameterValues = namedParameterValues.filter { namedValue: NamedValue ->
                parsedMethod.parameters.descriptors[namedValue.name]?.isHighlight ?: false
            }

            val highlightedValues = LinkedHashSet<NamedValue>()
                .plus(highlightedPropertyValues(parsedMethod.properties, context.instance))
                .plus(highlightedParameterValues)

            val scenarioProperties = parsedMethod.properties.filter { it.value.isScenario }

            val tokenFactory = SentenceTokenFactory(
                context.instance,
                context.arguments,
                configuration.renderers,
                CachingScenarioMethodAccessor(context.instance, scenarioProperties),
                parsedMethod.parameters.descriptors,
                parsedMethod.properties,
                parsedMethod.methods,
                highlightedValues
            )

            val sentences = regenerateSentences(parsedMethod.sentences, tokenFactory)

//                sentences.forEach { println(it.squashedTokens) }

            val parameterizedTestDescription: String? = parsedMethod.parameters.descriptors.values.find { it.isParameterizedTestDescription }?.valueOfIn(context.arguments)?.toString()

            ParsedInvocation(parsedMethod.name, namedParameterValues, sentences, highlightedValues, parameterizedTestDescription)
        } catch (e: Exception) {
            throw KensaException("Unable to parse test invocation ", e)
        }

    private fun regenerateSentences(source: List<Sentence>, tokenFactory: SentenceTokenFactory): List<Sentence> =
        source.map { sentence -> Sentence(regenerateTokens(sentence.tokens, tokenFactory)) }

    private fun regenerateTokens(tokens: List<SentenceToken>, tokenFactory: SentenceTokenFactory): List<SentenceToken> =
        tokens.map { token ->
            when {
                token.hasType(FieldValue) -> tokenFactory.fieldValueTokenFrom(token)
                token.hasType(MethodValue) -> tokenFactory.methodValueTokenFrom(token)
                token.hasType(ParameterValue) -> tokenFactory.parameterValueTokenFrom(token)
                token.hasType(ScenarioValue) -> tokenFactory.scenarioValueTokenFrom(token)
                token.hasType(Expandable) -> SentenceToken(token.value, token.tokenTypes, nestedTokens = token.nestedTokens.map { subTokens -> regenerateTokens(subTokens, tokenFactory) })
                else -> token
            }
        }

    private fun highlightedPropertyValues(fields: Map<String, ValueAccessor>, testInstance: Any) = fields.values
        .filter(ValueAccessor::isHighlight)
        .map { NamedValue(highlightOrFieldNameFor(it), configuration.renderers.renderValue(it.valueOfIn(testInstance))) }
        .toSet()

    private fun highlightOrFieldNameFor(accessor: ValueAccessor): String =
        accessor.takeIf { it.isHighlight }?.run {
            highlight.value.run { ifEmpty { accessor.name } }
        } ?: accessor.name
}