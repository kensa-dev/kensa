package dev.kensa.outputs

import dev.kensa.util.NamedValue
import kotlin.jvm.kotlin
import kotlin.reflect.KClass

class CapturedOutput<T : Any>(val key: String, val type: KClass<T>, val highlighted: Boolean = false)

class CapturedOutputs {
    private val lock = Any()
    private val values: MutableMap<String, Any> = LinkedHashMap()
    private val highlightedKeys: MutableSet<String> = LinkedHashSet()

    fun values(): Set<NamedValue> = synchronized(lock) { values.entries.map { NamedValue(it.key, it.value) }.toSet() }

    fun highlightedValues(): Set<NamedValue> = synchronized(lock) {
        values.entries
            .filter { it.key in highlightedKeys }
            .map { NamedValue(it.key, it.value) }
            .toSet()
    }

    fun contains(key: String): Boolean = synchronized(lock) { values.containsKey(key) }
    fun contains(key: CapturedOutput<*>): Boolean = contains(key.key)

    fun put(key: String, value: Any) {
        synchronized(lock) {
            values[key] = value
        }
    }

    fun <T : Any> put(key: CapturedOutput<T>, value: T) {
        synchronized(lock) {
            values[key.key] = value
            if (key.highlighted) highlightedKeys.add(key.key)
        }
    }

    operator fun <T : Any> set(key: CapturedOutput<T>, value: T) {
        put(key, value)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: String): T? =
        synchronized(lock) {
            values[key] as? T
        }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: CapturedOutput<T>): T? =
        synchronized(lock) {
            values[key.key]?.also {
                require(key.type.isInstance(it)) {
                    "Value for key '${key.key}' is of type ${it::class.simpleName} but was requested as ${key.type.simpleName}"
                }
            } as? T
        }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: String): T = getOrNull(key) ?: throw NoSuchElementException("No captured output for key '$key'")

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: CapturedOutput<T>): T = getOrNull(key) ?: throw NoSuchElementException("No captured output for key '${key.key}'")
}

inline fun <reified T : Any> capturedOutput(key: String, highlighted: Boolean = false) = CapturedOutput(key, T::class, highlighted)

@JvmName("createCapturedOutput")
fun <T : Any> createCapturedOutput(key: String, type: Class<T>): CapturedOutput<T> = CapturedOutput(key, type.kotlin)

@JvmName("createCapturedOutput")
fun <T : Any> createCapturedOutput(key: String, type: Class<T>, highlighted: Boolean): CapturedOutput<T> = CapturedOutput(key, type.kotlin, highlighted)
