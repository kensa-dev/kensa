package dev.kensa

import org.junit.jupiter.api.Test

class KotlinWithTypeArgumentsTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        given(aLiteralOf<String>("aStringValue"))
    }

    private fun <T> aLiteralOf(aValue: T?): GivensBuilder = GivensBuilder { }
}