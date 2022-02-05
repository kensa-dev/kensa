package dev.kensa.parse

import dev.kensa.parse.CacheState.NotCached
import dev.kensa.util.invokeMethod

class CachingFieldAccessor(private val testInstance: Any, fieldNames: Set<String>) {
    private val cachedValues: MutableMap<String, Any> = fieldNames.associateByTo(HashMap(), { it }, { NotCached })

    fun valueOf(fieldName: String): Any? =
        cachedValues.compute(fieldName) { _, existing ->
            if (existing == NotCached) testInstance.invokeMethod(fieldName) else existing
        }
}