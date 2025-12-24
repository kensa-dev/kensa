package dev.kensa.parse

import org.antlr.v4.runtime.tree.ParseTree

private fun Java20Parser.MethodHeaderContext.parameterNamesAndTypes() =
    ArrayList<Pair<String, String>>().apply {
        methodDeclarator().formalParameterList()?.let { fpl ->
            fpl.formalParameter()?.forEach { fp ->
                add(Pair(fp.variableDeclaratorId().text, fp.unannType().text))
            }
        }
    }

interface MethodDeclarationContext {
    val name: String
    val body: ParseTree
    val parameterNamesAndTypes: List<Pair<String, String>>
}

class JavaMethodDeclarationContext(private val delegate: Java20Parser.MethodDeclarationContext) : MethodDeclarationContext {
    override val name: String by lazy {
        delegate.methodHeader().methodDeclarator().identifier().text
    }

    override val body: ParseTree by lazy {
        delegate.methodBody()
    }

    override val parameterNamesAndTypes: List<Pair<String, String>> by lazy {
        delegate.methodHeader().parameterNamesAndTypes()
    }
}

class JavaInterfaceDeclarationContext(private val delegate: Java20Parser.InterfaceMethodDeclarationContext) : MethodDeclarationContext {
    override val name: String by lazy {
        delegate.methodHeader().methodDeclarator().identifier().text
    }

    override val body: ParseTree by lazy { delegate.methodBody() }

    override val parameterNamesAndTypes: List<Pair<String, String>> by lazy {
        delegate.methodHeader().parameterNamesAndTypes()
    }
}

class KotlinMethodDeclarationContext(private val delegate: KotlinParser.FunctionDeclarationContext) : MethodDeclarationContext {
    override val name: String by lazy {
        delegate.simpleIdentifier().text.replace("`", "")
    }

    override val body: ParseTree by lazy {
        delegate.functionBody()
    }

    override val parameterNamesAndTypes: List<Pair<String, String>> by lazy {
        delegate.functionValueParameters().functionValueParameter()
            .map { it.parameter() }
            .map { it.simpleIdentifier().text to it.type().text.trim().trimEnd('?') }
            .toList()
    }
}