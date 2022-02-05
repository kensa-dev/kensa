package dev.kensa.parse

import dev.kensa.*
import dev.kensa.util.*
import java.lang.reflect.Method
import java.lang.reflect.Parameter

val greedyGenericPattern = "<.*>".toRegex()

interface MethodParser : ParserCache, ParserDelegate {
    fun parse(method: Method): ParsedMethod =
        parsedMethodCache.getOrPut(method) {
            val testClass = method.declaringClass
            val actualDeclaringClass = method.actualDeclaringClass

            val classToParse = testClass.takeIf { it == actualDeclaringClass } ?: actualDeclaringClass
            val properties = fieldCache.getOrPut(testClass) { prepareFieldsFor(testClass) }
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

            val methods: Map<String, MethodDescriptor> = methodCache.getOrPut(testClass) {
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
                findAnnotation<Emphasise>(testClass.findMethod(dc.name))!!.run {
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
                ParameterDescriptor(
                    parameterNamesAndTypes[index].first,
                    index,
                    hasAnnotation<SentenceValue>(parameter),
                    hasAnnotation<Highlight>(parameter),
                    shouldRender(parameter),
                )
            }.associateByTo(LinkedHashMap(), ParameterDescriptor::name)
        )

    fun shouldRender(parameter: Parameter) =
        findAnnotation<CapturedParameter>(parameter.type)?.value ?: true ||
                findAnnotation<CapturedParameter>(parameter.type)?.value ?: true

    private fun prepareMethodsFor(clazz: Class<*>) =
        clazz.allMethods()
            .map {
                MethodDescriptor(
                    it.name,
                    it,
                    hasAnnotation<SentenceValue>(it),
                    hasAnnotation<Highlight>(it)
                )
            }
            .associateBy(MethodDescriptor::name)

    private fun prepareFieldsFor(clazz: Class<*>) =
        clazz.allFields()
            .map {
                FieldDescriptor(
                    it.name,
                    it,
                    hasAnnotation<SentenceValue>(it),
                    hasAnnotation<Highlight>(it),
                    hasAnnotation<Scenario>(it)
                )
            }
            .associateBy(FieldDescriptor::name)
}