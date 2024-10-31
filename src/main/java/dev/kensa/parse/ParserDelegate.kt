package dev.kensa.parse

interface ParserDelegate {

    fun findMethodDeclarationsIn(target: Class<out Any>): MethodDeclarations

    fun parse(stateMachine: ParserStateMachine, dc: MethodDeclarationContext)

    companion object {
        val testAnnotationNames = listOf("Test", "org.junit.jupiter.api.Test", "ParameterizedTest", "org.junit.jupiter.params.ParameterizedTest")
        val nestedSentenceAnnotationNames = listOf("NestedSentence", "dev.kensa.NestedSentence")
        val emphasisedMethodAnnotationNames = listOf("Emphasise", "dev.kensa.Emphasise")
    }
}
