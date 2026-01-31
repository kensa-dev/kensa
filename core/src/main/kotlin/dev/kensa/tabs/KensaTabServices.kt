package dev.kensa.tabs

import kotlin.reflect.KClass

/**
 * A registry of shared services available during tab generation.
 *
 * Services are typically scoped to a single report generation run (or a single test container)
 * and may perform expensive work (e.g. indexing logs) once and then serve cheap queries.
 */
interface KensaTabServices {
    /**
     * Returns a service instance for the given type, or null if not registered.
     */
    fun <T : Any> getOrNull(type: KClass<T>): T?

    /**
     * Returns a service instance for the given type, or throws if not registered.
     */
    fun <T : Any> get(type: KClass<T>): T = getOrNull(type) ?: error("No service registered for ${type.qualifiedName}")
}
