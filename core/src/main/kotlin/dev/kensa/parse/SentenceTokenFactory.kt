package dev.kensa.parse

import dev.kensa.ElementDescriptor
import dev.kensa.ElementDescriptor.ResolveHolderElementDescriptor
import dev.kensa.KensaException
import dev.kensa.render.Renderers
import dev.kensa.sentence.SentenceToken
import dev.kensa.sentence.TokenType
import dev.kensa.sentence.TokenType.*
import dev.kensa.util.NamedValue

class SentenceTokenFactory(
    private val testInstance: Any,
    private val arguments: Array<Any?>,
    private val renderers: Renderers,
    private val fixturesAccessor: FixturesAccessor,
    private val parameters: Map<String, ElementDescriptor>,
    private val properties: Map<String, ElementDescriptor>,
    private val methods: Map<String, ElementDescriptor>,
    private val highlightedValues: Set<NamedValue>
) {

    private val regex = """^([a-zA-Z_][a-zA-Z0-9_]*)(?:\(\))?(?:\.(.+))?$""".toRegex()

    fun fixturesValueTokenFrom(token: SentenceToken) =
        SentenceToken(renderers.renderValue(fixturesAccessor.valueOf(token.value)), setOf(FixturesValue))

    fun fieldValueTokenFrom(token: SentenceToken): SentenceToken =
        regex.matchEntire(token.value)?.let { match ->
            properties[match.groupValues[1]]?.let { pd ->
                if (pd is ResolveHolderElementDescriptor) {
                    renderers.renderValue(pd.resolveValue(testInstance, token.value))
                } else {
                    renderers.renderValue(pd.resolveValue(testInstance, match.groupValues[2]))
                }.asSentenceToken(FieldValue, pd.isHighlight)
            }
        } ?: throw KensaException("Token with type FieldValue did not refer to an actual field")

    fun methodValueTokenFrom(token: SentenceToken): SentenceToken =
        regex.matchEntire(token.value)?.let { match ->
            methods[match.groupValues[1]]?.let { md ->
                renderers.renderValue(md.resolveValue(testInstance, match.groupValues[2]))
                    .asSentenceToken(MethodValue, md.isHighlight)
            }
        } ?: throw KensaException("Token with type MethodValue did not refer to an actual method")

    fun parameterValueTokenFrom(token: SentenceToken): SentenceToken =
        regex.matchEntire(token.value)?.let { match ->
            parameters[match.groupValues[1]]?.let { pd ->
                renderers.renderValue(pd.resolveValue(arguments, match.groupValues[2]))
                    .asSentenceToken(ParameterValue, pd.isHighlight)
            }

        } ?: throw KensaException("Token with type ParameterValue did not refer to an actual parameter")

    private fun String.asSentenceToken(type: TokenType, shouldHighlight: Boolean) =
        SentenceToken(this, buildSet {
            add(type)
            if (shouldHighlight || isHighlighted()) add(Highlighted)
        })

    private fun String.isHighlighted() = highlightedValues.any { it.value == this }
}