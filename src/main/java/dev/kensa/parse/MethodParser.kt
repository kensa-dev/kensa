package dev.kensa.parse

import dev.kensa.*
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
                val (testMethodDeclarations, nestedSentenceDeclarations) = declarationCache.getOrPut(testClass) { findMethodDeclarationsIn(testClass) }
                val testMethodDeclaration = testMethodDeclarations.find { dc ->
                    // Only match on parameter simple type name - saves having to go looking in the imports
                    methodNameFrom(dc) == method.name && parameterNamesAndTypesFrom(dc).map { it.second } == method.parameterTypes.map { it.simpleName }
                } ?: throw KensaException("Did not find method declaration for test method [${method.name}]")

                val testMethodParameters = parameterCache.getOrPut(method) { prepareParametersFor(method, parameterNamesAndTypesFrom(testMethodDeclaration)) }

                nestedSentenceCache[testClass] = nestedSentenceDeclarations
                        .map {
                            Pair(
                                    methodNameFrom(it),
                                    ParserStateMachine(
                                            Kensa.configuration.dictionary,
                                            properties,
                                            testMethodParameters.descriptors
                                    ).run {
                                        parse(this, it)
                                        sentences
                                    }
                            )
                        }.associateBy({ it.first }, { it.second })

                val testMethodSentences = ParserStateMachine(
                        Kensa.configuration.dictionary,
                        properties,
                        testMethodParameters.descriptors,
                        nestedSentenceCache[testClass] ?: emptyMap()
                ).run {
                    parse(this, testMethodDeclaration)
                    sentences
                }

                ParsedMethod(method.name, parameterCache[method]!!, testMethodSentences, nestedSentenceCache[testClass]!!, properties)
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