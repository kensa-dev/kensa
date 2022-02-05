package dev.kensa.parse

import org.antlr.v4.runtime.tree.ParseTree

interface ParserDelegate<DC : ParseTree> {

    fun methodNameFrom(dc: DC): String

    fun findMethodDeclarationsIn(testClass: Class<out Any>): Triple<List<DC>, List<DC>, List<DC>>

    fun parameterNamesAndTypesFrom(dc: DC): List<Pair<String, String>>

    fun parse(stateMachine: ParserStateMachine, dc: DC)

    companion object {
        val testAnnotationNames = listOf("Test", "org.junit.jupiter.api.Test", "ParameterizedTest", "org.junit.jupiter.params.ParameterizedTest")
        val nestedSentenceAnnotationNames = listOf("NestedSentence", "dev.kensa.NestedSentence")
        val emphasisedMethodAnnotationNames = listOf("Emphasise", "dev.kensa.Emphasise")
    }
}
