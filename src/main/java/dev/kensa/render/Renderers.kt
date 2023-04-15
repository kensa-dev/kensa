package dev.kensa.render

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

data class ListRendererFormat(val separator: String = ", ", val prefix: String = "[", val postfix: String = "]")

class Renderers {
    private val defaultListRenderer = HeterogeneousListRenderer()
    private val valueRenderers: SortedMap<KClass<*>, ValueRenderer<Any>> = TreeMap(SubclassFirstComparator())
    private val interactionRenderers: SortedMap<KClass<*>, InteractionRenderer<Any>> = TreeMap(SubclassFirstComparator())
    private var listRenderer: ValueRenderer<List<*>> = defaultListRenderer
    private var listRendererFormat = ListRendererFormat()

    fun setListRendererFormat(format: ListRendererFormat) {
        listRendererFormat = format
    }

    fun setListRenderer(renderer: ValueRenderer<List<*>>) {
        listRenderer = renderer
    }

    fun <T : Any> addValueRenderer(klass: Class<T>, renderer: ValueRenderer<out T>) {
        addValueRenderer(klass.kotlin, renderer)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> addValueRenderer(klass: KClass<T>, renderer: ValueRenderer<out T>) {
        valueRenderers[klass] = renderer as ValueRenderer<Any>
    }

    fun <T : Any> addInteractionRenderer(klass: Class<T>, renderer: InteractionRenderer<out T>) {
        addInteractionRenderer(klass.kotlin, renderer)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> addInteractionRenderer(klass: KClass<T>, renderer: InteractionRenderer<out T>) {
        interactionRenderers[klass] = renderer as InteractionRenderer<Any>
    }

    fun renderValue(value: Any?): String = value?.let {
        if (it is List<*>) {
            listRenderer.render(it)
        } else {
            valueRendererFor(value::class)?.render(value) ?: value.toString()
        }
    } ?: "NULL"

    private fun valueRendererFor(kClass: KClass<*>): ValueRenderer<Any>? =
        valueRenderers.entries
            .filter { entry -> entry.key.isSuperclassOf(kClass) }
            .map { entry -> entry.value }
            .firstOrNull()

    fun renderInteraction(value: Any): List<RenderedInteraction> = interactionRendererFor(value::class)?.render(value) ?: listOf(RenderedInteraction("Undefined Value", value.toString()))

    fun renderInteractionAttributes(value: Any): List<RenderedAttributes> = interactionRendererFor(value::class)?.renderAttributes(value) ?: emptyList()

    private fun interactionRendererFor(kClass: KClass<*>): InteractionRenderer<Any>? =
        interactionRenderers.entries
            .filter { entry -> entry.key.isSuperclassOf(kClass) }
            .map { entry -> entry.value }
            .firstOrNull()

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

    private inner class HeterogeneousListRenderer : ValueRenderer<List<*>> {
        override fun render(value: List<*>): String = value.joinToString(separator = listRendererFormat.separator, prefix = listRendererFormat.prefix, postfix = listRendererFormat.postfix) { renderValue(it) }
    }
}