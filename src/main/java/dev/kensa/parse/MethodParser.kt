package dev.kensa.parse

import dev.kensa.Emphasise
import dev.kensa.Kensa
import dev.kensa.KensaException
import dev.kensa.parse.Accessor.ValueAccessor.ParameterAccessor
import dev.kensa.parse.Accessor.ValueAccessor
import dev.kensa.parse.Accessor.ValueAccessor.*
import dev.kensa.util.*
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import kotlin.reflect.KClass

val greedyGenericPattern = "<.*>".toRegex()

interface MethodParser : ParserCache, ParserDelegate {
    fun parse(method: Method): ParsedMethod =
        parsedMethodCache.getOrPut(method) {
            val testClass = method.declaringClass
            val actualDeclaringClass = method.actualDeclaringClass

            val classToParse = testClass.takeIf { it == actualDeclaringClass } ?: actualDeclaringClass
            val properties = propertyCache.getOrPut(testClass) { preparePropertiesFor(testClass) }
            val (testMethodDeclarations, nestedSentenceDeclarations, emphasisedMethodDeclarations) =
                declarationCache.getOrPut(classToParse) { findMethodDeclarationsIn(classToParse) }

            val testMethodDeclaration = testMethodDeclarations.find(matchingDeclarationFor(method))
                ?: throw KensaException("Did not find method declaration for test method [${method.name}]")

            val testMethodParameters = parameterCache.getOrPut(method) {
                prepareParametersFor(
                    method,
                    testMethodDeclaration.parameterNamesAndTypes
                )
            }

            val emphasisedMethods: Map<String, EmphasisDescriptor> = emphasisedMethodCache.getOrPut(testClass) {
                prepareEmphasisedMethods(testClass, emphasisedMethodDeclarations)
            }

            val methods: Map<String, MethodAccessor> = methodCache.getOrPut(testClass) {
                prepareMethodsFor(testClass)
            }

            nestedSentenceCache[testClass] = nestedSentenceDeclarations
                .map {
                    Pair(
                        it.name,
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
                method.normalisedName,
                parameterCache[method]!!,
                testMethodSentences,
                nestedSentenceCache[testClass]!!,
                properties,
                methods
            )
        }

    fun matchingDeclarationFor(method: Method) = { dc: MethodDeclarationContext ->
        method.normalisedName == dc.name &&
            // Only match on parameter simple type name - saves having to go looking in the imports
            dc.parameterNamesAndTypes.map {
                it.second.substringAfterLast('.').replace(greedyGenericPattern, "")
            } == method.parameterTypes.map(toSimpleTypeName)
    }

    val toSimpleTypeName: (Class<*>) -> String

    private fun prepareEmphasisedMethods(
        testClass: Class<*>,
        emphasisedMethodDeclarations: List<MethodDeclarationContext>
    ): Map<String, EmphasisDescriptor> {
        return emphasisedMethodDeclarations
            .map { dc ->
                testClass.findMethod(dc.name).findAnnotation<Emphasise>()!!.run {
                    Pair(dc.name, EmphasisDescriptor(textStyles.toSet(), textColour, backgroundColor))
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
                ParameterAccessor(
                    parameter,
                    parameterNamesAndTypes[index].first,
                    index,
                )
            }.associateByTo(LinkedHashMap(), ParameterAccessor::name)
        )

    private fun prepareMethodsFor(clazz: Class<*>) =
        clazz.allMethods
            .map { MethodAccessor(it) }
            .associateBy(MethodAccessor::name)

    private fun preparePropertiesFor(clazz: Class<*>) =
        clazz.allProperties
            .flatMap { property ->
                val valueAccessor = PropertyAccessor(property)

                if (valueAccessor.isScenarioHolder) {
                    listOf(valueAccessor) + when (val classifier = property.returnType.classifier) {
                        is KClass<*> -> classifier.allProperties.map { ScenarioHolderAccessor(property, it) }.filter { it.isScenario }
                        else -> emptyList()
                    }
                } else {
                    listOf(valueAccessor)
                }
            }
            .associateBy(ValueAccessor::name)
}