package dev.kensa.parse.java

import dev.kensa.Configuration
import dev.kensa.KensaException
import dev.kensa.parse.*
import dev.kensa.parse.Java20Parser.ClassDeclarationContext
import dev.kensa.parse.Java20Parser.InterfaceDeclarationContext
import dev.kensa.util.SourceCode
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

class JavaParserDelegate(
    private val isClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean,
    private val isInterfaceTest: (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean,
    private val configuration: Configuration
) : ParserDelegate {

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
                                testMethods.takeIf { isClassTest(md) }?.add(JavaMethodDeclarationContext(md))
                                nestedMethods.takeIf { md.isAnnotatedAsNested() }?.add(JavaMethodDeclarationContext(md))
                                emphasisedMethods.takeIf { md.isAnnotatedAsEmphasised() }?.add(JavaMethodDeclarationContext(md))
                            }
                        }

                    is InterfaceDeclarationContext ->
                        normalInterfaceDeclaration().interfaceBody().interfaceMemberDeclaration().forEach { imd ->
                            imd.interfaceMethodDeclaration()?.let { md ->
                                testMethods.takeIf { isInterfaceTest(md) }?.add(JavaInterfaceDeclarationContext(md))
                                nestedMethods.takeIf { md.isAnnotatedAsNested() }?.add(JavaInterfaceDeclarationContext(md))
                                emphasisedMethods.takeIf { md.isAnnotatedAsEmphasised() }?.add(JavaInterfaceDeclarationContext(md))
                            }
                        }

                    else -> throw KensaException("Cannot handle Parser Rule Contexts of type [${this::class.java}]")
                }
            } ?: throw KensaException("Unable to find class declaration in source code")

        return MethodDeclarations(testMethods, nestedMethods, emphasisedMethods)
    }

    private fun compilationUnitFor(target: Class<out Any>): Java20Parser.CompilationUnitContext =
        Java20Parser(
            CommonTokenStream(
                Java20Lexer(SourceCode.sourceStreamFor(target))
            )
        ).apply {
            takeIf { configuration.antlrErrorListenerDisabled }?.removeErrorListeners()
            interpreter.predictionMode = configuration.antlrPredicationMode
        }.compilationUnit()

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

    override fun parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext) {
        ParseTreeWalker().walk(JavaMethodBodyParser(stateMachine, parseContext), dc.body)
    }
}