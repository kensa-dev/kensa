package dev.kensa.example

import dev.kensa.Action
import dev.kensa.GivensContext
import org.junit.jupiter.api.Test

class KotlinWithTypeArgumentsTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        given(aLiteralOf<String>("aStringValue"))
    }

    private fun <T> aLiteralOf(aValue: T?) = Action<GivensContext> {}
}