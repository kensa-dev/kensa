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

    fun fixturesValueTokenFrom(token: SentenceToken) =
        token.value.split(":").let {(name, path) ->
            SentenceToken(renderers.renderValue(fixturesAccessor.valueOf(name, path)), setOf(FixturesValue))
        }

    fun fieldValueTokenFrom(token: SentenceToken): SentenceToken =
        token.value.split(":").let { (name, path) ->
            properties[name]?.let { pd ->
                if (pd is ResolveHolderElementDescriptor) {
                    renderers.renderValue(pd.resolveValue(testInstance, "$name.$path"))
                } else {
                    renderers.renderValue(pd.resolveValue(testInstance, path))
                }.asSentenceToken(FieldValue, pd.isHighlight)
            }
        } ?: throw KensaException("Token [${token.value}] with type FieldValue did not refer to an actual field")

    fun methodValueTokenFrom(token: SentenceToken): SentenceToken =
        token.value.split(":").let { (name, path) ->
            methods[name]?.let { md ->
                renderers.renderValue(md.resolveValue(testInstance, path))
                    .asSentenceToken(MethodValue, md.isHighlight)
            }
        } ?: throw KensaException("Token [${token.value}] with type MethodValue did not refer to an actual method")

    fun parameterValueTokenFrom(token: SentenceToken): SentenceToken =
        token.value.split(":").let { (name, path) ->
            parameters[name]?.let { pd ->
                renderers.renderValue(pd.resolveValue(arguments, path))
                    .asSentenceToken(ParameterValue, pd.isHighlight)
            }
        } ?: throw KensaException("Token [${token.value}] with type ParameterValue did not refer to an actual parameter")

    private fun String.asSentenceToken(type: TokenType, shouldHighlight: Boolean) =
        SentenceToken(this, buildSet {
            add(type)
            if (shouldHighlight || isHighlighted()) add(Highlighted)
        })

    private fun String.isHighlighted() = highlightedValues.any { it.value == this }
}