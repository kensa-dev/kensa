package dev.kensa.output.json

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonValue
import com.eclipsesource.json.WriterConfig
import dev.kensa.KensaException
import dev.kensa.context.TestContainer
import dev.kensa.render.Renderers
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceToken
import dev.kensa.state.CapturedInteractions.Companion.sdMarkerKey
import dev.kensa.state.TestInvocation
import dev.kensa.state.TestMethodContainer
import dev.kensa.util.Attributes
import dev.kensa.util.DurationFormatter
import dev.kensa.util.KensaMap
import dev.kensa.util.NamedValue
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Duration
import com.eclipsesource.json.Json.`object` as jsonObject

object JsonTransforms {

    fun toJsonWith(renderers: Renderers): (TestContainer) -> JsonValue = { container: TestContainer ->
        jsonObject()
            .add("testClass", container.testClass.name)
            .add("displayName", container.classDisplayName)
            .add("state", container.state.description)
            .add("notes", container.notes)
            .add("issues", asJsonArray(container.issues))
            .add("tests", asJsonArray(container.methods.values) { invocation: TestMethodContainer ->
                var totalElapsed: Duration = Duration.ZERO

                val invocations = asJsonArray(invocation.invocations) { i ->
                    totalElapsed += i.elapsed

                    jsonObject()
                        .add("elapsedTime", DurationFormatter.format(i.elapsed))
                        .add("highlights", asJsonArray(i.highlightedValues, nvValueAsJson(renderers)))
                        .add("sentences", asJsonArray(i.sentences, sentenceAsJson()))
                        .add("parameters", asJsonArray(i.parameters, nvAsJson(renderers)))
                        .add("givens", asJsonArray(i.givens, givensEntryAsJson(renderers)))
                        .add("capturedInteractions", asJsonArray(i.interactions.filter { it.key != sdMarkerKey }, interactionEntryAsJson(renderers)))
                        .add("sequenceDiagram", i.sequenceDiagram?.toString())
                        .add("state", i.state.description)
                        .add("executionException", executionExceptionFrom(i))
                        .add("displayName", i.parameterizedTestDescription ?: i.displayName)
                }

                jsonObject()
                    .add("elapsedTime", DurationFormatter.format(totalElapsed))
                    .add("testMethod", invocation.method.name)
                    .add("displayName", invocation.displayName)
                    .add("notes", invocation.notes)
                    .add("issues", asJsonArray(invocation.issues))
                    .add("state", invocation.state.description)
                    .add("autoOpenTab", invocation.autoOpenTab.name)
                    .add("invocations", invocations)
            })
    }

    // Overload rangeTo to allow chaining of function calls
    operator fun <T, R, V> ((T) -> R).rangeTo(other: (R) -> V): ((T) -> V) = {
        other(this(it))
    }

    fun toIndexJson(id: String): (TestContainer) -> JsonValue = { container: TestContainer ->
        jsonObject()
            .add("id", id)
            .add("testClass", container.testClass.name)
            .add("issues", asJsonArray(container.issues))
            .add("displayName", container.classDisplayName)
            .add("state", container.state.description)
            .add("tests", asJsonArray(container.methods.values) { invocation: TestMethodContainer ->
                jsonObject()
                    .add("testMethod", invocation.method.name)
                    .add("issues", asJsonArray(invocation.issues))
                    .add("displayName", invocation.displayName)
                    .add("state", invocation.state.description)
            })
    }

    fun toJsonString(): (JsonValue) -> String = { jv: JsonValue ->
        try {
            StringWriter().let {
                jv.writeTo(it, WriterConfig.MINIMAL)
                it.toString()
            }
        } catch (e: IOException) {
            throw KensaException("Unable to write Json string", e)
        }
    }

    private fun sentenceAsJson(): (Sentence) -> JsonValue = { sentence: Sentence ->
        asJsonArray(sentence.squashedTokens) { token: SentenceToken ->
            jsonObject()
                .add("types", asJsonArray(token.cssClasses))
                .add("value", token.value)
                .add("tokens", asJsonArray(token.nestedTokens) { sentenceTokens: List<SentenceToken> ->
                    asJsonArray(sentenceTokens) { sentenceToken ->
                        jsonObject()
                            .add("types", asJsonArray(sentenceToken.cssClasses))
                            .add("value", sentenceToken.value)
                    }
                })
        }
    }

    private fun asJsonArray(collection: Collection<String>) = asJsonArray(collection) { string: String -> Json.value(string) }

    private fun <T> asJsonArray(collection: Collection<T>, transformer: (T) -> JsonValue?): JsonArray = Json.array().apply {
        collection.mapNotNull(transformer)
            .forEach { add(it) }
    }

    private fun <T> asJsonArray(sequence: Sequence<T>, transformer: (T) -> JsonValue?): JsonArray = Json.array().apply {
        sequence.mapNotNull(transformer)
            .forEach { add(it) }
    }

    private fun givensEntryAsJson(renderers: Renderers): (KensaMap.Entry) -> JsonValue = { entry: KensaMap.Entry -> jsonObject().add(entry.key, renderers.renderValue(entry.value)) }

    private fun interactionEntryAsJson(renderers: Renderers): (KensaMap.Entry) -> JsonValue? = { entry ->
        entry.takeUnless {
            it.key.matches("^\\{.+}.*$".toRegex())
        }?.let {
            jsonObject()
                .add("id", it.key.hashCode().toString())
                .add("name", it.key)
                .add("rendered", renderedInteractionAsJson(it.value!!, it.attributes, renderers))
                .add("attributes", asJsonArray(it.attributes, entryAsJson(renderers)))
        }
    }

    private fun renderedInteractionAsJson(value: Any, attributes: Attributes, renderers: Renderers): JsonValue =
        jsonObject()
            .add("values", renderedInteractionValuesAsJson(value, attributes, renderers))
            .add("attributes", renderedInteractionAttributesAsJson(value, renderers))

    private fun renderedInteractionValuesAsJson(value: Any, attributes: Attributes, renderers: Renderers): JsonArray =
        renderers.renderInteraction(value, attributes).fold(Json.array()) { parent, ri ->
            parent.add(
                jsonObject().add("name", ri.name)
                    .add("value", ri.value)
                    .add("language", ri.language.value)
            )
        }

    private fun renderedInteractionAttributesAsJson(value: Any, renderers: Renderers): JsonArray =
        renderers.renderInteractionAttributes(value).fold(Json.array()) { parent, ra ->
            parent.add(
                jsonObject().add("name", ra.name)
                    .add("attributes", ra.attributes.fold(Json.array()) { array, nv ->
                        array.add(nv.asJson(renderers))
                    })
            )
        }

    private fun nvValueAsJson(renderers: Renderers) = { nv: NamedValue -> Json.value(renderers.renderValue(nv.value)) }

    private fun NamedValue.asJson(renderers: Renderers) = jsonObject().add(name, renderers.renderValue(value))
    private fun nvAsJson(renderers: Renderers) = { nv: NamedValue -> jsonObject().add(nv.name, renderers.renderValue(nv.value)) }

    private fun entryAsJson(renderers: Renderers) = { e: Map.Entry<String, *> -> jsonObject().add(e.key, renderers.renderValue(e.value)) }

    private fun executionExceptionFrom(invocation: TestInvocation) =
        invocation.executionException?.let {
            jsonObject()
                .add("message", it.message)
                .add("stackTrace", toString(it))
        } ?: jsonObject()

    private fun toString(throwable: Throwable) = StringWriter().let { out ->
        throwable.printStackTrace(PrintWriter(out))
        out.toString()
    }
}