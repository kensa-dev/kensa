package dev.kensa

import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test

class KotlinWithTypeArgumentsTest : KensaTest {

    @Test
    fun passingTest() {
        given(aLiteralOf<String>("aStringValue"))
    }

    private fun <T> aLiteralOf(aValue: T?): GivensBuilderWithFixtures {
        return GivensBuilderWithFixtures { _, _ -> }
    }
}