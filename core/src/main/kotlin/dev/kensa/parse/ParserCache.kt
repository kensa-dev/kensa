package dev.kensa.parse

import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.parse.Accessor.ValueAccessor.PropertyAccessor
import dev.kensa.sentence.Sentence
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

interface ParserCache {
    val parsedMethodCache: MutableMap<Method, ParsedMethod>
    val declarationCache: MutableMap<Class<*>, MethodDeclarations>
    val propertyCache: MutableMap<Class<*>, Map<String, PropertyAccessor>>
    val parameterCache: MutableMap<Method, MethodParameters>
    val testMethodSentenceCache: MutableMap<Method, List<Sentence>>
    val nestedSentenceCache: MutableMap<Class<*>, Map<String, List<Sentence>>>
    val emphasisedMethodCache: MutableMap<Class<*>, Map<String, EmphasisDescriptor>>
    val methodCache: MutableMap<Class<*>, Map<String, MethodAccessor>>
}

class RealParserCache : ParserCache {
    override val parsedMethodCache: MutableMap<Method, ParsedMethod> = ConcurrentHashMap()
    override val declarationCache: MutableMap<Class<*>, MethodDeclarations> = HashMap()
    override val propertyCache: MutableMap<Class<*>, Map<String, PropertyAccessor>> = HashMap()
    override val parameterCache: MutableMap<Method, MethodParameters> = HashMap()
    override val testMethodSentenceCache: MutableMap<Method, List<Sentence>> = HashMap()
    override val nestedSentenceCache: MutableMap<Class<*>, Map<String, List<Sentence>>> = HashMap()
    override val emphasisedMethodCache: MutableMap<Class<*>, Map<String, EmphasisDescriptor>> = HashMap()
    override val methodCache: MutableMap<Class<*>, Map<String, MethodAccessor>> = HashMap()
}