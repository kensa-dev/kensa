package dev.kensa.parse

import dev.kensa.KensaException
import dev.kensa.parse.Accessor.ParameterAccessor
import dev.kensa.parse.Accessor.ValueAccessor
import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.render.Renderers
import dev.kensa.sentence.SentenceToken
import dev.kensa.sentence.TokenType
import dev.kensa.sentence.TokenType.*
import dev.kensa.util.NamedValue
import dev.kensa.util.invokeMethod

class SentenceTokenFactory(
    private val testInstance: Any,
    private val arguments: Array<Any?>,
    private val renderers: Renderers,
    private val scenarioAccessor: CachingScenarioMethodAccessor,
    private val parameters: Map<String, ParameterAccessor>,
    private val properties: Map<String, ValueAccessor>,
    private val methods: Map<String, MethodAccessor>,
    private val highlightedValues: Set<NamedValue>
) {

    fun scenarioValueTokenFrom(token: SentenceToken) = token.value.split(".").let { split ->
        renderers.renderValue(scenarioAccessor.valueOf(split[0], split[1])).let { value ->
            SentenceToken(value, HashSet<TokenType>().apply {
                add(ScenarioValue)
                takeIf { valueIsHighlighted(value) }?.add(Highlighted)
            })
        }
    }

    fun fieldValueTokenFrom(token: SentenceToken) = properties[token.value]?.let { pd ->
        renderers.renderValue(pd.valueOfIn(testInstance)).let { value ->
            SentenceToken(value, HashSet<TokenType>().apply {
                add(FieldValue)
                takeIf { pd.isHighlight }?.add(Highlighted)
                takeIf { valueIsHighlighted(value) }?.add(Highlighted)
            })
        }
    } ?: throw KensaException("Token with type FieldValue did not refer to an actual field")

    fun methodValueTokenFrom(token: SentenceToken) = methods[token.value]?.let { md ->
        renderers.renderValue(testInstance.invokeMethod<Any>(md.method)).let { value ->
            SentenceToken(value, HashSet<TokenType>().apply {
                add(MethodValue)
                takeIf { md.isHighlight }?.add(Highlighted)
                takeIf { valueIsHighlighted(value) }?.add(Highlighted)
            })
        }
    } ?: throw KensaException("Token with type MethodValue did not refer to an actual method")

    fun parameterValueTokenFrom(token: SentenceToken) = parameters[token.value]?.let { pd ->
        renderers.renderValue(arguments[pd.index]).let { value ->
            SentenceToken(value, HashSet<TokenType>().apply {
                add(ParameterValue)
                takeIf { pd.isHighlight }?.add(Highlighted)
                takeIf { valueIsHighlighted(value) }?.add(Highlighted)
            })
        }
    } ?: throw KensaException("Token with type ParameterValue did not refer to an actual parameter")

    private fun valueIsHighlighted(value: String) = highlightedValues.any { it.value == value }
}