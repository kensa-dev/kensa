package dev.kensa.parse

import dev.kensa.parse.CacheState.NotCached
import dev.kensa.parse.CacheState.NullValue
import dev.kensa.util.invokeMethod

class CachingScenarioMethodAccessor(private val testInstance: Any, private val valueAccessors: Map<String, Accessor.ValueAccessor>) {

    private val scenarioCache: MutableMap<String, Any> = valueAccessors.keys.associateByTo(HashMap(), { it }, { NotCached })
    private val valueCache: Map<String, MutableMap<String, Any?>> = valueAccessors.keys.associateByTo(HashMap(), { it }, { HashMap() })

    fun valueOf(scenarioName: String, methodName: String): Any? {
        val value = valueCache[scenarioName]?.compute(methodName) { mn, existing ->
            existing ?: scenarioInstanceWithName(scenarioName)?.let { scenario ->
                if (scenario != NullValue) scenario.invokeMethod<Any>(mn) else scenario
            } ?: NullValue
        }

        return if (value == NullValue) null else value
    }

    private fun scenarioInstanceWithName(scenarioName: String): Any? =
            scenarioCache.compute(scenarioName) { sn: String, existing: Any? ->
                if (existing == NotCached) valueAccessors[scenarioName]?.valueOfIn(testInstance) ?: NullValue else existing
            }
}