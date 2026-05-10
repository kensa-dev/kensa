package dev.kensa.hamkrest.testsupport.field.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import dev.kensa.hamkrest.testsupport.field.MatcherField

/**
 * A [MatcherField] backed by a Jackson [JsonNode] and a JSONPointer [path] (e.g. `/customer/name`).
 *
 * The path is exposed publicly so tooling (e.g. report rendering) can introspect it. Most users
 * will reach for one of the typed subclasses ([JsonTextField], [JsonIntField], …) rather than
 * constructing this directly.
 *
 * ```
 * val anOrderId = JsonField("/order/id") { node, p -> node.at(p).textValue() }
 * ```
 */
open class JsonField<TARGET>(val path: String, private val fn: (JsonNode, String) -> TARGET?) : MatcherField<JsonNode, TARGET> {
    override val name: String = path
    override val description: String get() = path
    override fun extract(value: JsonNode): TARGET? = fn(value, path)
}

/** A [JsonField] returning the textual value at [path], or `null` if absent or non-textual. */
open class JsonTextField(path: String) : JsonField<String>(path, transformString { it })

/** A [JsonField] returning the integer value at [path], or `null` if absent or non-numeric. */
open class JsonIntField(path: String) : JsonField<Int>(path, transformInt { it })

/** A [JsonField] returning the long value at [path], or `null` if absent or non-numeric. */
open class JsonLongField(path: String) : JsonField<Long>(path, transformLong { it })

/** A [JsonField] returning the boolean value at [path], or `null` if absent or non-boolean. */
open class JsonBooleanField(path: String) : JsonField<Boolean>(path, transformBoolean { it })

/** A [JsonField] returning the [JsonNode] at [path], or `null` if absent or null-valued. */
open class JsonNodeField(path: String) : JsonField<JsonNode>(path, { n, p -> n.nodeOrNullAt(p) })

/** A [JsonField] returning the [ArrayNode] at [path], or `null` if absent or not an array. */
open class ArrayNodeField(path: String) : JsonField<ArrayNode>(path, { n, p -> n.nodeOrNullAt(p) as? ArrayNode })

/**
 * A [MatcherField] that extracts a `Map<String, JsonNode>` from an array node at [path], keying
 * each element by its `keyName` text attribute.
 *
 * ```
 * // input: { "items": [ { "code": "A", … }, { "code": "B", … } ] }
 * val items = JsonMapField("/items", "code")
 * // items.extract(json) → { "A" -> {...}, "B" -> {...} }
 * ```
 */
open class JsonMapField(val path: String, private val keyName: String) : MatcherField<JsonNode, Map<String, JsonNode>> {
    override val name: String = path
    override val description: String get() = path
    override fun extract(value: JsonNode): Map<String, JsonNode>? = (value.at(path) as? ArrayNode)?.associate(keyName)
}

/** Marker interface for fields/selectors that identify a key within a [JsonMapField] result. */
interface JsonMapKey {
    val keyName: String
}

/**
 * A [MatcherField] that selects [keyName] from a `Map<String, JsonNode>` (typically extracted by
 * a [JsonMapField]) and applies [fn] to the value.
 */
open class JsonMapKeyField<TARGET>(override val keyName: String, private val fn: (JsonNode) -> TARGET?) : JsonMapKey, MatcherField<Map<String, JsonNode>, TARGET> {
    override val name: String get() = keyName
    override fun extract(value: Map<String, JsonNode>): TARGET? = value[keyName]?.let(fn)
}

/**
 * Builds an extractor for [JsonField] that reads the textual value at the field's path and
 * applies [targetTransform] to produce a typed value.
 */
fun <TARGET : Any> transformString(targetTransform: (String) -> TARGET?): (JsonNode, String) -> TARGET? =
    { node, path -> node.transform({ it.textValueOrNullAt(path) }, targetTransform) }

/**
 * Builds an extractor for [JsonField] that reads the integer value at the field's path and
 * applies [targetTransform] to produce a typed value.
 */
fun <TARGET : Any> transformInt(targetTransform: (Int) -> TARGET?): (JsonNode, String) -> TARGET? =
    { node, path -> node.transform({ it.intValueOrNullAt(path) }, targetTransform) }

/**
 * Builds an extractor for [JsonField] that reads the long value at the field's path and applies
 * [targetTransform] to produce a typed value.
 */
fun <TARGET : Any> transformLong(targetTransform: (Long) -> TARGET?): (JsonNode, String) -> TARGET? =
    { node, path -> node.transform({ it.longValueOrNullAt(path) }, targetTransform) }

/**
 * Builds an extractor for [JsonField] that reads the boolean value at the field's path and
 * applies [targetTransform] to produce a typed value.
 */
fun <TARGET : Any> transformBoolean(targetTransform: (Boolean) -> TARGET?): (JsonNode, String) -> TARGET? =
    { node, path -> node.transform({ it.booleanValueOrNullAt(path) }, targetTransform) }

/**
 * Builds an extractor for [JsonField] that reads the double value at the field's path and applies
 * [targetTransform] to produce a typed value.
 */
fun <TARGET : Any> transformDouble(targetTransform: (Double) -> TARGET?): (JsonNode, String) -> TARGET? =
    { node, path -> node.transform({ it.doubleValueOrNullAt(path) }, targetTransform) }
