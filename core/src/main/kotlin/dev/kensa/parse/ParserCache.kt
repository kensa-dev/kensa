package dev.kensa.parse

import dev.kensa.parse.ElementDescriptor.MethodElementDescriptor
import dev.kensa.sentence.TemplateSentence
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

class ParserCache {
    private val parsedMethods = ConcurrentHashMap<Method, ParsedMethod>()
    private val methodDeclarations = ConcurrentHashMap<Class<*>, MethodDeclarations>()
    private val properties = ConcurrentHashMap<Class<*>, Map<String, ElementDescriptor>>()
    private val parameters = ConcurrentHashMap<Method, MethodParameters>()
    private val nestedMethods = ConcurrentHashMap<Class<*>, Map<String, ParsedNestedMethod>>()
    private val emphasisedMethods = ConcurrentHashMap<Class<*>, Map<String, EmphasisDescriptor>>()
    private val methods = ConcurrentHashMap<Class<*>, Map<String, MethodElementDescriptor>>()

    fun getOrPutParsedMethod(method: Method, provider: () -> ParsedMethod): ParsedMethod =
        parsedMethods.computeIfAbsent(method) { provider() }

    fun getOrPutMethodDeclarations(clazz: Class<*>, provider: () -> MethodDeclarations): MethodDeclarations =
        methodDeclarations.computeIfAbsent(clazz) { provider() }

    fun getOrPutProperties(clazz: Class<*>, provider: () -> Map<String, ElementDescriptor>): Map<String, ElementDescriptor> =
        properties.computeIfAbsent(clazz) { provider() }

    fun getOrPutParameters(method: Method, provider: () -> MethodParameters): MethodParameters =
        parameters.computeIfAbsent(method) { provider() }

    fun getOrPutNestedMethods(clazz: Class<*>, provider: () -> Map<String, ParsedNestedMethod>): Map<String, ParsedNestedMethod> =
        nestedMethods.computeIfAbsent(clazz) { provider() }

    fun getOrPutEmphasisedMethods(clazz: Class<*>, provider: () -> Map<String, EmphasisDescriptor>): Map<String, EmphasisDescriptor> =
        emphasisedMethods.computeIfAbsent(clazz) { provider() }

    fun getOrPutMethods(clazz: Class<*>, provider: () -> Map<String, MethodElementDescriptor>): Map<String, MethodElementDescriptor> =
        methods.computeIfAbsent(clazz) { provider() }
}