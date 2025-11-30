package dev.kensa.parse.java

import dev.kensa.KensaException
import dev.kensa.parse.*
import dev.kensa.parse.Java20Parser.ClassDeclarationContext
import dev.kensa.parse.Java20Parser.InterfaceDeclarationContext
import dev.kensa.util.SourceCode
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.lang.reflect.Method

class JavaParserDelegate(
    private val isClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean,
    private val isInterfaceTest: (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean,
    private val antlrErrorListenerDisabled: Boolean,
    private val antlrPredicationMode: PredictionMode
) : ParserDelegate {

    override fun findMethodDeclarationsIn(target: Class<out Any>): MethodDeclarations {
        val testMethods = mutableListOf<MethodDeclarationContext>()
        val nestedMethods = mutableListOf<MethodDeclarationContext>()
        val emphasisedMethods = mutableListOf<MethodDeclarationContext>()

        val tokens = tokenStreamFor(target)

        // TODO : Need to test with nested classes as this probably won't work...
        compilationUnitFor(tokens).ordinaryCompilationUnit().topLevelClassOrInterfaceDeclaration()
            .firstNotNullOfOrNull {
                it.classDeclaration() ?: it.interfaceDeclaration()
            }?.apply {
                when (this) {
                    is ClassDeclarationContext ->
                        normalClassDeclaration().classBody().classBodyDeclaration().forEach { cbd ->
                            cbd.classMemberDeclaration()?.methodDeclaration()?.let { md ->
                                testMethods.takeIf { isClassTest(md) }?.add(JavaMethodDeclarationContext(md, tokens))
                                nestedMethods.takeIf { md.isAnnotatedAsNested() }?.add(JavaMethodDeclarationContext(md, tokens))
                                emphasisedMethods.takeIf { md.isAnnotatedAsEmphasised() }?.add(JavaMethodDeclarationContext(md, tokens))
                            }
                        }

                    is InterfaceDeclarationContext ->
                        normalInterfaceDeclaration().interfaceBody().interfaceMemberDeclaration().forEach { imd ->
                            imd.interfaceMethodDeclaration()?.let { md ->
                                testMethods.takeIf { isInterfaceTest(md) }?.add(JavaInterfaceDeclarationContext(md, tokens))
                                nestedMethods.takeIf { md.isAnnotatedAsNested() }?.add(JavaInterfaceDeclarationContext(md, tokens))
                                emphasisedMethods.takeIf { md.isAnnotatedAsEmphasised() }?.add(JavaInterfaceDeclarationContext(md, tokens))
                            }
                        }

                    else -> throw KensaException("Cannot handle Parser Rule Contexts of type [${this::class.java}]")
                }
            } ?: throw KensaException("Unable to find class declaration in source code")

        return MethodDeclarations(testMethods, nestedMethods, emphasisedMethods)
    }

    override fun prepareParametersFor(method: Method, parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters =
        MethodParameters(
            method.parameters.mapIndexed { index, parameter ->
                ElementDescriptor.forParameter(parameter, parameterNamesAndTypes[index].first, index)
            }.associateByTo(LinkedHashMap(), ElementDescriptor::name)
        )

    private fun tokenStreamFor(target: Class<out Any>): CommonTokenStream =
        SourceCode
            .sourceStreamFor(target)
            .let { Java20Lexer(it) }
            .let { CommonTokenStream(it) }

    private fun compilationUnitFor(tokens: CommonTokenStream): Java20Parser.CompilationUnitContext =
        Java20Parser(tokens).apply {
            takeIf { antlrErrorListenerDisabled }?.removeErrorListeners()
            interpreter.predictionMode = antlrPredicationMode
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