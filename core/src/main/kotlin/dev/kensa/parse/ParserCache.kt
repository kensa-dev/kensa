package dev.kensa.parse

import dev.kensa.RenderingDirectives
import dev.kensa.parse.ElementDescriptor.MethodElementDescriptor
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

internal fun <K : Any, V> ConcurrentHashMap<K, CompletableFuture<V>>.memoize(
    key: K,
    provider: () -> V,
): V {
    this[key]?.let { return it.join() }
    val future = CompletableFuture<V>()
    val existing = putIfAbsent(key, future)
    if (existing != null) return existing.join()
    return try {
        provider().also { future.complete(it) }
    } catch (t: Throwable) {
        remove(key, future)
        future.completeExceptionally(t)
        throw t
    }
}

class ParserCache {
    private val parsedMethods = ConcurrentHashMap<Method, CompletableFuture<ParsedMethod>>()
    private val methodDeclarations = ConcurrentHashMap<Class<*>, CompletableFuture<MethodDeclarations>>()
    private val expandableMethods = ConcurrentHashMap<Class<*>, CompletableFuture<Map<String, ParsedExpandableMethod>>>()
    private val properties = ConcurrentHashMap<Class<*>, Map<String, ElementDescriptor>>()
    private val parameters = ConcurrentHashMap<Method, MethodParameters>()
    private val methods = ConcurrentHashMap<Class<*>, Map<String, MethodElementDescriptor>>()
    private val renderingDirectives = ConcurrentHashMap<Class<*>, RenderingDirectives>()

    fun getOrPutParsedMethod(method: Method, provider: () -> ParsedMethod): ParsedMethod =
        parsedMethods.memoize(method, provider)

    fun getOrPutMethodDeclarations(clazz: Class<*>, provider: () -> MethodDeclarations): MethodDeclarations =
        methodDeclarations.memoize(clazz, provider)

    fun getOrPutExpandableMethods(clazz: Class<*>, provider: () -> Map<String, ParsedExpandableMethod>): Map<String, ParsedExpandableMethod> =
        expandableMethods.memoize(clazz, provider)

    fun getOrPutProperties(clazz: Class<*>, provider: () -> Map<String, ElementDescriptor>): Map<String, ElementDescriptor> =
        properties.computeIfAbsent(clazz) { provider() }

    fun getOrPutParameters(method: Method, provider: () -> MethodParameters): MethodParameters =
        parameters.computeIfAbsent(method) { provider() }

    fun getOrPutMethods(clazz: Class<*>, provider: () -> Map<String, MethodElementDescriptor>): Map<String, MethodElementDescriptor> =
        methods.computeIfAbsent(clazz) { provider() }

    fun getOrPutRenderingDirectives(clazz: Class<*>, provider: () -> RenderingDirectives): RenderingDirectives =
        renderingDirectives.computeIfAbsent(clazz) { provider() }
}