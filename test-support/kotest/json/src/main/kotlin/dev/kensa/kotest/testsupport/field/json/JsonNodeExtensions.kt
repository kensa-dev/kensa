package dev.kensa.kotest.testsupport.field.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

/** Returns the textual value at [path] (a JSONPointer), or `null` if absent or non-textual. */
fun JsonNode.textValueOrNullAt(path: String): String? = at(path).textValue()

/** Returns the integer value at [path] (a JSONPointer), or `null` if absent or non-numeric. */
fun JsonNode.intValueOrNullAt(path: String): Int? = at(path)?.takeIf { it.isNumber }?.asText()?.toInt()

/** Returns the long value at [path] (a JSONPointer), or `null` if absent or non-numeric. */
fun JsonNode.longValueOrNullAt(path: String): Long? = at(path)?.takeIf { it.isNumber }?.asText()?.toLong()

/**
 * Returns the boolean value at [path] (a JSONPointer). Accepts either a JSON boolean node or a
 * textual `"true"` / `"false"`. Returns `null` for any other type or if the path is absent.
 */
fun JsonNode.booleanValueOrNullAt(path: String): Boolean? = at(path).let {
    when {
        it.isBoolean -> it.booleanValue()
        it.isTextual -> it.textValue().toBoolean()
        else -> null
    }
}

/** Returns the double value at [path] (a JSONPointer), or `null` if absent or non-numeric. */
fun JsonNode.doubleValueOrNullAt(path: String): Double? = at(path)?.takeIf { it.isNumber }?.asText()?.toDouble()

/** Returns the [JsonNode] at [path] (a JSONPointer), or `null` if absent or null-valued. */
fun JsonNode.nodeOrNullAt(path: String): JsonNode? = at(path).takeUnless { it.isNull || it.isMissingNode }

/**
 * Returns the [ArrayNode] at [path] (a JSONPointer).
 *
 * @throws IllegalArgumentException if the path is absent or does not refer to an array.
 */
fun JsonNode.arrayNodeAt(path: String): ArrayNode = nodeOrNullAt(path) as? ArrayNode
    ?: throw IllegalArgumentException("No array node for $path")

/**
 * Reads a node via [nodeAccessor] then applies [targetTransform]; both stages may return `null`.
 *
 * Used internally by the `transformX` factory functions; exposed for users composing their own
 * extractors.
 */
fun <NODE_TYPE : Any, TARGET : Any> JsonNode.transform(nodeAccessor: (JsonNode) -> NODE_TYPE?, targetTransform: (NODE_TYPE) -> TARGET?): TARGET? =
    nodeAccessor(this)?.let(targetTransform)

internal fun ArrayNode.associate(keyName: String): Map<String, JsonNode> =
    filter { it.at("/$keyName").isTextual }.associateBy { it.at("/$keyName").textValue() }
