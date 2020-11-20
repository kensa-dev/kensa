package dev.kensa.parse

import dev.kensa.sentence.Sentence
import java.lang.reflect.Method
import kotlin.reflect.KClass

interface ParserCache<DC> {
    val parsedMethodCache: MutableMap<Method, ParsedMethod>
    val declarationCache: MutableMap<KClass<*>, Triple<List<DC>, List<DC>, List<DC>>>
    val fieldCache: MutableMap<KClass<*>, Map<String, FieldDescriptor>>
    val parameterCache: MutableMap<Method, MethodParameters>
    val testMethodSentenceCache: MutableMap<Method, List<Sentence>>
    val nestedSentenceCache: MutableMap<KClass<*>, Map<String, List<Sentence>>>
    val emphasisedMethodCache: MutableMap<KClass<*>, Map<String, EmphasisDescriptor>>
    val methodCache: MutableMap<KClass<*>, Map<String, MethodDescriptor>>
}