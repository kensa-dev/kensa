package dev.kensa.attachments

/**
 * Compile-time-typed key for an [Attachments] entry. Two keys are equal iff their
 * [name] values match — the type parameter is erased at runtime, so the [name] alone
 * identifies the entry. Define keys once (top-level `val` or companion object) and
 * share them between the producer and the consumer.
 */
data class TypedKey<T : Any>(val name: String) {
    override fun toString(): String = "TypedKey($name)"
}

/**
 * Per-test typed bag for arbitrary plugin-defined data. Sits alongside
 * [dev.kensa.outputs.CapturedOutputs] and [dev.kensa.state.CapturedInteractions]
 * on the test context, but unlike them carries no framework semantics: Kensa
 * neither validates nor renders the contents. The producer puts a value under a
 * [TypedKey]; a tab renderer (or other consumer) retrieves it using the same key.
 *
 * Use this when:
 * - the framework does not need to understand the value
 * - a tab plugin owns rendering end-to-end
 * - the data is captured in-process (push-based) — for pull-based external lookup
 *   see [dev.kensa.service.logs.LogQueryService]
 *
 * First consumer: the UI testing module, which attaches captured browser screenshots
 * for the screenshots tab.
 */
class Attachments {
    private val lock = Any()
    private val values: MutableMap<TypedKey<*>, Any> = LinkedHashMap()

    fun <T : Any> put(key: TypedKey<T>, value: T) {
        synchronized(lock) { values[key] = value }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrNull(key: TypedKey<T>): T? =
        synchronized(lock) { values[key] as? T }
}
