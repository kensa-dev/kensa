package dev.kensa.render

import dev.kensa.render.Language.PlainText
import dev.kensa.util.Attributes
import dev.kensa.util.SubclassFirstComparator
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

data class ListRendererFormat(val separator: String = ", ", val prefix: String = "[", val postfix: String = "]")

class Renderers {
    private val defaultListRenderer = HeterogeneousListRenderer()
    private val defaultTableRenderer = DefaultTableRenderer()
    private val valueRenderers: SortedMap<KClass<*>, ValueRenderer<Any>> = TreeMap(SubclassFirstComparator())
    private val tableRenderers: SortedMap<KClass<*>, TableRenderer<Any>> = TreeMap(SubclassFirstComparator())
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

    fun renderTable(value: Any): List<List<Any?>> =
        tableRendererFor(value::class)?.render(value) ?: defaultTableRenderer.render(value)

    private fun tableRendererFor(kClass: KClass<*>): TableRenderer<Any>? =
        tableRenderers.entries
            .filter { entry -> entry.key.isSuperclassOf(kClass) }
            .map { entry -> entry.value }
            .firstOrNull()

    fun renderInteraction(value: Any, attributes: Attributes): List<RenderedInteraction> = interactionRendererFor(value::class)?.render(value, attributes) ?: listOf(RenderedInteraction("Value", value.toString(), attributes.getOrDefault("language", PlainText)))

    fun renderInteractionAttributes(value: Any): List<RenderedAttributes> = interactionRendererFor(value::class)?.renderAttributes(value) ?: emptyList()

    private fun interactionRendererFor(kClass: KClass<*>): InteractionRenderer<Any>? =
        interactionRenderers.entries
            .filter { entry -> entry.key.isSuperclassOf(kClass) }
            .map { entry -> entry.value }
            .firstOrNull()

    private inner class HeterogeneousListRenderer : ValueRenderer<List<*>> {
        override fun render(value: List<*>): String = value.joinToString(separator = listRendererFormat.separator, prefix = listRendererFormat.prefix, postfix = listRendererFormat.postfix) { renderValue(it) }
    }

    private class DefaultTableRenderer : TableRenderer<Any> {
        override fun render(value: Any): List<List<Any?>> =
            when (value) {
                is Iterable<*> -> value.map { item ->
                    when (item) {
                        is Pair<*, *> -> listOf(item.first, item.second)
                        is Triple<*, *, *> -> listOf(item.first, item.second, item.third)
                        is Iterable<*> -> item.toList()
                        is Array<*> -> item.toList()
                        else -> listOf(item)
                    }
                }
                is Array<*> -> value.map { item ->
                    when (item) {
                        is Pair<*, *> -> listOf(item.first, item.second)
                        is Triple<*, *, *> -> listOf(item.first, item.second, item.third)
                        is Array<*> -> item.toList()
                        else -> listOf(item)
                    }
                }
                else -> listOf(listOf(value))
            }
    }
}