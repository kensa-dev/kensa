package dev.kensa.parse

import dev.kensa.KensaException
import java.lang.reflect.Method

interface ParserDelegate {

    fun Class<*>.isParsable(): Boolean

    fun Class<*>.toSimpleName(): (Class<*>) -> String

    fun Class<*>.findMethodDeclarations(): MethodDeclarations

    fun Class<*>.parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext)

    fun Method.prepareParameters(parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters

    companion object {
        val nestedSentenceAnnotationNames = listOf("NestedSentence", "dev.kensa.NestedSentence")
        val emphasisedMethodAnnotationNames = listOf("Emphasise", "dev.kensa.Emphasise")
    }
}

class CompositeParserDelegate(private val delegates: List<ParserDelegate>) : ParserDelegate {

    override fun Class<*>.isParsable(): Boolean = delegates.any { delegate -> with(delegate) { isParsable() } }

    override fun Class<*>.toSimpleName(): (Class<*>) -> String = with(findDelegate()) { toSimpleName() }

    override fun Class<*>.findMethodDeclarations(): MethodDeclarations = with(findDelegate()) { findMethodDeclarations() }

    override fun Class<*>.parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext) {
        with(findDelegate()) { parse(stateMachine, parseContext, dc) }
    }

    override fun Method.prepareParameters(parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters =
        with(declaringClass.findDelegate()) { prepareParameters(parameterNamesAndTypes) }

    private fun Class<*>.findDelegate(): ParserDelegate =
        delegates.find { delegate ->
            with(delegate) { isParsable() }
        } ?: throw KensaException("No delegate found to parse [${this.name}]")

}
