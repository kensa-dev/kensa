package dev.kensa.tabs

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class DefaultKensaTabServices : KensaTabServices {

    private val factories = ConcurrentHashMap<KClass<*>, () -> Any>()
    private val instances = ConcurrentHashMap<KClass<*>, Any>()

    fun <T : Any> register(type: KClass<T>, factory: () -> T) {
        factories[type] = factory
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getOrNull(type: KClass<T>): T? {
        val existing = instances[type]
        if (existing != null) return existing as T

        val factory = factories[type] ?: return null
        val created = instances.computeIfAbsent(type) { factory() }
        return created as T
    }
}