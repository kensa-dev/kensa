package dev.kensa.util

import dev.kensa.util.Attributes.Companion.emptyAttributes
import java.util.*

abstract class KensaMap<M : KensaMap<M>> {

    private val lock = Any()
    private val values: MutableMap<String, Entry> = LinkedHashMap()

    fun put(value: Any, timestamp: Long = System.currentTimeMillis()): M = putWithUniqueKey(value.javaClass.simpleName + "__idx__", value,timestamp, emptyAttributes())

    @JvmOverloads
    fun put(key: String, value: Any?, timestamp: Long = System.currentTimeMillis(), attributes: Attributes = emptyAttributes()): M = self().apply {
        synchronized(lock) { values[key] = Entry(key, value, timestamp, attributes) }
    }

    internal fun putWithUniqueKey(key: String, value: Any?, timestamp: Long = System.currentTimeMillis(), attributes: Attributes = emptyAttributes()): M = self().apply {
        fun baseKeyFrom(result: MatchResult) = result.groups["prefix"]?.value + result.groups["suffix"]?.value
        fun indexedKeyFrom(result: MatchResult, index: Int) = (result.groups["prefix"]?.value + (result.groups["prekey"]?.value + index +
                if (result.groups["suffix"]?.value?.isNotBlank() == true) result.groups["postkey"]?.value else "") + result.groups["suffix"]?.value)

        KEY_REGEX.matchEntire(key)?.let { result ->
            synchronized(lock) {
                var realKey = baseKeyFrom(result)
                if (values.containsKey(realKey)) {
                    var i = 1
                    do {
                        realKey = indexedKeyFrom(result, i++)
                    } while (values.containsKey(realKey))
                }
                values[realKey] = Entry(realKey, value, timestamp, attributes)
            }
        } ?: throw IllegalArgumentException("Must specify __key__ placeholder")
    }

    fun putAll(values: Collection<Any>) {
        synchronized(lock) { values.forEach { value -> this.put(value) } }
    }

    fun putNamedValues(values: Collection<NamedValue>) {
        synchronized(lock) { values.forEach { nv: NamedValue -> this.put(nv.name, nv.value) } }
    }

    operator fun <T> get(key: String): T? =
            synchronized(lock) {
                values[key]?.let { entry ->
                    @Suppress("UNCHECKED_CAST")
                    entry.value as T
                }
            }

    fun containsKey(key: String): Boolean = synchronized(lock) { values.containsKey(key) }

    fun entrySet(): Set<Entry> = synchronized(lock) { LinkedHashSet(values.values.sortedBy(Entry::timestamp)) }

    @Suppress("UNCHECKED_CAST")
    private fun self(): M = this as M

    class Entry(val key: String, val value: Any?, val timestamp: Long, val attributes: Attributes = emptyAttributes())

    companion object {
        private val KEY_REGEX = "(?<prefix>.*)(?<key>__(?<prekey>[ ]*)idx(?<postkey>[ ]*)__)(?<suffix>.*)".toRegex()
    }
}