package dev.kensa.parse.kotlin

import dev.kensa.parse.*
import dev.kensa.sentence.Sentence
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class KotlinParserCache : ParserCache<KotlinParser.FunctionDeclarationContext> {
    override val parsedMethodCache: MutableMap<Method, ParsedMethod> = ConcurrentHashMap()
    override val declarationCache: MutableMap<KClass<*>, Triple<List<KotlinParser.FunctionDeclarationContext>, List<KotlinParser.FunctionDeclarationContext>, List<KotlinParser.FunctionDeclarationContext>>> = HashMap()
    override val fieldCache: MutableMap<KClass<*>, Map<String, FieldDescriptor>> = HashMap()
    override val parameterCache: MutableMap<Method, MethodParameters> = HashMap()
    override val testMethodSentenceCache: MutableMap<Method, List<Sentence>> = HashMap()
    override val nestedSentenceCache: MutableMap<KClass<*>, Map<String, List<Sentence>>> = HashMap()
    override val emphasisedMethodCache: MutableMap<KClass<*>, Map<String, EmphasisDescriptor>> = HashMap()
}