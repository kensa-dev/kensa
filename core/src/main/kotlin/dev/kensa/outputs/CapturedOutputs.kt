package dev.kensa.outputs

import dev.kensa.util.NamedValue
import kotlin.jvm.kotlin
import kotlin.reflect.KClass

class CapturedOutput<T : Any>(val key: String, val type: KClass<T>)

class CapturedOutputs {
    private val lock = Any()
    private val values: MutableMap<String, Any> = LinkedHashMap()

    fun values(): Set<NamedValue> = values.entries.map { NamedValue(it.key, it.value) }.toSet()

    fun contains(key: String): Boolean = synchronized(lock) { values.containsKey(key) }
    fun contains(key: CapturedOutput<*>): Boolean = contains(key.key)

    fun put(key: String, value: Any) {
        synchronized(lock) {
            values[key] = value
        }
    }

    fun <T : Any> put(key: CapturedOutput<T>, value: T) {
        put(key.key, value)
    }

    operator fun <T : Any> set(key: CapturedOutput<T>, value: T) {
        put(key.key, value)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: String): T? =
        synchronized(lock) {
            values[key] as T
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

inline fun <reified T : Any> capturedOutput(key: String) = CapturedOutput(key, T::class)

@JvmName("createCapturedOutput")
fun <T : Any> createCapturedOutput(key: String, type: Class<T>): CapturedOutput<T> = CapturedOutput(key, type.kotlin)
