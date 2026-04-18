package dev.kensa.parse

import dev.kensa.*
import dev.kensa.context.ExpandableInvocationContextHolder
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
    val expandableMethods: List<MethodDeclarationContext> = emptyList()
) {
    operator fun plus(other: ClassDeclarations): ClassDeclarations =
        ClassDeclarations(
            imports + other.imports,
            testMethods + other.testMethods,
            expandableMethods + other.expandableMethods
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

                val methods = cache.getOrPutMethods(testClass) {
                    relatedClasses.fold(mutableMapOf()) { acc, clazz ->
                        acc.apply { putAll(clazz.prepareMethods()) }
                    }
                }
                val expandableMethods = relatedClasses.fold(emptyMap<String, ParsedExpandableMethod>()) { acc, clazz ->
                    acc + clazz.prepareExpandableMethods(methodDeclarations, ParseContext(properties, methods))
                }
                val (testMethodSentences, testMethodParseErrors) = testClass.prepareTestMethodSentences(testMethodDeclaration, ParseContext(properties, methods, testMethodParameters.descriptors, expandableMethods))

                ParsedMethod(
                    indexInSource,
                    method.normalisedPlatformName,
                    testMethodParameters,
                    testMethodSentences,
                    expandableMethods,
                    properties,
                    methods,
                    testMethodParseErrors
                )
            }
        }.also {
            ExpandableInvocationContextHolder.expandableSentenceInvocationContext().update(it.expandableMethods)
        }

    private fun sentenceBuilder(): (Boolean, Location) -> SentenceBuilder =
        { isCommentSentence, location -> SentenceBuilder(isCommentSentence, location, configuration.dictionary, configuration.tabSize) }

    private fun Class<*>.prepareExpandableMethods(methodDeclarations: MethodDeclarations, parseContext: ParseContext): Map<String, ParsedExpandableMethod> =
        cache.getOrPutExpandableMethods(this) {
            val imports = methodDeclarations.declarationsByClass[this]?.imports ?: Imports(emptySet(), emptySet(), this)
            val declarations = methodDeclarations.declarationsByClass[this]?.expandableMethods ?: emptyList()

            declarations
                .map { dc ->
                    val method = findExpandableMethod(dc, imports)
                    val parameters = with(parserDelegate) {
                        method.prepareParameters(dc.parameterNamesAndTypes)
                    }
                    ParsedExpandableMethod(
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

    private fun Class<*>.findExpandableMethod(dc: MethodDeclarationContext, imports: Imports): Method =
        allMethods.filter { it.normalisedPlatformName == dc.name }
            .find { method ->
                val syntheticCount = method.findSyntheticKotlinReceivers().size
                val totalMethodParams = method.parameterTypes.size

                if (totalMethodParams != syntheticCount + dc.parameterTypes.size) return@find false

                val realMethodParams = method.parameterTypes.drop(syntheticCount).toTypedArray()
                imports.match(realMethodParams, dc.parameterTypes)
            } ?: throw KensaException("Did not find expandable method [${dc.name}] in class [${this.name}]")

    private fun Class<*>.prepareTestMethodSentences(methodDeclarationContext: MethodDeclarationContext, parseContext: ParseContext): Pair<List<TemplateSentence>, List<ParseError>> =
        ParserStateMachine(sentenceBuilder()).run {
            with(parserDelegate) {
                parse(this@run, parseContext, methodDeclarationContext)
            }
            sentences to parseErrors
        }

    private fun Class<*>.prepareMethods(): Map<String, MethodElementDescriptor> =
        allMethods
            .filter {
                it.hasAnnotation<RenderedValue>() ||
                    it.hasAnnotation<ExpandableRenderedValue>() ||
                    it.hasAnnotation<ExpandableSentence>() ||
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