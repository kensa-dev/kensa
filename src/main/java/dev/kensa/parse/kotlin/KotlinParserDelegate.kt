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
        compilationUnitFor(testClass).topLevelObject()
            .mapNotNull { it.declaration()?.classDeclaration() ?: it.declaration().functionDeclaration() }
            .forEach {
                when (it) {
                    is KotlinParser.ClassDeclarationContext -> {
                        it.classBody().classMemberDeclarations().classMemberDeclaration().forEach { cmd ->
                            cmd.declaration()?.functionDeclaration()?.let(
                                assignDeclarations(testFunctions, nestedFunctions, emphasisedFunctions)
                            )
                        }
                    }
                    is KotlinParser.FunctionDeclarationContext -> {
                        assignDeclarations(testFunctions, nestedFunctions, emphasisedFunctions)(it)
                    }
                }
            }

        if (testFunctions.isEmpty())
            throw KensaException("Unable to find class declaration in source code")

        return Triple(testFunctions, nestedFunctions, emphasisedFunctions)
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

    private fun compilationUnitFor(testClass: KClass<out Any>): KotlinParser.KotlinFileContext {
        return KotlinParser(
            CommonTokenStream(
                KotlinLexer(CharStreams.fromPath(SourceCodeIndex.locate(testClass)))
            )
        ).apply {
            takeIf { Kensa.configuration.antlrErrorListenerDisabled }?.removeErrorListeners()
            interpreter.predictionMode = Kensa.configuration.antlrPredicationMode
        }.kotlinFile()
    }

    private fun isAnnotatedAsTest(fd: KotlinParser.FunctionDeclarationContext): Boolean {
        return fd.modifiers().annotation().any { ac ->
            ac.singleAnnotation()?.unescapedAnnotation()?.userType()?.text?.let {
                ParserDelegate.testAnnotationNames.contains(it)
            } ?: false
        }
    }

    private fun isAnnotatedAsNested(fd: KotlinParser.FunctionDeclarationContext): Boolean {
        return fd.modifiers().annotation().any { ac ->
            ac.singleAnnotation()?.unescapedAnnotation()?.userType()?.text?.let {
                ParserDelegate.nestedSentenceAnnotationNames.contains(it)
            } ?: false
        }
    }

    private fun isAnnotatedAsEmphasised(fd: KotlinParser.FunctionDeclarationContext): Boolean {
        return fd.modifiers().annotation().any { ac ->
            ac.singleAnnotation()?.unescapedAnnotation()?.constructorInvocation()?.userType()?.text?.let {
                ParserDelegate.emphasisedMethodAnnotationNames.contains(it)
            } ?: false
        }
    }

    override fun parameterNamesAndTypesFrom(dc: KotlinParser.FunctionDeclarationContext): List<Pair<String, String>> {
        return ArrayList<Pair<String, String>>().apply {
            dc.functionValueParameters().functionValueParameter()
                .map { it.parameter() }
                .forEach {
                    add(Pair(it.simpleIdentifier().text, it.type().text.trimEnd('?')))
                }
        }
    }

    override fun parse(stateMachine: ParserStateMachine, dc: KotlinParser.FunctionDeclarationContext) {
        ParseTreeWalker().walk(KotlinFunctionBodyParser(stateMachine), dc.functionBody())
    }
}