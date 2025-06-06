package dev.kensa

import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test

class KotlinWithLiteralsTest : KensaTest {

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

    private fun <T> aLiteralOf(aValue: T?): GivensBuilderWithFixtures {
        return GivensBuilderWithFixtures { _, _ -> }
    }
}