package dev.kensa.parse.kotlin

import dev.kensa.parse.*
import dev.kensa.parse.ParserDelegate.Companion.emphasisedMethodAnnotationNames
import dev.kensa.parse.ParserDelegate.Companion.expandableSentenceAnnotationNames
import dev.kensa.util.SourceCode
import dev.kensa.util.isKotlinClass
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.tree.ParseTreeWalker
import java.lang.reflect.Method
import dev.kensa.util.findSyntheticKotlinReceivers

class KotlinParserDelegate(
    private val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean,
    private val antlrErrorListenerDisabled: Boolean,
    private val antlrPredicationMode: PredictionMode,
    private val sourceCode: SourceCode
) : ParserDelegate {

    override fun Class<*>.isParsable(): Boolean = isKotlinClass

    override fun Class<*>.parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext) {
        ParseTreeWalker().walk(KotlinFunctionBodyParser(stateMachine, parseContext), dc.body)
    }

    override fun Class<*>.toSimpleName(): (Class<*>) -> String = { it.kotlin.simpleName ?: throw IllegalArgumentException("Types must have names") }

    override fun Class<out Any>.findMethodDeclarations(): MethodDeclarations {
        val testFunctions = mutableListOf<MethodDeclarationContext>()
        val expandableSentenceFunctions = mutableListOf<MethodDeclarationContext>()
        val emphasisedFunctions = mutableListOf<MethodDeclarationContext>()

        // TODO : Need to test with nested classes as this probably won't work...
        val sourceStream = sourceCode.sourceStreamFor(this)
        val compilationUnit = sourceStream.compilationUnit()

        val importStrings = compilationUnit.importList().children?.map { it.text.substringAfter("import").trim() } ?: emptyList()
        val imports = Imports(importStrings, this)

        ArrayList<KotlinParser.FunctionDeclarationContext>().apply {
            compilationUnit.topLevelObject().forEach {
                findFunctionDeclarations(it, this)
            }

            forEach {
                assignDeclarations(testFunctions, expandableSentenceFunctions, emphasisedFunctions)(it)
            }
        }

        return MethodDeclarations(mapOf(this to ClassDeclarations(imports, testFunctions, expandableSentenceFunctions, emphasisedFunctions)))
    }

    override fun Method.prepareParameters(parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters {
        val combined = findSyntheticKotlinReceivers() + parameterNamesAndTypes

        return MethodParameters(
            parameters.mapIndexed { index, parameter ->
                ElementDescriptor.forParameter(parameter, combined[index].first, index)
            }.associateByTo(LinkedHashMap(), ElementDescriptor::name)
        )
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
        expandableSentenceFunctions: MutableList<MethodDeclarationContext>,
        emphasisedFunctions: MutableList<MethodDeclarationContext>
    ): (KotlinParser.FunctionDeclarationContext) -> Unit = { fd ->
        testFunctions.takeIf { isTest(fd) }?.add(KotlinMethodDeclarationContext(fd))
        expandableSentenceFunctions.takeIf { fd.isAnnotatedAsExpandableSentence() }?.add(KotlinMethodDeclarationContext(fd))
        emphasisedFunctions.takeIf { fd.isAnnotatedAsEmphasised() }?.add(KotlinMethodDeclarationContext(fd))
    }

    private fun CharStream.compilationUnit(): KotlinParser.KotlinFileContext =
        // Reset the CharStream to the beginning
        KotlinParser(CommonTokenStream(KensaKotlinLexer(apply { seek(0) }))).apply {
            takeIf { antlrErrorListenerDisabled }?.removeErrorListeners()
            interpreter.predictionMode = antlrPredicationMode
        }.kotlinFile()

    private fun KotlinParser.FunctionDeclarationContext.isAnnotatedAsExpandableSentence(): Boolean = findAnnotationNames().any { name -> expandableSentenceAnnotationNames.contains(name) }

    private fun KotlinParser.FunctionDeclarationContext.isAnnotatedAsEmphasised(): Boolean = findAnnotationNames().any { name -> emphasisedMethodAnnotationNames.contains(name) }

    companion object {
        fun KotlinParser.FunctionDeclarationContext.findAnnotationNames(): List<String> {
            val functionAnnotations = modifiers().flatMap { it.annotation() }
            val statementAnnotations = parent?.parent?.takeIf { it is KotlinParser.StatementContext }?.let { (it as KotlinParser.StatementContext).annotation() }.orEmpty()

            val annotationContexts = functionAnnotations + statementAnnotations

            return annotationContexts.mapNotNull {
                val namedWithConstructorInvocation = it.singleAnnotation()?.unescapedAnnotation()?.constructorInvocation()?.userType()?.text
                val namedWithoutConstructorInvocation = it.singleAnnotation()?.unescapedAnnotation()?.userType()?.text

                namedWithConstructorInvocation ?: namedWithoutConstructorInvocation
            }
        }
    }
}