package dev.kensa.util

import java.util.*

class Attributes(val attributes: Set<NamedValue>) : Iterable<NamedValue> {

    val isEmpty: Boolean
        get() = attributes.isEmpty()

    override fun iterator(): Iterator<NamedValue> {
        return attributes.iterator()
    }

    companion object {
        private val EMPTY_ATTRIBUTES = Attributes(emptySet())

        @JvmStatic
        fun emptyAttributes(): Attributes = EMPTY_ATTRIBUTES

        @JvmStatic
        fun of(name: String, value: Any): Attributes = of(NamedValue(name, value))

        @JvmStatic
        fun of(name1: String, value1: Any, name2: String, value2: Any): Attributes {
            return of(
                    NamedValue(name1, value1),
                    NamedValue(name2, value2)
            )
        }

        @JvmStatic
        fun of(name1: String, value1: Any, name2: String, value2: Any, name3: String, value3: Any): Attributes {
            return of(
                    NamedValue(name1, value1),
                    NamedValue(name2, value2),
                    NamedValue(name3, value3)
            )
        }

        @JvmStatic
        fun of(attributes: Set<NamedValue>): Attributes {
            return Attributes(LinkedHashSet(attributes))
        }

        @JvmStatic
        fun of(vararg attributes: NamedValue): Attributes {
            return Attributes(LinkedHashSet(Arrays.asList(*attributes)))
        }
    }

}