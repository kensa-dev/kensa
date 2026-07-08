package dev.kensa.parse

import dev.kensa.parse.java.Java20Parser
import dev.kensa.parse.kotlin.KotlinParser
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTree

private fun Java20Parser.MethodHeaderContext.parameterNamesAndTypes() =
    ArrayList<Pair<String, String>>().apply {
        methodDeclarator().formalParameterList()?.let { fpl ->
            fpl.formalParameter()?.forEach { fp ->
                add(Pair(fp.variableDeclaratorId().text, fp.unannType().text))
            }
        }
    }

// Sever the captured body from the rest of the parse tree so retaining a
// MethodDeclarationContext does not pin the whole source file's AST and token
// stream. See docs/superpowers/plans/2026-05-28-parser-cache-ast-detachment.md.
private fun <T : ParserRuleContext> T.detachedFromParent(): T = apply { parent = null }

private fun KotlinParser.FunctionValueParameterContext.isVararg(): Boolean =
    parameterModifiers()?.parameterModifier()?.any { it.VARARG() != null } == true

interface MethodDeclarationContext {
    val name: String
    val body: ParseTree
    val parameterNamesAndTypes: List<Pair<String, String>>
    val parameterTypes: List<String> get() = parameterNamesAndTypes.map { it.second }
    val startLine: Int
    val endLine: Int
}

class JavaMethodDeclarationContext private constructor(
    override val name: String,
    override val body: ParseTree,
    override val parameterNamesAndTypes: List<Pair<String, String>>,
    override val startLine: Int,
    override val endLine: Int,
) : MethodDeclarationContext {
    companion object {
        operator fun invoke(delegate: Java20Parser.MethodDeclarationContext) = JavaMethodDeclarationContext(
            name = delegate.methodHeader().methodDeclarator().identifier().text,
            body = delegate.methodBody().detachedFromParent(),
            parameterNamesAndTypes = delegate.methodHeader().parameterNamesAndTypes(),
            startLine = delegate.start.line,
            endLine = delegate.stop?.line ?: delegate.start.line,
        )
    }
}

class JavaInterfaceDeclarationContext private constructor(
    override val name: String,
    override val body: ParseTree,
    override val parameterNamesAndTypes: List<Pair<String, String>>,
    override val startLine: Int,
    override val endLine: Int,
) : MethodDeclarationContext {
    companion object {
        operator fun invoke(delegate: Java20Parser.InterfaceMethodDeclarationContext) = JavaInterfaceDeclarationContext(
            name = delegate.methodHeader().methodDeclarator().identifier().text,
            body = delegate.methodBody().detachedFromParent(),
            parameterNamesAndTypes = delegate.methodHeader().parameterNamesAndTypes(),
            startLine = delegate.start.line,
            endLine = delegate.stop?.line ?: delegate.start.line,
        )
    }
}

class KotlinMethodDeclarationContext private constructor(
    override val name: String,
    override val body: ParseTree,
    override val parameterNamesAndTypes: List<Pair<String, String>>,
    override val startLine: Int,
    override val endLine: Int,
) : MethodDeclarationContext {
    companion object {
        operator fun invoke(delegate: KotlinParser.FunctionDeclarationContext) = KotlinMethodDeclarationContext(
            name = delegate.simpleIdentifier().text.replace("`", ""),
            body = delegate.functionBody().detachedFromParent(),
            parameterNamesAndTypes = delegate.functionValueParameters().functionValueParameter()
                .map { fvp ->
                    val parameter = fvp.parameter()
                    val type = parameter.type().text.trim().trimEnd('?')
                    // vararg params are arrays at runtime, so present them as such for declaration matching
                    parameter.simpleIdentifier().text to if (fvp.isVararg()) "Array<$type>" else type
                },
            startLine = delegate.start.line,
            endLine = delegate.stop?.line ?: delegate.start.line,
        )
    }
}
