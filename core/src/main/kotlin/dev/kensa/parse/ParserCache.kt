package dev.kensa.parse

import dev.kensa.sentence.TemplateSentence
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

interface ParserCache {
    val parsedMethodCache: ConcurrentHashMap<Method, ParsedMethod>
    val declarationCache: MutableMap<Class<*>, MethodDeclarations>
    val propertyCache: MutableMap<Class<*>, Map<String, ElementDescriptor>>
    val parameterCache: MutableMap<Method, MethodParameters>
    val testMethodSentenceCache: MutableMap<Method, List<TemplateSentence>>
    val nestedMethodCache: MutableMap<Class<*>, Map<String, ParsedNestedMethod>>
    val emphasisedMethodCache: MutableMap<Class<*>, Map<String, EmphasisDescriptor>>
    val methodCache: MutableMap<Class<*>, Map<String, ElementDescriptor>>
}

class RealParserCache : ParserCache {
    override val parsedMethodCache: ConcurrentHashMap<Method, ParsedMethod> = ConcurrentHashMap()
    override val declarationCache: MutableMap<Class<*>, MethodDeclarations> = HashMap()
    override val propertyCache: MutableMap<Class<*>, Map<String, ElementDescriptor>> = HashMap()
    override val parameterCache: MutableMap<Method, MethodParameters> = HashMap()
    override val testMethodSentenceCache: MutableMap<Method, List<TemplateSentence>> = HashMap()
    override val nestedMethodCache: MutableMap<Class<*>, Map<String, ParsedNestedMethod>> = HashMap()
    override val emphasisedMethodCache: MutableMap<Class<*>, Map<String, EmphasisDescriptor>> = HashMap()
    override val methodCache: MutableMap<Class<*>, Map<String, ElementDescriptor>> = HashMap()
}