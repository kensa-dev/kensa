package dev.kensa.output.json

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonValue
import com.eclipsesource.json.WriterConfig.MINIMAL
import dev.kensa.KensaException
import dev.kensa.context.TestContainer
import dev.kensa.render.Renderers
import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken
import dev.kensa.state.CapturedInteractions.Companion.sdMarkerKey
import dev.kensa.state.TestInvocation
import dev.kensa.state.TestMethodContainer
import dev.kensa.util.Attributes
import dev.kensa.util.KensaMap
import dev.kensa.util.NamedValue
import dev.kensa.util.format
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.time.Duration
import com.eclipsesource.json.Json.`object` as jsonObject

object JsonTransforms {

    fun toJsonWith(renderers: Renderers): (TestContainer) -> JsonValue = { container: TestContainer ->
        jsonObject()
            .add("testClass", container.testClass.name)
            .add("displayName", container.displayName)
            .add("state", container.state.description)
            .add("notes", container.notes)
            .add("minimumUniquePackageName", container.minimumUniquePackageName)
            .add("issues", asJsonArray(container.issues))
            .add("tests", asJsonArray(container.orderedMethodContainers) { testMethodContainer: TestMethodContainer ->
                var totalElapsed: Duration = Duration.ZERO

                val invocations = asJsonArray(testMethodContainer.invocations) { i ->
                    totalElapsed += i.elapsed

                    jsonObject()
                        .add("elapsedTime", i.elapsed.format())
                        .add("highlights", asJsonArray(i.highlightedValues, nvValueAsJson(renderers)))
                        .add("sentences", asJsonArray(i.sentences, sentenceAsJson()))
                        .add("parameters", asJsonArray(i.parameters, nvAsJson(renderers)))
                        .add("givens", asJsonArray(i.givens, givensEntryAsJson(renderers)))
                        .add("capturedInteractions", asJsonArray(i.interactions.filter { it.key != sdMarkerKey }, interactionEntryAsJson(renderers)))
                        .add("capturedOutputs", asJsonArray(i.outputs, nvAsJson(renderers)))
                        .add("fixtures", asJsonArray(i.fixtures, nvAsJson(renderers)))
                        .add("sequenceDiagram", i.sequenceDiagram?.toString())
                        .add("state", i.state.description)
                        .add("executionException", executionExceptionFrom(i))
                        .add("displayName", i.parameterizedTestDescription ?: i.displayName)
                }

                jsonObject()
                    .add("elapsedTime", totalElapsed.format())
                    .add("testMethod", testMethodContainer.method.name)
                    .add("displayName", testMethodContainer.displayName)
                    .add("notes", testMethodContainer.notes)
                    .add("issues", asJsonArray(testMethodContainer.issues))
                    .add("state", testMethodContainer.state.description)
                    .add("autoOpenTab", testMethodContainer.autoOpenTab.name)
                    .add("invocations", invocations)
            })
    }

    fun <T, R, V> ((T) -> R).andThen(other: (R) -> V): ((T) -> V) = { other(this(it)) }

    fun toIndexJson(id: String): (TestContainer) -> JsonValue = { container: TestContainer ->
        jsonObject()
            .add("id", id)
            .add("testClass", container.testClass.name)
            .add("issues", asJsonArray(container.issues))
            .add("displayName", container.displayName)
            .add("state", container.state.description)
            .add("tests", asJsonArray(container.methodContainers.values) { invocation: TestMethodContainer ->
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
                jv.writeTo(it, MINIMAL)
                it.toString()
            }
        } catch (e: IOException) {
            throw KensaException("Unable to write Json string", e)
        }
    }

    private fun sentenceAsJson(): (RenderedSentence) -> JsonValue = { sentence: RenderedSentence ->
        asJsonArray(sentence.tokens) { token: RenderedToken ->
            jsonObject().apply {
                add("types", asJsonArray(token.cssClasses))
                add("value", token.value)
                token.hint?.let { add("hint", it) }

                if (token is RenderedToken.RenderedNestedToken) {
                    add("parameterTokens", asJsonArray(token.parameterTokens) { token ->
                        jsonObject().apply {
                            add("types", asJsonArray(token.cssClasses))
                            add("value", token.value)
                            token.hint?.let { add("hint", it) }
                        }
                    })
                    add("tokens", asJsonArray(token.nestedTokens) { sentenceTokens: List<RenderedToken> ->
                        asJsonArray(sentenceTokens) { sentenceToken ->
                            jsonObject().apply {
                                add("types", asJsonArray(sentenceToken.cssClasses))
                                add("value", sentenceToken.value)
                                sentenceToken.hint?.let { add("hint", it) }
                            }
                        }
                    })
                }
            }
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