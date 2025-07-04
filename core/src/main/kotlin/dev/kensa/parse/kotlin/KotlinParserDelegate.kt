package dev.kensa.parse.kotlin

import dev.kensa.parse.*
import dev.kensa.util.SourceCode
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.tree.ParseTreeWalker

class KotlinParserDelegate(private val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean, private val antlrErrorListenerDisabled: Boolean, private val antlrPredicationMode: PredictionMode) : ParserDelegate {

    override fun findMethodDeclarationsIn(target: Class<out Any>): MethodDeclarations {
        val testFunctions = mutableListOf<MethodDeclarationContext>()
        val nestedFunctions = mutableListOf<MethodDeclarationContext>()
        val emphasisedFunctions = mutableListOf<MethodDeclarationContext>()

        // TODO : Need to test with nested classes as this probably won't work...
        ArrayList<KotlinParser.FunctionDeclarationContext>().apply {
            compilationUnitFor(target).topLevelObject().forEach {
                findFunctionDeclarations(it, this)
            }

            forEach {
                assignDeclarations(testFunctions, nestedFunctions, emphasisedFunctions)(it)
            }
        }

        return MethodDeclarations(testFunctions, nestedFunctions, emphasisedFunctions)
    }

    private fun findFunctionDeclarations(it: ParserRuleContext, result: MutableList<KotlinParser.FunctionDeclarationContext>) {
        it.children?.forEach {
            when (it) {
                is KotlinParser.FunctionDeclarationContext -> result.add(it)
                is ParserRuleContext -> findFunctionDeclarations(it, result)
            }
        }
    }

    private fun assignDeclarations(
        testFunctions: MutableList<MethodDeclarationContext>,
        nestedFunctions: MutableList<MethodDeclarationContext>,
        emphasisedFunctions: MutableList<MethodDeclarationContext>
    ): (KotlinParser.FunctionDeclarationContext) -> Unit = { fd ->
        testFunctions.takeIf { isTest(fd) }?.add(KotlinMethodDeclarationContext(fd))
        nestedFunctions.takeIf { fd.isAnnotatedAsNested() }?.add(KotlinMethodDeclarationContext(fd))
        emphasisedFunctions.takeIf { fd.isAnnotatedAsEmphasised() }?.add(KotlinMethodDeclarationContext(fd))
    }

    private fun compilationUnitFor(testClass: Class<out Any>): KotlinParser.KotlinFileContext =
        KotlinParser(
            CommonTokenStream(
                KotlinLexer(SourceCode.sourceStreamFor(testClass))
            )
        ).apply {
            takeIf { antlrErrorListenerDisabled }?.removeErrorListeners()
            interpreter.predictionMode = antlrPredicationMode
        }.kotlinFile()

    private fun KotlinParser.FunctionDeclarationContext.isAnnotatedAsNested(): Boolean = findAnnotationNames().any { name -> ParserDelegate.nestedSentenceAnnotationNames.contains(name) }

    private fun KotlinParser.FunctionDeclarationContext.isAnnotatedAsEmphasised(): Boolean = findAnnotationNames().any { name -> ParserDelegate.emphasisedMethodAnnotationNames.contains(name) }

    override fun parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext) {
        ParseTreeWalker().walk(KotlinFunctionBodyParser(stateMachine, parseContext), dc.body)
    }

    companion object {
        fun KotlinParser.FunctionDeclarationContext.findAnnotationNames(): List<String> {
            val annotationContexts = modifiers()?.annotation()?.takeIf { it.isNotEmpty() }
                ?: parent?.parent?.takeIf { it is KotlinParser.StatementContext }?.let { (it as KotlinParser.StatementContext).annotation() }
                ?: emptyList()

            return annotationContexts.mapNotNull {
                val namedWithConstructorInvocation = it.singleAnnotation()?.unescapedAnnotation()?.constructorInvocation()?.userType()?.text
                val namedWithoutConstructorInvocation = it.singleAnnotation()?.unescapedAnnotation()?.userType()?.text

                namedWithConstructorInvocation ?: namedWithoutConstructorInvocation
            }
        }
    }
}