package dev.kensa.parse

import dev.kensa.parse.CacheState.NotCached
import dev.kensa.util.Reflect

class CachingFieldAccessor(private val testInstance: Any, fieldNames: Set<String>) {
    private val cachedValues: MutableMap<String, Any>

    fun valueOf(fieldName: String): Any? {
        return cachedValues.compute(fieldName) { fn, existing ->
            if (existing == NotCached) {
                Reflect.fieldValue<Any>(fieldName, testInstance)
            } else {
                existing
            }
        }
    }

    init {
        cachedValues = fieldNames.associateByTo(HashMap(), { it }, { NotCached })
    }
}