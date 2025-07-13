package dev.kensa.parse

import dev.kensa.*
import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.sentence.TemplateSentence
import dev.kensa.sentence.SentenceBuilder
import dev.kensa.util.*
import java.lang.reflect.Method
import kotlin.collections.buildList
import kotlin.reflect.KClass
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.jvm.kotlinFunction

val greedyGenericPattern = "<.*>".toRegex()

class MethodDeclarations(val testMethods: List<MethodDeclarationContext> = emptyList(), val nestedMethods: List<MethodDeclarationContext> = emptyList(), val emphasisedMethods: List<MethodDeclarationContext> = emptyList()) {
    operator fun plus(other: MethodDeclarations) = MethodDeclarations(testMethods + other.testMethods, nestedMethods + other.nestedMethods, emphasisedMethods + other.emphasisedMethods)

    fun findTestMethodDeclaration(method: Method, toSimpleTypeName: (Class<*>) -> String): Pair<Int, MethodDeclarationContext> {
        val index = testMethods.indexOfFirst(matchingDeclarationFor(method, toSimpleTypeName))
        if (index < 0) throw KensaException("Did not find method declaration for test method [${method.name}]")

        return index to testMethods[index]
    }

    private fun matchingDeclarationFor(method: Method, toSimpleTypeName: (Class<*>) -> String) = { dc: MethodDeclarationContext ->
        method.normalisedPlatformName == dc.name &&
            // Only match on parameter simple type name - saves having to go looking in the imports
            dc.parameterNamesAndTypes.map {
                it.second.substringAfterLast('.').replace(greedyGenericPattern, "")
            } == method.parameterTypes.map(toSimpleTypeName)
    }
}

interface MethodParser : ParserCache, ParserDelegate {

    val configuration: Configuration
    val toSimpleTypeName: (Class<*>) -> String

    fun parse(method: Method): ParsedMethod =
        parsedMethodCache.computeIfAbsent(method) {
            val testClass = method.declaringClass
            val actualDeclaringClass = method.actualDeclaringClass()
            val classToParse = testClass.takeIf { it == actualDeclaringClass } ?: actualDeclaringClass

            val methodDeclarations = testClass.findSourcesMethodDeclarations() + classToParse.findMethodDeclarations()
            val (indexInSource, testMethodDeclaration) = methodDeclarations.findTestMethodDeclaration(method, toSimpleTypeName)

            val properties = propertyCache.getOrPut(testClass) { preparePropertiesFor(testClass) }

            val testMethodParameters = parameterCache.getOrPut(method) {
                prepareParametersFor(method, testMethodDeclaration.parameterNamesAndTypes)
            }

            val emphasisedMethods = emphasisedMethodCache.getOrPut(testClass) {
                prepareEmphasisedMethods(testClass, methodDeclarations.emphasisedMethods)
            }
            val methods = methodCache.getOrPut(testClass) { prepareMethodsFor(testClass) }
            val nestedSentences = prepareNestedSentences(testClass, methodDeclarations.nestedMethods, ParseContext(properties, methods))
            val testMethodSentences = testMethodDeclaration.prepareTestMethodSentences(ParseContext(properties, methods, testMethodParameters.descriptors, nestedSentences, emphasisedMethods))

            ParsedMethod(
                indexInSource,
                method.normalisedPlatformName,
                testMethodParameters,
                testMethodSentences,
                nestedSentences,
                properties,
                methods
            )
        }.also {
            NestedInvocationContextHolder.nestedSentenceInvocationContext().update(it.nestedMethods)
        }

    private fun Class<*>.findSourcesMethodDeclarations(): MethodDeclarations =
        MethodDeclarations().let { declarations ->
            findAnnotation<Sources>()?.value?.map { it.java }?.fold(declarations) { acc, sourceClass ->
                acc + declarationCache.getOrPut(sourceClass) { findMethodDeclarationsIn(sourceClass) }
            } ?: declarations
        }

    private fun Class<*>.findMethodDeclarations(): MethodDeclarations = declarationCache.getOrPut(this) { findMethodDeclarationsIn(this) }

    private fun sentenceBuilder(): (Location, Location) -> SentenceBuilder = { location, previousLocation -> SentenceBuilder(location, previousLocation, configuration.dictionary, configuration.tabSize) }

    private fun prepareNestedSentences(testClass: Class<*>, nestedSentenceDeclarations: List<MethodDeclarationContext>, parseContext: ParseContext): Map<String, ParsedNestedMethod> {
        nestedMethodCache[testClass] = nestedSentenceDeclarations
            .map {
                val parameters = prepareParametersFor(testClass.findMethod(it.name), it.parameterNamesAndTypes)
                val parsedNestedMethod = ParsedNestedMethod(
                    it.name,
                    parameters,
                    ParserStateMachine(sentenceBuilder()).run {
                        parse(this, parseContext.copy(parameters.descriptors), it)
                        sentences
                    }
                )
                parsedNestedMethod
            }
            .associateBy({it.name}, { it })

        return nestedMethodCache[testClass] ?: emptyMap()
    }

    private fun MethodDeclarationContext.prepareTestMethodSentences(parseContext: ParseContext): List<TemplateSentence> =
        ParserStateMachine(sentenceBuilder()).run {
            parse(this, parseContext, this@prepareTestMethodSentences)
            sentences
        }

    private fun prepareEmphasisedMethods(testClass: Class<*>, emphasisedMethodDeclarations: List<MethodDeclarationContext>): Map<String, EmphasisDescriptor> =
        emphasisedMethodDeclarations
            .map { dc ->
                testClass.findMethod(dc.name).findAnnotation<Emphasise>()!!.run {
                    Pair(dc.name, EmphasisDescriptor(textStyles.toSet(), textColour, backgroundColor))
                }
            }
            .associateBy({ it.first }, { it.second })

    private fun prepareParametersFor(method: Method, parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters {
        val parameterNamesAndTypesWithReceiverParameter: List<Pair<String, String>> = method.kotlinFunction?.extensionReceiverParameter?.let {
            ArrayList(parameterNamesAndTypes).apply {
                add(it.index - 1, Pair("this", (it.type.classifier as KClass<*>).simpleName!!))
            }
        } ?: parameterNamesAndTypes

        return MethodParameters(
            method.parameters.mapIndexed { index, parameter ->
                ElementDescriptor.forParameter(parameter, parameterNamesAndTypesWithReceiverParameter[index].first, index)
            }.associateByTo(LinkedHashMap(), ElementDescriptor::name)
        )
    }

    private fun prepareMethodsFor(clazz: Class<*>) =
        clazz.allMethods
            .map { ElementDescriptor.forMethod(it) }
            .associateBy(ElementDescriptor::name)

    private fun preparePropertiesFor(clazz: Class<*>) =
        clazz.allProperties
            .flatMap { property ->
                buildList {
                    val descriptor = ElementDescriptor.forProperty(property).also { add(it) }

                    // Lift the properties of any ResolverHolders
                    if (descriptor.isRenderedValueContainer) {
                        val classifier = property.returnType.classifier
                        if (classifier is KClass<*>) {
                            addAll(
                                classifier.allProperties
                                    .filter { it.hasKotlinOrJavaAnnotation<RenderedValue>() }
                                    .map { ElementDescriptor.forResolveHolder(descriptor, it) }
                            )
                        }
                    }
                }
            }.associateBy(ElementDescriptor::name)
}