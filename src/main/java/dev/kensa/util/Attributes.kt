package dev.kensa.util

import dev.kensa.render.diagram.directive.ArrowStyle
import dev.kensa.render.diagram.directive.ArrowStyle.UmlSynchronous
import dev.kensa.util.Attributes.Key.Arrow
import dev.kensa.util.Attributes.Key.Group

class Attributes private constructor(private val attributes: Map<String, Any?>) : Sequence<Map.Entry<String, Any?>> {
    object Key {
        const val Group = "Group"
        const val Arrow = "ArrowStyle"
    }

    val isEmpty: Boolean
        get() = attributes.isEmpty()

    @PublishedApi
    internal fun doGet(key: String): Any? = attributes[key]

    inline fun <reified T> get(key: String) : T? = doGet(key) as T?

    inline fun <reified T> getOrDefault(key: String, default: T) : T = get<T>(key) ?: default

    val group: String?
        get() = get<String>(Group)

    val arrowStyle: ArrowStyle
        get() = getOrDefault(Arrow, UmlSynchronous)

    fun merge(other: Attributes): Attributes =
            Attributes(
                    (asSequence() + other.asSequence())
                            .distinct()
                            .associateByTo(LinkedHashMap(), { it.key }, { it.value })
            )

    override fun iterator(): Iterator<Map.Entry<String, Any?>> = attributes.entries.iterator()

    companion object {

        private val EMPTY_ATTRIBUTES = Attributes(linkedMapOf())

        @JvmStatic
        fun emptyAttributes(): Attributes = EMPTY_ATTRIBUTES

        @JvmStatic
        fun of(name: String, value: Any): Attributes = of(name to value)

        @JvmStatic
        fun of(name1: String, value1: Any, name2: String, value2: Any): Attributes =
                of(name1 to value1, name2 to value2)

        @JvmStatic
        fun of(name1: String, value1: Any, name2: String, value2: Any, name3: String, value3: Any): Attributes =
                of(name1 to value1, name2 to value2, name3 to value3)

        @JvmStatic
        fun of(vararg attributes: Pair<String, Any?>): Attributes = Attributes(linkedMapOf(*attributes))
    }
}