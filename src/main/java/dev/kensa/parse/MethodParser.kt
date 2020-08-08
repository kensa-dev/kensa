package dev.kensa.parse

import dev.kensa.Highlight
import dev.kensa.Kensa
import dev.kensa.Scenario
import dev.kensa.SentenceValue
import dev.kensa.util.Reflect
import org.antlr.v4.runtime.tree.ParseTree
import java.lang.reflect.Method
import kotlin.reflect.KClass

interface MethodParser<DC : ParseTree> : ParserCache<DC>, ParserDelegate<DC> {
    fun parse(method: Method): ParsedMethod =
            parsedMethodCache.getOrPut(method) {
                val testClass = method.declaringClass.kotlin
                val properties = fieldCache.getOrPut(testClass) {
                    prepareFieldsFor(testClass)
                }
                val (testMethodDeclarations, _) = declarationCache.getOrPut(testClass) {
                    findMethodDeclarationsIn(testClass).apply {
                        nestedSentenceCache[testClass] = second.map { dc ->
                            Pair(
                                    methodNameFrom(dc),
                                    ParserStateMachine(Kensa.configuration.dictionary, properties).run {
                                        parse(this, dc)
                                        sentences
                                    }
                            )
                        }.associateBy({ it.first }, { it.second })
                    }
                }
                val sentences = testMethodSentenceCache.getOrPut(method) {
                    testMethodDeclarations
                            .filter { dc ->
                                // Only match on parameter simple type name - saves having to go looking in the imports
                                methodNameFrom(dc) == method.name && parameterNamesAndTypesFrom(dc).map { it.second } == method.parameterTypes.map { it.simpleName }
                            }
                            .map { Pair(it, parameterCache.getOrPut(method) { prepareParametersFor(method, parameterNamesAndTypesFrom(it)) }) }
                            .flatMap { it ->
                                ParserStateMachine(
                                        Kensa.configuration.dictionary,
                                        properties,
                                        it.second.descriptors,
                                        nestedSentenceCache[testClass] ?: emptyMap()
                                ).run {
                                    parse(this, it.first)
                                    sentences
                                }
                            }
                }
                ParsedMethod(method.name, parameterCache[method]!!, sentences, nestedSentenceCache[testClass]!!, properties)
            }

    private fun prepareParametersFor(method: Method, parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters =
            MethodParameters(
                    method.parameters.mapIndexed { index, parameter ->
                        ParameterDescriptor(parameterNamesAndTypes[index].first, index, Reflect.hasAnnotation<SentenceValue>(parameter), Reflect.hasAnnotation<Highlight>(parameter))
                    }.associateByTo(LinkedHashMap(), ParameterDescriptor::name)
            )

    private fun prepareFieldsFor(clazz: KClass<*>) =
            Reflect.fieldsOf(clazz.java)
                    .map { FieldDescriptor(it.name, it, Reflect.hasAnnotation<SentenceValue>(it), Reflect.hasAnnotation<Highlight>(it), Reflect.hasAnnotation<Scenario>(it)) }
                    .associateBy(FieldDescriptor::name)
}