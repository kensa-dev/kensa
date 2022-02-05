package dev.kensa.parse

import dev.kensa.KensaException
import dev.kensa.render.Renderers
import dev.kensa.sentence.SentenceToken
import dev.kensa.sentence.TokenType
import dev.kensa.sentence.TokenType.*
import dev.kensa.util.NamedValue
import dev.kensa.util.Reflect

class SentenceTokenFactory(
        private val testInstance: Any,
        private val arguments: Array<Any?>,
        private val renderers: Renderers,
        private val scenarioAccessor: CachingScenarioMethodAccessor,
        private val parameters: Map<String, ParameterDescriptor>,
        private val fields: Map<String, FieldDescriptor>,
        private val methods: Map<String, MethodDescriptor>,
        private val highlightedValues: Set<NamedValue>
) {

    fun scenarioValueTokenFrom(token: SentenceToken) = token.value.split(".").let { split ->
        renderers.renderValueOnly(scenarioAccessor.valueOf(split[0], split[1])).let { value ->
            SentenceToken(value, HashSet<TokenType>().apply {
                add(ScenarioValue)
                takeIf { valueIsHighlighted(value) }?.add(Highlighted)
            })
        }
    }

    fun fieldValueTokenFrom(token: SentenceToken) = fields[token.value]?.let { fd ->
        renderers.renderValueOnly(Reflect.fieldValue<Any>(fd.field, testInstance)).let { value ->
            SentenceToken(value, HashSet<TokenType>().apply {
                add(FieldValue)
                takeIf { fd.isHighlighted }?.add(Highlighted)
                takeIf { valueIsHighlighted(value) }?.add(Highlighted)
            })
        }
    } ?: throw KensaException("Token with type FieldValue did not refer to an actual field")

    fun methodValueTokenFrom(token: SentenceToken) = methods[token.value]?.let { md ->
        renderers.renderValueOnly(Reflect.invokeMethod<Any>(md.method, testInstance)).let { value ->
            SentenceToken(value, HashSet<TokenType>().apply {
                add(MethodValue)
                takeIf { md.isHighlighted }?.add(Highlighted)
                takeIf { valueIsHighlighted(value) }?.add(Highlighted)
            })
        }
    } ?: throw KensaException("Token with type MethodValue did not refer to an actual method")

    fun parameterValueTokenFrom(token: SentenceToken) = parameters[token.value]?.let { pd ->
        renderers.renderValueOnly(arguments[pd.index]).let { value ->
            SentenceToken(value, HashSet<TokenType>().apply {
                add(ParameterValue)
                takeIf { pd.isHighlighted }?.add(Highlighted)
                takeIf { valueIsHighlighted(value) }?.add(Highlighted)
            })
        }
    } ?: throw KensaException("Token with type ParameterValue did not refer to an actual parameter")

    private fun valueIsHighlighted(value: String) = highlightedValues.any { it.value == value }
}