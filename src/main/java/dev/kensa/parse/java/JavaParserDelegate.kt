package dev.kensa.parse.java

import dev.kensa.Kensa
import dev.kensa.KensaException
import dev.kensa.parse.*
import dev.kensa.parse.Java20Parser.ClassDeclarationContext
import dev.kensa.parse.Java20Parser.InterfaceDeclarationContext
import dev.kensa.parse.ParserDelegate.Companion.testAnnotationNames
import dev.kensa.util.SourceCode
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

object JavaParserDelegate : ParserDelegate {

    override fun findMethodDeclarationsIn(target: Class<out Any>): MethodDeclarations {
        val testMethods = mutableListOf<MethodDeclarationContext>()
        val nestedMethods = mutableListOf<MethodDeclarationContext>()
        val emphasisedMethods = mutableListOf<MethodDeclarationContext>()

        // TODO : Need to test with nested classes as this probably won't work...
        compilationUnitFor(target).ordinaryCompilationUnit().topLevelClassOrInterfaceDeclaration()
            .firstNotNullOfOrNull {
                it.classDeclaration() ?: it.interfaceDeclaration()
            }?.apply {
                when (this) {
                    is ClassDeclarationContext ->
                        normalClassDeclaration().classBody().classBodyDeclaration().forEach { cbd ->
                            cbd.classMemberDeclaration().methodDeclaration()?.let { md ->
                                testMethods.takeIf { isAnnotatedAsTest(md.methodModifier(), Java20Parser.MethodModifierContext::annotation) }?.add(JavaMethodDeclarationContext(md))
                                nestedMethods.takeIf { md.isAnnotatedAsNested() }?.add(JavaMethodDeclarationContext(md))
                                emphasisedMethods.takeIf { md.isAnnotatedAsEmphasised() }?.add(JavaMethodDeclarationContext(md))
                            }
                        }
                    is InterfaceDeclarationContext ->
                        normalInterfaceDeclaration().interfaceBody().interfaceMemberDeclaration().forEach { imd ->
                            imd.interfaceMethodDeclaration()?.let { md ->
                                testMethods.takeIf { isAnnotatedAsTest(md.interfaceMethodModifier(), Java20Parser.InterfaceMethodModifierContext::annotation) }?.add(JavaInterfaceDeclarationContext(md))
                                nestedMethods.takeIf { md.isAnnotatedAsNested() }?.add(JavaInterfaceDeclarationContext(md))
                                emphasisedMethods.takeIf { md.isAnnotatedAsEmphasised() }?.add(JavaInterfaceDeclarationContext(md))
                            }
                        }

                    else -> throw KensaException("Cannot handle Parser Rule Contexts of type [${this::class.java}]")
                }
            } ?: throw KensaException("Unable to find class declaration in source code")

        return MethodDeclarations(testMethods, nestedMethods, emphasisedMethods)
    }

    private fun <T> isAnnotatedAsTest(l: List<T>, mmcProvider: (T) -> Java20Parser.AnnotationContext) =
        l.any { mmcProvider(it).isTestAnnotation() }

    private fun compilationUnitFor(target: Class<out Any>): Java20Parser.CompilationUnitContext =
        Java20Parser(
            CommonTokenStream(
                Java20Lexer(SourceCode.sourceStreamFor(target))
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

    private fun Java20Parser.MethodDeclarationContext.isAnnotatedAsNested() =
        methodModifier().any { mm ->
            mm.annotation()?.markerAnnotation()?.typeName()?.text?.let {
                ParserDelegate.nestedSentenceAnnotationNames.contains(it)
            } ?: false
        }

    private fun Java20Parser.MethodDeclarationContext.isAnnotatedAsEmphasised() =
        methodModifier().any { mm ->
            mm.annotation()?.normalAnnotation()?.typeName()?.text?.let {
                ParserDelegate.emphasisedMethodAnnotationNames.contains(it)
            } ?: false
        }

    private fun Java20Parser.InterfaceMethodDeclarationContext.isAnnotatedAsNested() =
        interfaceMethodModifier().any { mm ->
            mm.annotation()?.markerAnnotation()?.typeName()?.text?.let {
                ParserDelegate.nestedSentenceAnnotationNames.contains(it)
            } ?: false
        }

    private fun Java20Parser.InterfaceMethodDeclarationContext.isAnnotatedAsEmphasised() =
        interfaceMethodModifier().any { mm ->
            mm.annotation()?.normalAnnotation()?.typeName()?.text?.let {
                ParserDelegate.emphasisedMethodAnnotationNames.contains(it)
            } ?: false
        }

    override fun parse(stateMachine: ParserStateMachine, dc: MethodDeclarationContext) {
        ParseTreeWalker().walk(JavaMethodBodyParser(stateMachine), dc.body)
    }
}