package dev.kensa.parse.java

import dev.kensa.Kensa
import dev.kensa.KensaException
import dev.kensa.parse.*
import dev.kensa.parse.ParserDelegate.Companion.testAnnotationNames
import dev.kensa.util.SourceCodeIndex
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

object JavaParserDelegate : ParserDelegate {

    override fun findMethodDeclarationsIn(testClass: Class<out Any>): Triple<List<MethodDeclarationContext>, List<MethodDeclarationContext>, List<MethodDeclarationContext>> {
        val testMethodDeclarations = ArrayList<MethodDeclarationContext>()
        val nestedMethodDeclarations = ArrayList<MethodDeclarationContext>()
        val emphasisedMethodDeclarations = ArrayList<MethodDeclarationContext>()

        // TODO : Need to test with nested classes as this probably won't work...
        compilationUnitFor(testClass).ordinaryCompilationUnit().topLevelClassOrInterfaceDeclaration()
            .firstNotNullOfOrNull {
                it.classDeclaration() ?: it.interfaceDeclaration()
            }?.apply {
                when (this) {
                    is Java20Parser.ClassDeclarationContext ->
                        normalClassDeclaration().classBody().classBodyDeclaration().forEach { cbd ->
                            cbd.classMemberDeclaration().methodDeclaration()?.let { md ->
                                testMethodDeclarations.takeIf { isAnnotatedAsTest(md.methodModifier(), Java20Parser.MethodModifierContext::annotation) }?.add(JavaMethodDeclarationContext(md))
                                nestedMethodDeclarations.takeIf { isAnnotatedAsNested(md) }?.add(JavaMethodDeclarationContext(md))
                                emphasisedMethodDeclarations.takeIf { isAnnotatedAsEmphasised(md) }?.add(JavaMethodDeclarationContext(md))
                            }
                        }

                    is Java20Parser.InterfaceDeclarationContext ->
                        normalInterfaceDeclaration().interfaceBody().interfaceMemberDeclaration().forEach { imd ->
                            imd.interfaceMethodDeclaration()?.let { md ->
                                testMethodDeclarations.takeIf { isAnnotatedAsTest(md.interfaceMethodModifier(), Java20Parser.InterfaceMethodModifierContext::annotation) }
                                    ?.add(JavaInterfaceDeclarationContext(md))
                            }
                        }

                    else -> throw KensaException("Cannot handle Parser Rule Contexts of type [${this::class.java}]")
                }
            } ?: throw KensaException("Unable to find class declaration in source code")

        return Triple(testMethodDeclarations, nestedMethodDeclarations, emphasisedMethodDeclarations)
    }

    private fun <T> isAnnotatedAsTest(l: List<T>, mmcProvider: (T) -> Java20Parser.AnnotationContext) =
        l.any { mmcProvider(it).isTestAnnotation() }

    private fun compilationUnitFor(testClass: Class<out Any>): Java20Parser.CompilationUnitContext =
        Java20Parser(
            CommonTokenStream(
                Java20Lexer(CharStreams.fromPath(SourceCodeIndex.locate(testClass)))
            )
        ).apply {
            takeIf { Kensa.configuration.antlrErrorListenerDisabled }?.removeErrorListeners()
            interpreter.predictionMode = Kensa.configuration.antlrPredicationMode
        }.compilationUnit()

    private fun Java20Parser.AnnotationContext?.isTestAnnotation() =
        this?.markerAnnotation()?.typeName()?.text?.let {
            testAnnotationNames.contains(it)
        } ?: this?.normalAnnotation()?.typeName()?.text?.let {
            testAnnotationNames.contains(it)
        } ?: false

    private fun isAnnotatedAsNested(md: Java20Parser.MethodDeclarationContext) =
        md.methodModifier().any { mm ->
            mm.annotation()?.markerAnnotation()?.typeName()?.text?.let {
                ParserDelegate.nestedSentenceAnnotationNames.contains(it)
            } ?: false
        }

    private fun isAnnotatedAsEmphasised(md: Java20Parser.MethodDeclarationContext) =
        md.methodModifier().any { mm ->
            mm.annotation()?.normalAnnotation()?.typeName()?.text?.let {
                ParserDelegate.emphasisedMethodAnnotationNames.contains(it)
            } ?: false
        }

    override fun parse(stateMachine: ParserStateMachine, dc: MethodDeclarationContext) {
        ParseTreeWalker().walk(JavaMethodBodyParser(stateMachine), dc.body)
    }
}