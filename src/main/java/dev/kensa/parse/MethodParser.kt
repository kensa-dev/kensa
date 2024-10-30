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

interface MethodParser : ParserCache, ParserDelegate {
    fun parse(method: Method): ParsedMethod =
        parsedMethodCache.getOrPut(method) {
            val testClass = method.declaringClass
            val actualDeclaringClass = method.actualDeclaringClass

            val (testMethodDeclarations, nestedSentenceDeclarations, emphasisedMethodDeclarations) = findAndAggregateMethodDeclarations(testClass)

            val classToParse = testClass.takeIf { it == actualDeclaringClass } ?: actualDeclaringClass
            val properties = propertyCache.getOrPut(testClass) { preparePropertiesFor(testClass) }

            addMethodDeclarations(classToParse, testMethodDeclarations, nestedSentenceDeclarations, emphasisedMethodDeclarations)

            val testMethodDeclaration = findTestMethodDeclaration(method, testMethodDeclarations)
            val testMethodParameters = parameterCache.getOrPut(method) {
                prepareParametersFor(method, testMethodDeclaration.parameterNamesAndTypes)
            }

            val emphasisedMethods = emphasisedMethodCache.getOrPut(testClass) {
                prepareEmphasisedMethods(testClass, emphasisedMethodDeclarations)
            }
            val methods = methodCache.getOrPut(testClass) { prepareMethodsFor(testClass) }

            val nestedSentences = prepareNestedSentences(testClass, nestedSentenceDeclarations, properties, methods, testMethodParameters)

            val testMethodSentences = prepareTestMethodSentences(
                testMethodDeclaration, properties, methods, testMethodParameters, nestedSentences, emphasisedMethods
            )

            ParsedMethod(
                method.normalisedName,
                parameterCache[method]!!,
                testMethodSentences,
                nestedSentences,
                properties,
                methods
            )
        }

    private fun findAndAggregateMethodDeclarations(testClass: Class<*>): Triple<MutableList<MethodDeclarationContext>, MutableList<MethodDeclarationContext>, MutableList<MethodDeclarationContext>> {
        val testMethodDeclarations = ArrayList<MethodDeclarationContext>()
        val nestedSentenceDeclarations = ArrayList<MethodDeclarationContext>()
        val emphasisedMethodDeclarations = ArrayList<MethodDeclarationContext>()

        (testClass.findAnnotation<Sources>()?.value?.map { it.java }?.toList() ?: emptyList()).forEach {
            val (testMethods, nestedSentences, emphasisedMethods) = declarationCache.getOrPut(it) { findMethodDeclarationsIn(it) }
            testMethodDeclarations.addAll(testMethods)
            nestedSentenceDeclarations.addAll(nestedSentences)
            emphasisedMethodDeclarations.addAll(emphasisedMethods)
        }

        return Triple(testMethodDeclarations, nestedSentenceDeclarations, emphasisedMethodDeclarations)
    }

    private fun addMethodDeclarations(classToParse: Class<*>, testMethodDeclarations: MutableList<MethodDeclarationContext>, nestedSentenceDeclarations: MutableList<MethodDeclarationContext>, emphasisedMethodDeclarations: MutableList<MethodDeclarationContext>) {
        declarationCache.getOrPut(classToParse) { findMethodDeclarationsIn(classToParse) }.also {
                (testMethods, nestedSentences, emphasisedMethods) ->
            testMethodDeclarations.addAll(testMethods)
            nestedSentenceDeclarations.addAll(nestedSentences)
            emphasisedMethodDeclarations.addAll(emphasisedMethods)
        }
    }

    private fun findTestMethodDeclaration(
        method: Method,
        testMethodDeclarations: List<MethodDeclarationContext>
    ): MethodDeclarationContext {
        return testMethodDeclarations.find(matchingDeclarationFor(method))
            ?: throw KensaException("Did not find method declaration for test method [${method.name}]")
    }

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