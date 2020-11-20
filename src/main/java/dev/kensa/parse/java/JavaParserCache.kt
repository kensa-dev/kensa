package dev.kensa.parse.java

import dev.kensa.parse.*
import dev.kensa.sentence.Sentence
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class JavaParserCache : ParserCache<Java8Parser.MethodDeclarationContext> {
    override val parsedMethodCache: MutableMap<Method, ParsedMethod> = ConcurrentHashMap()
    override val declarationCache: MutableMap<KClass<*>, Triple<List<Java8Parser.MethodDeclarationContext>, List<Java8Parser.MethodDeclarationContext>, List<Java8Parser.MethodDeclarationContext>>> = HashMap()
    override val fieldCache: MutableMap<KClass<*>, Map<String, FieldDescriptor>> = HashMap()
    override val parameterCache: MutableMap<Method, MethodParameters> = HashMap()
    override val testMethodSentenceCache: MutableMap<Method, List<Sentence>> = HashMap()
    override val nestedSentenceCache: MutableMap<KClass<*>, Map<String, List<Sentence>>> = HashMap()
    override val emphasisedMethodCache: MutableMap<KClass<*>, Map<String, EmphasisDescriptor>> = HashMap()
    override val methodCache: MutableMap<KClass<*>, Map<String, MethodDescriptor>> = HashMap()
}