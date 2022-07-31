package dev.kensa.acceptance.example

import dev.kensa.ActionUnderTest
import dev.kensa.kotlin.KotlinKensaTest
import org.junit.jupiter.api.Test

class KotlinTestWithSimpleAction : KotlinKensaTest {

    @Test
    fun simpleTest() {
        whenever(someActionIsPerformed())
    }

    private fun someActionIsPerformed() = ActionUnderTest  { _, _ -> }
}