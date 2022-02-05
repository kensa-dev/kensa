package dev.kensa.parse

import dev.kensa.sentence.Sentence
import java.lang.reflect.Method
import kotlin.reflect.KClass

interface ParserCache<DC> {
    val parsedMethodCache: MutableMap<Method, ParsedMethod>
    val declarationCache: MutableMap<Class<*>, Triple<List<DC>, List<DC>, List<DC>>>
    val fieldCache: MutableMap<Class<*>, Map<String, FieldDescriptor>>
    val parameterCache: MutableMap<Method, MethodParameters>
    val testMethodSentenceCache: MutableMap<Method, List<Sentence>>
    val nestedSentenceCache: MutableMap<Class<*>, Map<String, List<Sentence>>>
    val emphasisedMethodCache: MutableMap<Class<*>, Map<String, EmphasisDescriptor>>
    val methodCache: MutableMap<Class<*>, Map<String, MethodDescriptor>>
}