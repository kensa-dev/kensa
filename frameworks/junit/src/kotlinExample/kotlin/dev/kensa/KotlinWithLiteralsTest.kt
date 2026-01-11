package dev.kensa

import org.junit.jupiter.api.Test

class KotlinWithLiteralsTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        given(aLiteralOf("aStringValue"))
        given(aLiteralOf(true))
        given(aLiteralOf(10))
        given(aLiteralOf(null))
        given(aLiteralOf('a'))
        given(aLiteralOf(
            """
                a multiline string
                """
        )
        )
    }

    private fun <T> aLiteralOf(aValue: T?): GivensBuilder = GivensBuilder { }
}