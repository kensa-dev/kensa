package dev.kensa.parse.kotlin

import dev.kensa.Kensa
import dev.kensa.KensaException
import dev.kensa.parse.*
import dev.kensa.util.SourceCodeIndex
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTreeWalker

object KotlinParserDelegate : ParserDelegate {

    override fun findMethodDeclarationsIn(testClass: Class<out Any>): Triple<List<MethodDeclarationContext>, List<MethodDeclarationContext>, List<MethodDeclarationContext>> {
        val testFunctions = ArrayList<MethodDeclarationContext>()
        val nestedFunctions = ArrayList<MethodDeclarationContext>()
        val emphasisedFunctions = ArrayList<MethodDeclarationContext>()

        // TODO : Need to test with nested classes as this probably won't work...
        ArrayList<KotlinParser.FunctionDeclarationContext>().apply {
            compilationUnitFor(testClass).topLevelObject().forEach {
                findFunctionDeclarations(it, this)
            }

            forEach {
                assignDeclarations(testFunctions, nestedFunctions, emphasisedFunctions)(it)
            }
        }

        if (testFunctions.isEmpty())
            throw KensaException("Unable to find class declaration in source code")

        return Triple(testFunctions, nestedFunctions, emphasisedFunctions)
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
        testFunctions: ArrayList<MethodDeclarationContext>,
        nestedFunctions: ArrayList<MethodDeclarationContext>,
        emphasisedFunctions: ArrayList<MethodDeclarationContext>
    ): (KotlinParser.FunctionDeclarationContext) -> Unit = { fd ->
        testFunctions.takeIf { isAnnotatedAsTest(fd) }?.add(KotlinMethodDeclarationContext(fd))
        nestedFunctions.takeIf { isAnnotatedAsNested(fd) }?.add(KotlinMethodDeclarationContext(fd))
        emphasisedFunctions.takeIf { isAnnotatedAsEmphasised(fd) }?.add(KotlinMethodDeclarationContext(fd))
    }

    private fun compilationUnitFor(testClass: Class<out Any>): KotlinParser.KotlinFileContext =
            KotlinParser(
                    CommonTokenStream(
                            KotlinLexer(CharStreams.fromPath(SourceCodeIndex.locate(testClass)))
                    )
            ).apply {
                takeIf { Kensa.configuration.antlrErrorListenerDisabled }?.removeErrorListeners()
                interpreter.predictionMode = Kensa.configuration.antlrPredicationMode
            }.kotlinFile()

    private fun isAnnotatedAsTest(fd: KotlinParser.FunctionDeclarationContext): Boolean =
            findAnnotationsIn(fd).any { ac ->
                ac.singleAnnotation()?.unescapedAnnotation()?.userType()?.text?.let {
                    ParserDelegate.testAnnotationNames.contains(it)
                } ?: false
            }

    private fun findAnnotationsIn(fd: KotlinParser.FunctionDeclarationContext): List<KotlinParser.AnnotationContext> =
            fd.modifiers()?.annotation()?.takeIf { it.isNotEmpty() }
                    ?: fd.parent?.parent?.takeIf { it is KotlinParser.StatementContext }?.let { (it as KotlinParser.StatementContext).annotation() }
                    ?: emptyList()

    private fun isAnnotatedAsNested(fd: KotlinParser.FunctionDeclarationContext): Boolean =
            findAnnotationsIn(fd).any { ac ->
                ac.singleAnnotation()?.unescapedAnnotation()?.userType()?.text?.let {
                    ParserDelegate.nestedSentenceAnnotationNames.contains(it)
                } ?: false
            }

    private fun isAnnotatedAsEmphasised(fd: KotlinParser.FunctionDeclarationContext): Boolean =
            findAnnotationsIn(fd).any { ac ->
                ac.singleAnnotation()?.unescapedAnnotation()?.constructorInvocation()?.userType()?.text?.let {
                    ParserDelegate.emphasisedMethodAnnotationNames.contains(it)
                } ?: false
            }

    override fun parse(stateMachine: ParserStateMachine, dc: MethodDeclarationContext) {
        ParseTreeWalker().walk(KotlinFunctionBodyParser(stateMachine), dc.body)
    }
}