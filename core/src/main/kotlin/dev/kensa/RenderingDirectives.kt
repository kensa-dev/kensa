package dev.kensa

import dev.kensa.util.SubclassFirstComparator
import java.util.TreeMap
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf

/**
 * Lookup of rendering directives by target class, with hierarchy-aware resolution:
 * a directive declared for a supertype (or interface) applies to every subtype unless a
 * more specific directive exists. The most specific match wins.
 */
class RenderingDirectives(entries: Map<KClass<*>, RenderingDirective> = emptyMap()) {
    private val sorted: TreeMap<KClass<*>, RenderingDirective> =
        TreeMap<KClass<*>, RenderingDirective>(SubclassFirstComparator()).apply { putAll(entries) }

    operator fun get(kClass: KClass<*>?): RenderingDirective? =
        kClass?.let { target -> sorted.entries.firstOrNull { (declared, _) -> declared.isSuperclassOf(target) }?.value }

    fun appliesTo(kClass: KClass<*>?): Boolean = get(kClass) != null
}