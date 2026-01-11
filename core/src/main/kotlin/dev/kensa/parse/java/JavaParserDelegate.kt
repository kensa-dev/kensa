package dev.kensa.parse.java

import dev.kensa.KensaException
import dev.kensa.parse.*
import dev.kensa.parse.java.Java20Parser.ClassDeclarationContext
import dev.kensa.parse.java.Java20Parser.InterfaceDeclarationContext
import dev.kensa.util.SourceCode
import dev.kensa.util.isKotlinClass
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.lang.reflect.Method

class JavaParserDelegate(
    private val isClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean,
    private val isInterfaceTest: (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean,
    private val antlrErrorListenerDisabled: Boolean,
    private val antlrPredicationMode: PredictionMode,
    private val sourceCode: SourceCode
) : ParserDelegate {

    override fun Class<*>.isParsable(): Boolean = !isKotlinClass

    override fun Class<*>.toSimpleName(): (Class<*>) -> String = Class<*>::getSimpleName

    override fun Class<*>.findMethodDeclarations(): MethodDeclarations {
        val testMethods = mutableListOf<MethodDeclarationContext>()
        val nestedMethods = mutableListOf<MethodDeclarationContext>()
        val emphasisedMethods = mutableListOf<MethodDeclarationContext>()

        // TODO : Need to test with nested classes as this probably won't work...
        val sourceStream = sourceCode.sourceStreamFor(this)
        val declaration = sourceStream.compilationUnit().ordinaryCompilationUnit()
            .topLevelClassOrInterfaceDeclaration()
            .mapNotNull { it.classDeclaration() ?: it.interfaceDeclaration() }
            .firstOrNull { decl ->
                when (decl) {
                    is ClassDeclarationContext -> decl.normalClassDeclaration()?.typeIdentifier()?.text == simpleName
                    is InterfaceDeclarationContext -> decl.normalInterfaceDeclaration()?.typeIdentifier()?.text == simpleName
                    else -> false
                }
            } ?: throw KensaException("Unable to find declaration for [$simpleName] in source file [${sourceStream.sourceName}]")

        declaration.apply {
            when (this) {
                is ClassDeclarationContext ->
                    normalClassDeclaration().classBody().classBodyDeclaration().forEach { cbd ->
                        cbd.classMemberDeclaration()?.methodDeclaration()?.let { md ->
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
        }

        return MethodDeclarations(testMethods, nestedMethods, emphasisedMethods)
    }

    override fun Method.prepareParameters(parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters =
        MethodParameters(
            parameters.mapIndexed { index, parameter ->
                ElementDescriptor.forParameter(parameter, parameterNamesAndTypes[index].first, index)
            }.associateByTo(LinkedHashMap(), ElementDescriptor::name)
        )

    private fun CharStream.compilationUnit(): Java20Parser.CompilationUnitContext =
        // Reset the CharStream to the beginning
        Java20Parser(CommonTokenStream(KensaJavaLexer(apply { seek(0) }))).apply {
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

    override fun Class<*>.parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext) {
        ParseTreeWalker().walk(JavaMethodBodyParser(stateMachine, parseContext), dc.body)
    }
}