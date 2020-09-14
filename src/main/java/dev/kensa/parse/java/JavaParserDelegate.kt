package dev.kensa.parse.java

import dev.kensa.Kensa
import dev.kensa.KensaException
import dev.kensa.parse.Java8Lexer
import dev.kensa.parse.Java8Parser
import dev.kensa.parse.ParserDelegate
import dev.kensa.parse.ParserStateMachine
import dev.kensa.util.SourceCodeIndex
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import kotlin.reflect.KClass

object JavaParserDelegate : ParserDelegate<Java8Parser.MethodDeclarationContext> {

    override fun methodNameFrom(dc: Java8Parser.MethodDeclarationContext): String = dc.methodHeader().methodDeclarator().Identifier().text

    override fun findMethodDeclarationsIn(testClass: KClass<out Any>): Triple<List<Java8Parser.MethodDeclarationContext>, List<Java8Parser.MethodDeclarationContext>, List<Java8Parser.MethodDeclarationContext>> {
        val testMethodDeclarations = ArrayList<Java8Parser.MethodDeclarationContext>()
        val nestedMethodDeclarations = ArrayList<Java8Parser.MethodDeclarationContext>()
        val emphasisedMethodDeclarations = ArrayList<Java8Parser.MethodDeclarationContext>()

        // TODO : Need to test with nested classes as this probably won't work...
        val cdc = compilationUnitFor(testClass).typeDeclaration()
                .map { it.classDeclaration() }
                .firstOrNull { it is Java8Parser.ClassDeclarationContext }
                ?: throw KensaException("Unable to find class declaration in source code")

        cdc.normalClassDeclaration().classBody().classBodyDeclaration().forEach { cbd ->
            cbd.classMemberDeclaration().methodDeclaration()?.let { md ->
                testMethodDeclarations.takeIf { isAnnotatedAsTest(md) }?.add(md)
                nestedMethodDeclarations.takeIf { isAnnotatedAsNested(md) }?.add(md)
                emphasisedMethodDeclarations.takeIf { isAnnotatedAsEmphasised(md) }?.add(md)
            }
        }

        return Triple(testMethodDeclarations, nestedMethodDeclarations, emphasisedMethodDeclarations)
    }

    private fun compilationUnitFor(testClass: KClass<out Any>): Java8Parser.CompilationUnitContext {
        return Java8Parser(
                CommonTokenStream(
                        Java8Lexer(CharStreams.fromPath(SourceCodeIndex.locate(testClass)))
                )
        ).apply {
            takeIf { Kensa.configuration.antlrErrorListenerDisabled }?.removeErrorListeners()
            interpreter.predictionMode = Kensa.configuration.antlrPredicationMode
        }.compilationUnit()
    }

    private fun isAnnotatedAsTest(md: Java8Parser.MethodDeclarationContext) =
            md.methodModifier().any { mm ->
                val annotation = mm.annotation()

                annotation?.markerAnnotation()?.typeName()?.text?.let {
                    ParserDelegate.testAnnotationNames.contains(it)
                } ?: annotation?.normalAnnotation()?.typeName()?.text?.let {
                    ParserDelegate.testAnnotationNames.contains(it)
                } ?: false
            }

    private fun isAnnotatedAsNested(md: Java8Parser.MethodDeclarationContext) =
            md.methodModifier().any { mm ->
                mm.annotation()?.markerAnnotation()?.typeName()?.text?.let {
                    ParserDelegate.nestedSentenceAnnotationNames.contains(it)
                } ?: false
            }

    private fun isAnnotatedAsEmphasised(md: Java8Parser.MethodDeclarationContext) =
            md.methodModifier().any { mm ->
                mm.annotation()?.normalAnnotation()?.typeName()?.text?.let {
                    ParserDelegate.emphasisedMethodAnnotationNames.contains(it)
                } ?: false
            }

    override fun parameterNamesAndTypesFrom(dc: Java8Parser.MethodDeclarationContext): List<Pair<String, String>> {
        return ArrayList<Pair<String, String>>().apply {
            dc.methodHeader().methodDeclarator().formalParameterList()?.let { fpl ->
                fpl.formalParameters()?.let { fps ->
                    fps.formalParameter()?.forEach { fp ->
                        add(Pair(fp.variableDeclaratorId().text, fp.unannType().text))
                    }
                }
                fpl.lastFormalParameter().let { fps ->
                    fps.formalParameter()?.let { fp ->
                        add(Pair(fp.variableDeclaratorId().text, fp.unannType().text))
                    }
                }
            }
        }
    }

    override fun parse(stateMachine: ParserStateMachine, dc: Java8Parser.MethodDeclarationContext) {
        ParseTreeWalker().walk(JavaMethodBodyParser(stateMachine), dc.methodBody())
    }
}