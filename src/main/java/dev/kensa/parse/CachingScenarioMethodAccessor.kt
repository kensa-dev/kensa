package dev.kensa.parse

import dev.kensa.parse.CacheState.NotCached
import dev.kensa.parse.CacheState.NullValue
import dev.kensa.util.Reflect
import java.util.*

class CachingScenarioMethodAccessor(private val testInstance: Any, scenarioNames: Set<String>) {

    private val scenarioCache: MutableMap<String, Any>
    private val valueCache: Map<String, MutableMap<String, Any?>>

    fun valueOf(scenarioName: String, methodName: String): Any? {
        val value = valueCache[scenarioName]?.compute(methodName) { mn: String, existing: Any? ->
            existing ?: scenarioInstanceWithName(scenarioName)?.let { target ->
                if (target !== NullValue) {
                    Reflect.invokeMethod<Any>(mn, target)
                } else target
            } ?: NullValue
        }

        return if (value == NullValue) null else value
    }

    private fun scenarioInstanceWithName(scenarioName: String): Any? =
            scenarioCache.compute(scenarioName) { sn: String, existing: Any? ->
                if (existing === NotCached) {
                    Reflect.fieldValue<Any>(sn, testInstance) ?: NullValue
                } else existing
            }

    init {
        scenarioCache = scenarioNames.associateByTo(HashMap(), { it }, { NotCached })
        valueCache = scenarioNames.associateByTo(HashMap(), { it }, { HashMap<String, Any?>() })
    }
}