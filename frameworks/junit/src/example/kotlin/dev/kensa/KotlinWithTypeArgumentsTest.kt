package dev.kensa

import dev.kensa.junit.KensaTest
import dev.kensa.state.Givens
import org.junit.jupiter.api.Test

class KotlinWithTypeArgumentsTest : KensaTest {

    @Test
    fun passingTest() {
        given(aLiteralOf<String>("aStringValue"))
    }

    private fun <T> aLiteralOf(aValue: T?): GivensBuilder {
        return GivensBuilder { givens: Givens? -> }
    }
}