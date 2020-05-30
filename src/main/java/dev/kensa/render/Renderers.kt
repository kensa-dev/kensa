package dev.kensa.render

import dev.kensa.util.NamedValue
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

class Renderers {
    private val renderers: SortedMap<KClass<*>, Renderer<Any>> = TreeMap(SubclassFirstComparator())

    fun <T : Any> add(klass: Class<T>, renderer: Renderer<out T>) {
        add(klass.kotlin, renderer)
    }

    fun <T : Any> add(klass: KClass<T>, renderer: Renderer<out T>) {
        renderers[klass] = renderer as Renderer<Any>
    }

    fun renderValueOnly(value: Any?): String = render(value)

    fun renderAll(value: Any?): Set<NamedValue> {
        return LinkedHashSet<NamedValue>().apply {
            value?.let {
                rendererFor(it::class)?.let { renderer ->
                    if (renderer is RendererWithAttributes<Any>) {
                        for (attribute in renderer.attributes()) {
                            when (val attr = attribute.renderableFrom(it)) {
                                is Map<*, *> -> {
                                    add(NamedValue(attribute.name(), attr.entries.map { entry -> NamedValue(entry.key as String, entry.value ?: "NULL") }.toSet()))
                                }
                                else -> add(NamedValue(attribute.name(), render(attr)))
                            }
                        }
                    }
                } ?: value.toString()
            } ?: add(NamedValue("value", "NULL"))
        }
    }

    private fun render(value: Any?): String = value?.let { rendererFor(value::class)?.render(value) ?: value.toString() } ?: "NULL"

    private fun rendererFor(kClass: KClass<*>): Renderer<Any>? {
        return renderers.entries
                .filter { entry -> entry.key.isSuperclassOf(kClass) }
                .map { entry -> entry.value }
                .firstOrNull()
    }

    private class SubclassFirstComparator : Comparator<KClass<*>?> {
        override fun compare(c1: KClass<*>?, c2: KClass<*>?): Int {
            if (c1 == null) {
                return if (c2 == null) 0 else 1
            } else if (c2 == null) {
                return -1
            }
            if (c1 == c2) {
                return 0
            }
            val c1Sub = c2.isSuperclassOf(c1)
            val c2Sub = c1.isSuperclassOf(c2)
            if (c1Sub && !c2Sub) {
                return -1
            } else if (c2Sub && !c1Sub) {
                return 1
            }
            return c1.qualifiedName!!.compareTo(c2.qualifiedName!!)
        }
    }
}