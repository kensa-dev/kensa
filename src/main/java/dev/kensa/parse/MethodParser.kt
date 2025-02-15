package dev.kensa.parse

import dev.kensa.Emphasise
import dev.kensa.Kensa
import dev.kensa.KensaException
import dev.kensa.Sources
import dev.kensa.parse.Accessor.ValueAccessor.ParameterAccessor
import dev.kensa.parse.Accessor.ValueAccessor
import dev.kensa.parse.Accessor.ValueAccessor.*
import dev.kensa.sentence.Sentence
import dev.kensa.util.*
import java.lang.reflect.Method
import kotlin.reflect.KClass

val greedyGenericPattern = "<.*>".toRegex()

class MethodDeclarations(val testMethods: List<MethodDeclarationContext> = emptyList(), val nestedMethods: List<MethodDeclarationContext> = emptyList(), val emphasisedMethods: List<MethodDeclarationContext> = emptyList()) {
    operator fun plus(other: MethodDeclarations) = MethodDeclarations(testMethods + other.testMethods, nestedMethods + other.nestedMethods, emphasisedMethods + other.emphasisedMethods)

    fun findTestMethodDeclaration(method: Method, toSimpleTypeName: (Class<*>) -> String): MethodDeclarationContext =
        testMethods.find(matchingDeclarationFor(method, toSimpleTypeName))
            ?: throw KensaException("Did not find method declaration for test method [${method.name}]")

    private fun matchingDeclarationFor(method: Method, toSimpleTypeName: (Class<*>) -> String) = { dc: MethodDeclarationContext ->
        method.normalisedPlatformName == dc.name &&
                // Only match on parameter simple type name - saves having to go looking in the imports
                dc.parameterNamesAndTypes.map {
                    it.second.substringAfterLast('.').replace(greedyGenericPattern, "")
                } == method.parameterTypes.map(toSimpleTypeName)
    }
}

interface MethodParser : ParserCache, ParserDelegate {

    val toSimpleTypeName: (Class<*>) -> String

    fun parse(method: Method): ParsedMethod =
        parsedMethodCache.getOrPut(method) {
            val testClass = method.declaringClass
            val actualDeclaringClass = method.actualDeclaringClass
            val classToParse = testClass.takeIf { it == actualDeclaringClass } ?: actualDeclaringClass

            val methodDeclarations = testClass.findSourcesMethodDeclarations() + classToParse.findMethodDeclarations()
            val testMethodDeclaration = methodDeclarations.findTestMethodDeclaration(method, toSimpleTypeName)

            val properties = propertyCache.getOrPut(testClass) { preparePropertiesFor(testClass) }

            val testMethodParameters = parameterCache.getOrPut(method) {
                prepareParametersFor(method, testMethodDeclaration.parameterNamesAndTypes)
            }

            val emphasisedMethods = emphasisedMethodCache.getOrPut(testClass) {
                prepareEmphasisedMethods(testClass, methodDeclarations.emphasisedMethods)
            }
            val methods = methodCache.getOrPut(testClass) { prepareMethodsFor(testClass) }

            val nestedSentences = prepareNestedSentences(testClass, methodDeclarations.nestedMethods, properties, methods, testMethodParameters)

            val testMethodSentences = prepareTestMethodSentences(
                testMethodDeclaration, properties, methods, testMethodParameters, nestedSentences, emphasisedMethods
            )

            ParsedMethod(
                method.normalisedPlatformName,
                parameterCache[method]!!,
                testMethodSentences,
                nestedSentences,
                properties,
                methods
            )
        }

    private fun Class<*>.findSourcesMethodDeclarations(): MethodDeclarations =
        MethodDeclarations().let { declarations ->
            findAnnotation<Sources>()?.value?.map { it.java }?.fold(declarations) { acc, sourceClass ->
                acc + declarationCache.getOrPut(sourceClass) { findMethodDeclarationsIn(sourceClass) }
            } ?: declarations
        }

    private fun Class<*>.findMethodDeclarations(): MethodDeclarations = declarationCache.getOrPut(this) { findMethodDeclarationsIn(this) }

    private fun prepareNestedSentences(
        testClass: Class<*>,
        nestedSentenceDeclarations: List<MethodDeclarationContext>,
        properties: Map<String, PropertyAccessor>,
        methods: Map<String, MethodAccessor>,
        testMethodParameters: MethodParameters
    ): Map<String, List<Sentence>> {
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
            }
            .associateBy({ it.first }, { it.second })
        return nestedSentenceCache[testClass] ?: emptyMap()
    }

    private fun prepareTestMethodSentences(
        testMethodDeclaration: MethodDeclarationContext,
        properties: Map<String, PropertyAccessor>,
        methods: Map<String, MethodAccessor>,
        testMethodParameters: MethodParameters,
        nestedSentences: Map<String, List<Sentence>>,
        emphasisedMethods: Map<String, EmphasisDescriptor>
    ): List<Sentence> {
        return ParserStateMachine(
            Kensa.configuration.dictionary,
            properties,
            methods,
            testMethodParameters.descriptors,
            nestedSentences,
            emphasisedMethods
        ).run {
            parse(this, testMethodDeclaration)
            sentences
        }
    }

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