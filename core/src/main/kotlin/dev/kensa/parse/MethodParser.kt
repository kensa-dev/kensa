package dev.kensa.parse

import dev.kensa.*
import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.parse.ElementDescriptor.MethodElementDescriptor
import dev.kensa.sentence.SentenceBuilder
import dev.kensa.sentence.TemplateSentence
import dev.kensa.util.*
import java.lang.reflect.Method
import kotlin.reflect.KClass


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

    companion object {
        private val greedyGenericPattern = "<.*>".toRegex()
    }
}

class MethodParser(
    private val cache: ParserCache,
    private val configuration: Configuration,
    private val parserDelegate: CompositeParserDelegate,
) {

    fun parse(method: Method): ParsedMethod =
        cache.getOrPutParsedMethod(method) {
            with(parserDelegate) {
                val testClass = method.declaringClass

                val methodDeclarations = testClass.findAllMethodDeclarations()
                val (indexInSource, testMethodDeclaration) = methodDeclarations.findTestMethodDeclaration(method, testClass.toSimpleName())

                val properties = cache.getOrPutProperties(testClass) { testClass.prepareProperties() }
                val testMethodParameters = cache.getOrPutParameters(method) {
                    method.prepareParameters(testMethodDeclaration.parameterNamesAndTypes)
                }

                val emphasisedMethods = cache.getOrPutEmphasisedMethods(testClass) {
                    prepareEmphasisedMethods(testClass, methodDeclarations.emphasisedMethods)
                }
                val methods = cache.getOrPutMethods(testClass) { testClass.prepareMethodsFor() }
                val nestedMethods = testClass.prepareNestedMethods(methodDeclarations.nestedMethods, ParseContext(properties, methods))
                val testMethodSentences = testClass.prepareTestMethods(testMethodDeclaration, ParseContext(properties, methods, testMethodParameters.descriptors, nestedMethods, emphasisedMethods))

                ParsedMethod(
                    indexInSource,
                    method.normalisedPlatformName,
                    testMethodParameters,
                    testMethodSentences,
                    nestedMethods,
                    properties,
                    methods
                )
            }
        }.also {
            NestedInvocationContextHolder.nestedSentenceInvocationContext().update(it.nestedMethods)
        }

    private fun Class<*>.findAllMethodDeclarations(): MethodDeclarations =
        findAllRelatedClasses().fold(MethodDeclarations()) { acc, clazz ->
            acc + cache.getOrPutMethodDeclarations(clazz) {
                with(parserDelegate) { findMethodDeclarations() }
            }
        }

    private fun Class<*>.findAllRelatedClasses(): Set<Class<*>> =
        mutableSetOf<Class<*>>().also { classes ->

            fun collect(clazz: Class<*>) {
                if (clazz in classes) return
                if (!SourceCode.existsFor(clazz)) return

                classes += clazz
                clazz.findAnnotation<Sources>()?.value?.forEach { collect(it.java) }
                clazz.interfaces.forEach { collect(it) }
                clazz.superclass?.also { collect(it) }
            }

            collect(this)
        }

    private fun sentenceBuilder(): (Location, Location) -> SentenceBuilder = { location, previousLocation -> SentenceBuilder(location, previousLocation, configuration.dictionary, configuration.tabSize) }

    private fun Class<*>.prepareNestedMethods(declarations: List<MethodDeclarationContext>, parseContext: ParseContext): Map<String, ParsedNestedMethod> =
        cache.getOrPutNestedMethods(this) {
            declarations
                .map {
                    val parameters = with(parserDelegate) {
                        findLocalOrSourcesMethod(it.name).prepareParameters(it.parameterNamesAndTypes)
                    }
                    ParsedNestedMethod(
                        it.name,
                        parameters,
                        ParserStateMachine(sentenceBuilder()).run {
                            with(parserDelegate) {
                                parse(this@run, parseContext.copy(parameters.descriptors), it)
                            }
                            sentences
                        }
                    )
                }
                .associateBy({ it.name }, { it })
        }

    private fun Class<*>.prepareTestMethods(methodDeclarationContext: MethodDeclarationContext, parseContext: ParseContext): List<TemplateSentence> =
        ParserStateMachine(sentenceBuilder()).run {
            with(parserDelegate) {
                parse(this@run, parseContext, methodDeclarationContext)
            }
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

    private fun Class<*>.prepareMethodsFor(): Map<String, MethodElementDescriptor> =
        allMethods
            .map { ElementDescriptor.forMethod(it) }
            .associateBy(ElementDescriptor::name)

    private fun Class<*>.prepareProperties() =
        allProperties
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