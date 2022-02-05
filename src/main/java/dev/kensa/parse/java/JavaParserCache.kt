package dev.kensa.parse.java

import dev.kensa.parse.*
import dev.kensa.sentence.Sentence
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

class JavaParserCache : ParserCache<Java8Parser.MethodDeclarationContext> {
    override val parsedMethodCache: MutableMap<Method, ParsedMethod> = ConcurrentHashMap()
    override val declarationCache: MutableMap<Class<*>, Triple<List<Java8Parser.MethodDeclarationContext>, List<Java8Parser.MethodDeclarationContext>, List<Java8Parser.MethodDeclarationContext>>> = HashMap()
    override val fieldCache: MutableMap<Class<*>, Map<String, FieldDescriptor>> = HashMap()
    override val parameterCache: MutableMap<Method, MethodParameters> = HashMap()
    override val testMethodSentenceCache: MutableMap<Method, List<Sentence>> = HashMap()
    override val nestedSentenceCache: MutableMap<Class<*>, Map<String, List<Sentence>>> = HashMap()
    override val emphasisedMethodCache: MutableMap<Class<*>, Map<String, EmphasisDescriptor>> = HashMap()
    override val methodCache: MutableMap<Class<*>, Map<String, MethodDescriptor>> = HashMap()
}