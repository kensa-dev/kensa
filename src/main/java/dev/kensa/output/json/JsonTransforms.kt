package dev.kensa.output.json

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonArray
import com.eclipsesource.json.JsonValue
import com.eclipsesource.json.WriterConfig
import dev.kensa.KensaException
import dev.kensa.context.TestContainer
import dev.kensa.render.Renderers
import dev.kensa.sentence.Acronym
import dev.kensa.sentence.Sentence
import dev.kensa.sentence.SentenceToken
import dev.kensa.state.TestInvocation
import dev.kensa.state.TestMethodInvocation
import dev.kensa.util.DurationFormatter
import dev.kensa.util.KensaMap
import dev.kensa.util.NamedValue
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import com.eclipsesource.json.Json.`object` as jsonObject

object JsonTransforms {

    fun toJsonWith(renderers: Renderers): (TestContainer) -> JsonValue {
        return { container: TestContainer ->
            jsonObject()
                    .add("testClass", container.testClass.name)
                    .add("displayName", container.displayName)
                    .add("state", container.state.description)
                    .add("notes", container.notes)
                    .add("issue", asJsonArray(container.issues))
                    .add("tests", asJsonArray(container.invocations.values) { invocation: TestMethodInvocation ->
                        jsonObject()
                                .add("testMethod", invocation.method.name)
                                .add("displayName", invocation.displayName)
                                .add("notes", invocation.notes)
                                .add("issue", asJsonArray(invocation.issues))
                                .add("state", invocation.state.description)
                                .add("invocations", asJsonArray(invocation.invocations) { i ->
                                    jsonObject()
                                            .add("elapsedTime", DurationFormatter.format(i.elapsed))
                                            .add("highlights", asJsonArray(i.highlightedValues, nvpValueAsJson(renderers)))
                                            .add("acronyms", acronymsAsJson(i.acronyms))
                                            .add("sentences", asJsonArray(i.sentences, sentenceAsJson()))
                                            .add("parameters", asJsonArray(i.parameters, nvAsJson(renderers)))
                                            .add("givens", asJsonArray(i.givens, givensEntryAsJson(renderers)))
                                            .add("capturedInteractions", asJsonArray(i.interactions, interactionEntryAsJson(renderers)))
                                            .add("sequenceDiagram", if (i.sequenceDiagram == null) null else i.sequenceDiagram.toString())
                                            .add("state", i.state.description)
                                            .add("executionException", executionExceptionFrom(i))
                                })
                    })
        }
    }

    // Overload rangeTo to allow chaining of function calls
    operator fun <T, R, V> ((T) -> R).rangeTo(other: (R) -> V): ((T) -> V) = {
        other(this(it))
    }

    fun toIndexJson(id: String): (TestContainer) -> JsonValue {
        return { container: TestContainer ->
            jsonObject()
                    .add("id", id)
                    .add("testClass", container.testClass.name)
                    .add("displayName", container.displayName)
                    .add("state", container.state.description)
                    .add("tests", asJsonArray(container.invocations.values) { invocation: TestMethodInvocation ->
                        jsonObject()
                                .add("testMethod", invocation.method.name)
                                .add("displayName", invocation.displayName)
                                .add("state", invocation.state.description)
                    })
        }
    }

    fun toJsonString(): (JsonValue) -> String {
        return { jv: JsonValue ->
            try {
                StringWriter().let {
                    jv.writeTo(it, WriterConfig.MINIMAL)
                    it.toString()
                }
            } catch (e: IOException) {
                throw KensaException("Unable to write Json string", e)
            }
        }
    }

    private fun sentenceAsJson(): (Sentence) -> JsonValue {
        return { sentence: Sentence ->
            asJsonArray(sentence.squashedTokens) { token: SentenceToken ->
                jsonObject()
                        .add("types", asJsonArray(token.tokenTypeNames))
                        .add("value", token.value)
                        .add("tokens", asJsonArray(token.nestedTokens) { sentenceTokens: List<SentenceToken> ->
                            asJsonArray(sentenceTokens) { sentenceToken ->
                                jsonObject()
                                        .add("types", asJsonArray(sentenceToken.tokenTypeNames))
                                        .add("value", sentenceToken.value)
                            }
                        })
            }
        }
    }

    private fun asJsonArray(collection: Collection<String>) = asJsonArray(collection) { string: String -> Json.value(string) }

    private fun acronymsAsJson(collection: Collection<Acronym>) =
            jsonObject().apply {
                collection.forEach {
                    add(it.acronym, it.meaning)
                }
            }

    private fun <T> asJsonArray(collection: Collection<T>, transformer: (T) -> JsonValue): JsonArray {
        return Json.array().apply {
            collection.map(transformer)
                    .forEach {
                        add(it)
                    }
        }
    }

    private fun givensEntryAsJson(renderers: Renderers): (KensaMap.Entry) -> JsonValue = { entry: KensaMap.Entry -> jsonObject().add(entry.key, renderers.renderValueOnly(entry.value)) }

    private fun interactionEntryAsJson(renderers: Renderers): (KensaMap.Entry) -> JsonValue {
        return { entry: KensaMap.Entry ->
            jsonObject()
                    .add("id", entry.key.hashCode().toString())
                    .add("name", entry.key)
                    .add("value", renderers.renderValueOnly(entry.value))
                    .add("renderables", asJsonArray(renderers.renderAll(entry.value), renderablesAsJson()))
                    .add("attributes", asJsonArray(entry.attributes.attributes, nvAsJson(renderers)))
        }
    }

    private fun renderablesAsJson(): (NamedValue) -> JsonValue {
        return { nv: NamedValue ->
            when (val value = nv.value) {
                is Set<*> -> jsonObject().add(nv.name, asJsonArray(value as Set<NamedValue>, renderablesAsJson()))

                else -> jsonObject().add(nv.name, nv.value.toString())
            }
        }
    }

    private fun nvpValueAsJson(renderers: Renderers) = { nv: NamedValue -> Json.value(renderers.renderValueOnly(nv.value)) }

    private fun nvAsJson(renderers: Renderers) = { nv: NamedValue -> jsonObject().add(nv.name, renderers.renderValueOnly(nv.value)) }

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