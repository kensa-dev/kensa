package dev.kensa.parse

import dev.kensa.RenderingDirectives
import dev.kensa.parse.ElementDescriptor.MethodElementDescriptor
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

class ParserCache {
    private val parsedMethods = ConcurrentHashMap<Method, ParsedMethod>()
    private val methodDeclarations = ConcurrentHashMap<Class<*>, MethodDeclarations>()
    private val properties = ConcurrentHashMap<Class<*>, Map<String, ElementDescriptor>>()
    private val parameters = ConcurrentHashMap<Method, MethodParameters>()
    private val expandableMethods = ConcurrentHashMap<Class<*>, Map<String, ParsedExpandableMethod>>()
    private val methods = ConcurrentHashMap<Class<*>, Map<String, MethodElementDescriptor>>()
    private val renderingDirectives = ConcurrentHashMap<Class<*>, RenderingDirectives>()

    fun getOrPutParsedMethod(method: Method, provider: () -> ParsedMethod): ParsedMethod =
        parsedMethods.computeIfAbsent(method) { provider() }

    fun getOrPutMethodDeclarations(clazz: Class<*>, provider: () -> MethodDeclarations): MethodDeclarations =
        methodDeclarations.computeIfAbsent(clazz) { provider() }

    fun getOrPutProperties(clazz: Class<*>, provider: () -> Map<String, ElementDescriptor>): Map<String, ElementDescriptor> =
        properties.computeIfAbsent(clazz) { provider() }

    fun getOrPutParameters(method: Method, provider: () -> MethodParameters): MethodParameters =
        parameters.computeIfAbsent(method) { provider() }

    fun getOrPutExpandableMethods(clazz: Class<*>, provider: () -> Map<String, ParsedExpandableMethod>): Map<String, ParsedExpandableMethod> =
        expandableMethods.computeIfAbsent(clazz) { provider() }

    fun getOrPutMethods(clazz: Class<*>, provider: () -> Map<String, MethodElementDescriptor>): Map<String, MethodElementDescriptor> =
        methods.computeIfAbsent(clazz) { provider() }

    fun getOrPutRenderingDirectives(clazz: Class<*>, provider: () -> RenderingDirectives): RenderingDirectives =
        renderingDirectives.computeIfAbsent(clazz) { provider() }
}