package dev.kensa.parse.kotlin

import dev.kensa.parse.*
import dev.kensa.util.SourceCode
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.full.contextParameters
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.jvm.kotlinFunction

class KotlinParserDelegate(private val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean, private val antlrErrorListenerDisabled: Boolean, private val antlrPredicationMode: PredictionMode) : ParserDelegate {

    override fun findMethodDeclarationsIn(target: Class<out Any>): MethodDeclarations {
        val testFunctions = mutableListOf<MethodDeclarationContext>()
        val nestedFunctions = mutableListOf<MethodDeclarationContext>()
        val emphasisedFunctions = mutableListOf<MethodDeclarationContext>()

        val tokens = tokenStreamFor(target)

        // TODO : Need to test with nested classes as this probably won't work...
        ArrayList<KotlinParser.FunctionDeclarationContext>().apply {
            compilationUnitFor(tokens).topLevelObject().forEach {
                findFunctionDeclarations(it, this)
            }

            forEach {
                assignDeclarations(tokens, testFunctions, nestedFunctions, emphasisedFunctions)(it)
            }
        }

        return MethodDeclarations(testFunctions, nestedFunctions, emphasisedFunctions)
    }

    override fun prepareParametersFor(method: Method, parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters {
        val combined = method.syntheticKotlinReceivers() + parameterNamesAndTypes

        return MethodParameters(
            method.parameters.mapIndexed { index, parameter ->
                ElementDescriptor.forParameter(parameter, combined[index].first, index)
            }.associateByTo(LinkedHashMap(), ElementDescriptor::name)
        )
    }

    @OptIn(ExperimentalContextParameters::class)
    fun Method.syntheticKotlinReceivers(): List<Pair<String, String>> =
        kotlinFunction?.let { kfun ->
            buildList {
                kfun.contextParameters.forEachIndexed { idx, p ->
                    val t = (p.type.classifier as? KClass<*>)?.java?.name ?: "java.lang.Object"
                    add("context$${idx + 1}" to t)
                }
                kfun.extensionReceiverParameter?.let { p ->
                    val t = (p.type.classifier as? KClass<*>)?.java?.name ?: "java.lang.Object"
                    add("receiver" to t)
                }
            }
        } ?: emptyList()

    private fun findFunctionDeclarations(it: ParserRuleContext, result: MutableList<KotlinParser.FunctionDeclarationContext>) {
        it.children?.forEach {
            when (it) {
                is KotlinParser.FunctionDeclarationContext -> result.add(it)
                is ParserRuleContext -> findFunctionDeclarations(it, result)
            }
        }
    }

    private fun assignDeclarations(
        tokens: CommonTokenStream,
        testFunctions: MutableList<MethodDeclarationContext>,
        nestedFunctions: MutableList<MethodDeclarationContext>,
        emphasisedFunctions: MutableList<MethodDeclarationContext>
    ): (KotlinParser.FunctionDeclarationContext) -> Unit = { fd ->
        testFunctions.takeIf { isTest(fd) }?.add(KotlinMethodDeclarationContext(fd, tokens))
        nestedFunctions.takeIf { fd.isAnnotatedAsNested() }?.add(KotlinMethodDeclarationContext(fd, tokens))
        emphasisedFunctions.takeIf { fd.isAnnotatedAsEmphasised() }?.add(KotlinMethodDeclarationContext(fd, tokens))
    }

    private fun tokenStreamFor(testClass: Class<out Any>) =
        SourceCode
            .sourceStreamFor(testClass)
            .let { KotlinLexer(it) }
            .let { CommonTokenStream(it) }

    private fun compilationUnitFor(tokenStream: CommonTokenStream): KotlinParser.KotlinFileContext =
        KotlinParser(tokenStream).apply {
            takeIf { antlrErrorListenerDisabled }?.removeErrorListeners()
            interpreter.predictionMode = antlrPredicationMode
        }.kotlinFile()

    private fun KotlinParser.FunctionDeclarationContext.isAnnotatedAsNested(): Boolean = findAnnotationNames().any { name -> ParserDelegate.nestedSentenceAnnotationNames.contains(name) }

    private fun KotlinParser.FunctionDeclarationContext.isAnnotatedAsEmphasised(): Boolean = findAnnotationNames().any { name -> ParserDelegate.emphasisedMethodAnnotationNames.contains(name) }

    override fun parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext) {
        ParseTreeWalker().walk(KotlinFunctionBodyParser(dc.tokenStream, stateMachine, parseContext), dc.body)
    }

    companion object {
        fun KotlinParser.FunctionDeclarationContext.findAnnotationNames(): List<String> {
            val functionAnnotations = modifiers().flatMap { it.annotation() }
            val statementAnnotations = parent?.parent?.takeIf { it is KotlinParser.StatementContext }?.let { (it as KotlinParser.StatementContext).annotation() }.orEmpty()

            val annotationContexts = functionAnnotations + statementAnnotations

            return annotationContexts.mapNotNull {
                val namedWithConstructorInvocation = it.singleAnnotation()?.unescapedAnnotation()?.constructorInvocation()?.userType()?.text
                val namedWithoutConstructorInvocation = it.singleAnnotation()?.unescapedAnnotation()?.userType()?.text

                namedWithConstructorInvocation ?: namedWithoutConstructorInvocation
            }
        }
    }
}