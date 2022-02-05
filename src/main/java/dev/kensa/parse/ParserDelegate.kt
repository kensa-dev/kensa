package dev.kensa.parse

interface ParserDelegate {

    fun findMethodDeclarationsIn(testClass: Class<out Any>): Triple<List<MethodDeclarationContext>, List<MethodDeclarationContext>, List<MethodDeclarationContext>>

    fun parse(stateMachine: ParserStateMachine, dc: MethodDeclarationContext)

    companion object {
        val testAnnotationNames = listOf("Test", "org.junit.jupiter.api.Test", "ParameterizedTest", "org.junit.jupiter.params.ParameterizedTest")
        val nestedSentenceAnnotationNames = listOf("NestedSentence", "dev.kensa.NestedSentence")
        val emphasisedMethodAnnotationNames = listOf("Emphasise", "dev.kensa.Emphasise")
    }
}
