package dev.kensa.parse

import dev.kensa.*
import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.parse.ElementDescriptor.MethodElementDescriptor
import dev.kensa.sentence.SentenceBuilder
import dev.kensa.sentence.TemplateSentence
import dev.kensa.util.*
import java.lang.reflect.Method
import kotlin.collections.buildList
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
                val relatedClasses = testClass.findAllRelatedClasses(parserDelegate.sourceCode)

                val methodDeclarations = relatedClasses.fold(MethodDeclarations()) { acc, clazz ->
                    acc + cache.getOrPutMethodDeclarations(clazz) {
                        with(parserDelegate) { clazz.findMethodDeclarations() }
                    }
                }
                val (indexInSource, testMethodDeclaration) = methodDeclarations.findTestMethodDeclaration(method, testClass.toSimpleName())

                val directives = cache.getOrPutRenderingDirectives(testClass) { testClass.findAllRenderingDirectives() }
                val properties = cache.getOrPutProperties(testClass) {
                    relatedClasses.fold(mutableMapOf()) { acc, clazz ->
                        acc.apply { putAll(clazz.prepareProperties(directives)) }
                    }
                }
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

    private fun sentenceBuilder(): (Boolean, Location, Location) -> SentenceBuilder = { isCommentSentence, location, previousLocation -> SentenceBuilder(isCommentSentence, location, previousLocation, configuration.dictionary, configuration.tabSize) }

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

    private fun Class<*>.prepareProperties(directives: RenderingDirectives) =
        allProperties
            .flatMap { property ->
                buildList {
                    val propertyClass = property.returnType.classifier as? KClass<*>

                    directives[propertyClass]?.let { directive ->
                        add(ElementDescriptor.forHintedProperty(property, directive))
                    } ?: run {
                        val descriptor = ElementDescriptor.forProperty(property).also { add(it) }

                        if (descriptor.isRenderedValueContainer) {
                            (property.returnType.classifier as? KClass<*>)?.let { classifier ->
                                addAll(
                                    classifier.allProperties
                                        .filter { it.hasKotlinOrJavaAnnotation<RenderedValue>() }
                                        .map { ElementDescriptor.forResolveHolder(descriptor, it) }
                                )
                            }
                        }
                    }
                }
            }.associateBy(ElementDescriptor::name)
}