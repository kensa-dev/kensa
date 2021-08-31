package dev.kensa.parse.kotlin

import dev.kensa.Kensa
import dev.kensa.KensaException
import dev.kensa.parse.KotlinLexer
import dev.kensa.parse.KotlinParser
import dev.kensa.parse.ParserDelegate
import dev.kensa.parse.ParserStateMachine
import dev.kensa.util.SourceCodeIndex
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTreeWalker
import kotlin.reflect.KClass

object KotlinParserDelegate : ParserDelegate<KotlinParser.FunctionDeclarationContext> {

    override fun methodNameFrom(dc: KotlinParser.FunctionDeclarationContext): String =
            dc.simpleIdentifier().text.replace("`", "")

    override fun findMethodDeclarationsIn(testClass: KClass<out Any>): Triple<List<KotlinParser.FunctionDeclarationContext>, List<KotlinParser.FunctionDeclarationContext>, List<KotlinParser.FunctionDeclarationContext>> {
        val testFunctions = ArrayList<KotlinParser.FunctionDeclarationContext>()
        val nestedFunctions = ArrayList<KotlinParser.FunctionDeclarationContext>()
        val emphasisedFunctions = ArrayList<KotlinParser.FunctionDeclarationContext>()

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
            testFunctions: ArrayList<KotlinParser.FunctionDeclarationContext>,
            nestedFunctions: ArrayList<KotlinParser.FunctionDeclarationContext>,
            emphasisedFunctions: ArrayList<KotlinParser.FunctionDeclarationContext>
    ): (KotlinParser.FunctionDeclarationContext) -> Unit = { fd ->
        testFunctions.takeIf { isAnnotatedAsTest(fd) }?.add(fd)
        nestedFunctions.takeIf { isAnnotatedAsNested(fd) }?.add(fd)
        emphasisedFunctions.takeIf { isAnnotatedAsEmphasised(fd) }?.add(fd)
    }

    private fun compilationUnitFor(testClass: KClass<out Any>): KotlinParser.KotlinFileContext =
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

    override fun parameterNamesAndTypesFrom(dc: KotlinParser.FunctionDeclarationContext): List<Pair<String, String>> =
            ArrayList<Pair<String, String>>().apply {
                dc.functionValueParameters().functionValueParameter()
                        .map { it.parameter() }
                        .forEach {
                            add(Pair(it.simpleIdentifier().text, it.type().text.trimEnd('?')))
                        }
            }

    override fun parse(stateMachine: ParserStateMachine, dc: KotlinParser.FunctionDeclarationContext) {
        ParseTreeWalker().walk(KotlinFunctionBodyParser(stateMachine), dc.functionBody())
    }
}