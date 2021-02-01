package dev.kensa.parse

import dev.kensa.*
import dev.kensa.util.Reflect
import org.antlr.v4.runtime.tree.ParseTree
import java.lang.reflect.Method
import kotlin.reflect.KClass

val greedyGenericPattern = "<.*>".toRegex()

interface MethodParser<DC : ParseTree> : ParserCache<DC>, ParserDelegate<DC> {
    fun parse(method: Method): ParsedMethod =
        parsedMethodCache.getOrPut(method) {
            val testClass = method.declaringClass.kotlin
            val properties = fieldCache.getOrPut(testClass) {
                prepareFieldsFor(testClass)
            }
            val (testMethodDeclarations, nestedSentenceDeclarations, emphasisedMethodDeclarations) =
                declarationCache.getOrPut(testClass) { findMethodDeclarationsIn(testClass) }
            val testMethodDeclaration = testMethodDeclarations.find(matchingDeclarationFor(method))
                ?: throw KensaException("Did not find method declaration for test method [${method.name}]")

            val testMethodParameters = parameterCache.getOrPut(method) {
                prepareParametersFor(
                    method,
                    parameterNamesAndTypesFrom(testMethodDeclaration)
                )
            }

            val emphasisedMethods: Map<String, EmphasisDescriptor> = emphasisedMethodCache.getOrPut(testClass) {
                prepareEmphasisedMethods(testClass, emphasisedMethodDeclarations)
            }

            val methods: Map<String, MethodDescriptor> = methodCache.getOrPut(testClass) {
                prepareMethodsFor(testClass)
            }

            nestedSentenceCache[testClass] = nestedSentenceDeclarations
                .map {
                    Pair(
                        methodNameFrom(it),
                        ParserStateMachine(
                            Kensa.configuration.dictionary,
                            properties,
                            methods,
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
                methods,
                testMethodParameters.descriptors,
                nestedSentenceCache[testClass] ?: emptyMap(),
                emphasisedMethods
            ).run {
                parse(this, testMethodDeclaration)
                sentences
            }

            ParsedMethod(
                method.name,
                parameterCache[method]!!,
                testMethodSentences,
                nestedSentenceCache[testClass]!!,
                properties,
                methods
            )
        }

    fun matchingDeclarationFor(method: Method) = { dc: DC ->
        methodNameFrom(dc) == method.name &&
                // Only match on parameter simple type name - saves having to go looking in the imports
                parameterNamesAndTypesFrom(dc).map {
                    it.second.substringAfterLast('.').replace(greedyGenericPattern, "")
                } == method.parameterTypes.map(toSimpleTypeName)
    }

    val toSimpleTypeName: (Class<*>) -> String

    private fun prepareEmphasisedMethods(
        testClass: KClass<*>,
        emphasisedMethodDeclarations: List<DC>
    ): Map<String, EmphasisDescriptor> {
        return emphasisedMethodDeclarations
            .map { dc ->
                val methodName = methodNameFrom(dc)
                Reflect.findAnnotation<Emphasise>(Reflect.findMethod(methodName, testClass))!!.run {
                    Pair(methodName, EmphasisDescriptor(textStyles.toSet(), textColour, backgroundColor))
                }
            }
            .associateBy({ it.first }, { it.second })
    }

    private fun prepareParametersFor(
        method: Method,
        parameterNamesAndTypes: List<Pair<String, String>>
    ): MethodParameters =
        MethodParameters(
            method.parameters.mapIndexed { index, parameter ->
                ParameterDescriptor(
                    parameterNamesAndTypes[index].first,
                    index,
                    Reflect.hasAnnotation<SentenceValue>(parameter),
                    Reflect.hasAnnotation<Highlight>(parameter)
                )
            }.associateByTo(LinkedHashMap(), ParameterDescriptor::name)
        )

    private fun prepareMethodsFor(clazz: KClass<*>) =
        Reflect.methodsOf(clazz.java)
            .map {
                MethodDescriptor(
                    it.name,
                    it,
                    Reflect.hasAnnotation<SentenceValue>(it),
                    Reflect.hasAnnotation<Highlight>(it)
                )
            }
            .associateBy(MethodDescriptor::name)

    private fun prepareFieldsFor(clazz: KClass<*>) =
        Reflect.fieldsOf(clazz.java)
            .map {
                FieldDescriptor(
                    it.name,
                    it,
                    Reflect.hasAnnotation<SentenceValue>(it),
                    Reflect.hasAnnotation<Highlight>(it),
                    Reflect.hasAnnotation<Scenario>(it)
                )
            }
            .associateBy(FieldDescriptor::name)
}