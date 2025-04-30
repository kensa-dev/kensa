package dev.kensa.parse

import dev.kensa.KensaException
import dev.kensa.parse.Accessor.ValueAccessor.ParameterAccessor
import dev.kensa.parse.Accessor.ValueAccessor
import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.render.Renderers
import dev.kensa.sentence.SentenceToken
import dev.kensa.sentence.TokenType.*
import dev.kensa.util.invokeMethod

class SentenceTokenFactory(
    private val testInstance: Any,
    private val arguments: Array<Any?>,
    private val renderers: Renderers,
    private val scenarioAccessor: CachingScenarioMethodAccessor,
    private val parameters: Map<String, ParameterAccessor>,
    private val properties: Map<String, ValueAccessor>,
    private val methods: Map<String, MethodAccessor>,
    private val highlights: HighlightDescriptors
) {
    fun scenarioValueTokenFrom(token: SentenceToken) = token.value.split(".").let { split ->
        renderers.renderValue(scenarioAccessor.valueOf(split[0], split[1])).let { value ->
            SentenceToken(value, setOf(ScenarioValue), highlight = highlights.highlightDescriptorFor(value))
        }
    }

    fun fieldValueTokenFrom(token: SentenceToken) = properties[token.value]?.let { pd ->
        renderers.renderValue(pd.valueOfIn(testInstance)).let { value ->
            SentenceToken(value, setOf(FieldValue), highlight = highlights.highlightDescriptorFor(value, pd.name))
        }
    } ?: throw KensaException("Token with type FieldValue did not refer to an actual field")

    fun methodValueTokenFrom(token: SentenceToken) = methods[token.value]?.let { md ->
        renderers.renderValue(testInstance.invokeMethod<Any>(md.method)).let { value ->
            SentenceToken(value, setOf(MethodValue), highlight = highlights.highlightDescriptorFor(value, md.name))
        }
    } ?: throw KensaException("Token with type MethodValue did not refer to an actual method")

    fun parameterValueTokenFrom(token: SentenceToken) = parameters[token.value]?.let { pd ->
        renderers.renderValue(arguments[pd.index]).let { value ->
            SentenceToken(value, setOf(ParameterValue), highlight = highlights.highlightDescriptorFor(value, pd.name))
        }
    } ?: throw KensaException("Token with type ParameterValue did not refer to an actual parameter")
}