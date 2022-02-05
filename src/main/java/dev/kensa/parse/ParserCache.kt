package dev.kensa.parse

import dev.kensa.sentence.Sentence
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

interface ParserCache {
    val parsedMethodCache: MutableMap<Method, ParsedMethod>
    val declarationCache: MutableMap<Class<*>, Triple<List<MethodDeclarationContext>, List<MethodDeclarationContext>, List<MethodDeclarationContext>>>
    val fieldCache: MutableMap<Class<*>, Map<String, FieldDescriptor>>
    val parameterCache: MutableMap<Method, MethodParameters>
    val testMethodSentenceCache: MutableMap<Method, List<Sentence>>
    val nestedSentenceCache: MutableMap<Class<*>, Map<String, List<Sentence>>>
    val emphasisedMethodCache: MutableMap<Class<*>, Map<String, EmphasisDescriptor>>
    val methodCache: MutableMap<Class<*>, Map<String, MethodDescriptor>>
}

class RealParserCache : ParserCache {
    override val parsedMethodCache: MutableMap<Method, ParsedMethod> = ConcurrentHashMap()
    override val declarationCache: MutableMap<Class<*>, Triple<List<MethodDeclarationContext>, List<MethodDeclarationContext>, List<MethodDeclarationContext>>> = HashMap()
    override val fieldCache: MutableMap<Class<*>, Map<String, FieldDescriptor>> = HashMap()
    override val parameterCache: MutableMap<Method, MethodParameters> = HashMap()
    override val testMethodSentenceCache: MutableMap<Method, List<Sentence>> = HashMap()
    override val nestedSentenceCache: MutableMap<Class<*>, Map<String, List<Sentence>>> = HashMap()
    override val emphasisedMethodCache: MutableMap<Class<*>, Map<String, EmphasisDescriptor>> = HashMap()
    override val methodCache: MutableMap<Class<*>, Map<String, MethodDescriptor>> = HashMap()
}