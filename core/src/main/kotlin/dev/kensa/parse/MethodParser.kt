package dev.kensa.parse

import dev.kensa.*
import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.parse.ElementDescriptor.MethodElementDescriptor
import dev.kensa.sentence.SentenceBuilder
import dev.kensa.sentence.TemplateSentence
import dev.kensa.util.*
import java.lang.reflect.Method
import kotlin.collections.emptyList
import kotlin.reflect.KClass

class ClassDeclarations(
    val imports: Imports,
    val testMethods: List<MethodDeclarationContext> = emptyList(),
    val nestedMethods: List<MethodDeclarationContext> = emptyList(),
    val emphasisedMethods: List<MethodDeclarationContext> = emptyList()
) {
    operator fun plus(other: ClassDeclarations): ClassDeclarations =
        ClassDeclarations(
            imports + other.imports,
            testMethods + other.testMethods,
            nestedMethods + other.nestedMethods,
            emphasisedMethods + other.emphasisedMethods
        )
}

class MethodDeclarations(val declarationsByClass: Map<Class<*>, ClassDeclarations> = emptyMap()) {

    operator fun plus(other: MethodDeclarations): MethodDeclarations {
        val map = declarationsByClass.toMutableMap()
        other.declarationsByClass.forEach { (cls, dec) ->
            val declarations = map[cls]
            map[cls] = declarations?.plus(dec) ?: dec
        }

        return MethodDeclarations(map)
    }

    val allTestMethods: List<MethodDeclarationContext>
        get() = declarationsByClass.values.flatMap { it.testMethods }

    val nestedMethods: List<MethodDeclarationContext>
        get() = declarationsByClass.values.flatMap { it.nestedMethods }

    val emphasisedMethods: List<MethodDeclarationContext>
        get() = declarationsByClass.values.flatMap { it.emphasisedMethods }

    fun findTestMethodDeclaration(method: Method): Pair<Int, MethodDeclarationContext> {
        val declarations = declarationsByClass[method.declaringClass]
            ?: throw KensaException("Did not find declaration for class [${method.declaringClass.name}]")

        val index = declarations.testMethods.indexOfFirst(method.findMatchingDeclaration())
        if (index < 0) throw KensaException("Did not find method declaration for test method [${method.name}]")

        return index to declarations.testMethods[index]
    }

    private fun Method.findMatchingDeclaration() = { dc: MethodDeclarationContext ->
        val sourceTypes = dc.parameterTypes
        normalisedPlatformName == dc.name &&
            declarationsByClass[declaringClass]?.imports?.match(parameterTypes, sourceTypes) == true
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
                val (indexInSource, testMethodDeclaration) = methodDeclarations.findTestMethodDeclaration(method)

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
                val methods = cache.getOrPutMethods(testClass) {
                    relatedClasses.fold(mutableMapOf()) { acc, clazz ->
                        acc.apply { putAll(clazz.prepareMethods()) }
                    }
                }
                val nestedMethods = testClass.prepareNestedMethods(methodDeclarations, ParseContext(properties, methods))
                val testMethodSentences = testClass.prepareTestMethodSentences(testMethodDeclaration, ParseContext(properties, methods, testMethodParameters.descriptors, nestedMethods, emphasisedMethods))

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
            NestedInvocationContextHolder.expandableSentenceInvocationContext().update(it.nestedMethods)
        }

    private fun sentenceBuilder(): (Boolean, Location, Location) -> SentenceBuilder =
        { isCommentSentence, location, previousLocation -> SentenceBuilder(isCommentSentence, location, previousLocation, configuration.dictionary, configuration.tabSize) }

    private fun Class<*>.prepareNestedMethods(methodDeclarations: MethodDeclarations, parseContext: ParseContext): Map<String, ParsedNestedMethod> =
        cache.getOrPutNestedMethods(this) {
            val imports = methodDeclarations.declarationsByClass[this]?.imports ?: Imports(emptySet(), emptySet(), this)
            val declarations = methodDeclarations.declarationsByClass[this]?.nestedMethods ?: emptyList()

            declarations
                .map { dc ->
                    val method = findNestedMethod(dc, imports)
                    val parameters = with(parserDelegate) {
                        method.prepareParameters(dc.parameterNamesAndTypes)
                    }
                    ParsedNestedMethod(
                        dc.name,
                        parameters,
                        ParserStateMachine(sentenceBuilder()).run {
                            with(parserDelegate) {
                                parse(this@run, parseContext.copy(parameters.descriptors), dc)
                            }
                            sentences
                        }
                    )
                }
                .associateBy({ it.name }, { it })
        }

    private fun Class<*>.findNestedMethod(dc: MethodDeclarationContext, imports: Imports): Method =
        allMethods.filter { it.normalisedPlatformName == dc.name }
            .find { method ->
                val syntheticCount = method.findSyntheticKotlinReceivers().size
                val totalMethodParams = method.parameterTypes.size

                if (totalMethodParams != syntheticCount + dc.parameterTypes.size) return@find false

                val realMethodParams = method.parameterTypes.drop(syntheticCount).toTypedArray()
                imports.match(realMethodParams, dc.parameterTypes)
            } ?: throw KensaException("Did not find nested method [${dc.name}] in class [${this.name}]")

    private fun Class<*>.prepareTestMethodSentences(methodDeclarationContext: MethodDeclarationContext, parseContext: ParseContext): List<TemplateSentence> =
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

    private fun Class<*>.prepareMethods(): Map<String, MethodElementDescriptor> =
        allMethods
            .filter {
                it.hasAnnotation<RenderedValue>() ||
                    it.hasAnnotation<RenderedValue>() ||
                    it.hasAnnotation<ExpandableRenderedValue>() ||
                    it.hasAnnotation<ExpandableSentence>() ||
                    it.hasAnnotation<NestedSentence>() ||
                    it.hasAnnotation<Emphasise>() ||
                    it.hasAnnotation<Highlight>()
            }
            .map { ElementDescriptor.forMethod(it) }
            .associateBy(ElementDescriptor::name)

    private fun Class<*>.prepareProperties(directives: RenderingDirectives): Map<String, ElementDescriptor> =
        if (this.isEnum) {
            val enumClass = this as Class<Enum<*>>
            enumClass.enumConstants.flatMap<Enum<*>, ElementDescriptor> { it: Enum<*> ->
                buildList {
                    directives[enumClass.kotlin]?.let { directive ->
                        add(ElementDescriptor.forHintedEnumConstant(it, directive))
                    }
                }
            }.associateBy(ElementDescriptor::name)
        } else {
            allProperties
                .filter {
                    val propertyClass = it.returnType.classifier as? KClass<*>
                    it.hasKotlinOrJavaAnnotation<RenderedValue>() ||
                        it.hasKotlinOrJavaAnnotation<RenderedValueContainer>() ||
                        it.hasKotlinOrJavaAnnotation<Highlight>() ||
                        directives.containsKey(propertyClass)
                }
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
}