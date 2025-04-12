package dev.kensa.parse

interface ParserDelegate {

    fun findMethodDeclarationsIn(target: Class<out Any>): MethodDeclarations

    fun parse(stateMachine: ParserStateMachine, parseContext: ParseContext, dc: MethodDeclarationContext)

    companion object {
        val nestedSentenceAnnotationNames = listOf("NestedSentence", "dev.kensa.NestedSentence")
        val emphasisedMethodAnnotationNames = listOf("Emphasise", "dev.kensa.Emphasise")
    }
}
