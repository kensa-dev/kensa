package dev.kensa.parse

import dev.kensa.Kensa.configuration
import dev.kensa.KensaException
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceToken
import dev.kensa.sentence.TokenType.*
import dev.kensa.state.TestInvocationContext
import dev.kensa.util.NamedValue
import dev.kensa.util.Reflect

class TestInvocationParser {

    fun parse(context: TestInvocationContext, methodParser: MethodParser<*>): ParsedTestInvocation =
            try {
                val parsedMethod = methodParser.parse(context.method)

                val namedParameterValues = parsedMethod.parameters.descriptors.map { entry ->
                    NamedValue(entry.key, configuration.renderers.renderValueOnly(context.arguments[entry.value.index]))
                }

                val highlightedParameterValues = namedParameterValues.filter { namedValue: NamedValue ->
                    parsedMethod.parameters.descriptors[namedValue.name]?.isHighlighted ?: false
                }

                val highlightedValues = LinkedHashSet<NamedValue>()
                        .plus(highlightedFieldValues(parsedMethod.fields, context.instance))
                        .plus(highlightedParameterValues)

                val tokenFactory = SentenceTokenFactory(
                        context.instance,
                        context.arguments,
                        configuration.renderers,
                        Reflect.scenarioAccessorFor(context.instance),
                        parsedMethod.parameters.descriptors,
                        parsedMethod.fields,
                        highlightedValues
                )

                val sentences = regenerateSentences(parsedMethod.sentences, tokenFactory)

//                sentences.forEach { println(it.squashedTokens) }

                ParsedTestInvocation(parsedMethod.name, namedParameterValues, sentences, highlightedValues)
            } catch (e: Exception) {
                throw KensaException("Unable to parse test invocation for  ", e)
            }

    private fun regenerateSentences(source: List<Sentence>, tokenFactory: SentenceTokenFactory): List<Sentence> =
            source.map { sentence -> Sentence(regenerateTokens(sentence.tokens, tokenFactory)) }

    private fun regenerateTokens(tokens: List<SentenceToken>, tokenFactory: SentenceTokenFactory): List<SentenceToken> =
            tokens.map { token ->
                when {
                    token.hasType(FieldValue) -> tokenFactory.fieldValueTokenFrom(token)
                    token.hasType(ParameterValue) -> tokenFactory.parameterValueTokenFrom(token)
                    token.hasType(ScenarioValue) -> tokenFactory.scenarioValueTokenFrom(token)
                    token.hasType(Expandable) -> SentenceToken(token.value, token.tokenTypes, nestedTokens = token.nestedTokens.map { subTokens -> regenerateTokens(subTokens, tokenFactory) })
                    else -> token
                }
            }

    private fun highlightedFieldValues(fields: Map<String, FieldDescriptor>, testInstance: Any) = fields.values
            .filter { it.isHighlighted }
            .map { NamedValue(it.name, configuration.renderers.renderValueOnly(Reflect.fieldValue<Any>(it.field, testInstance))) }
            .toSet()
}