package dev.kensa.parse

import java.lang.reflect.Method

interface ParserDelegate {

    fun findMethodDeclarationsIn(target: Class<out Any>): MethodDeclarations

    fun parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext)

    fun prepareParametersFor(method: Method, parameterNamesAndTypes: List<Pair<String, String>>): MethodParameters

    companion object {
        val nestedSentenceAnnotationNames = listOf("NestedSentence", "dev.kensa.NestedSentence")
        val emphasisedMethodAnnotationNames = listOf("Emphasise", "dev.kensa.Emphasise")
    }
}
