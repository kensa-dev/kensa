package dev.kensa.example

import dev.kensa.GivensBuilder
import dev.kensa.kotlin.KotlinKensaTest
import org.junit.jupiter.api.Test

fun test(block: () -> Unit) {
    block()
}

class KotlinTestWithExpressionFunction : KotlinKensaTest {

    private val actionName = "ACTION1"

    @Test
    fun expressionTest() = test {
        given(someActionNameIsAddedToGivens())
    }

    private fun someActionNameIsAddedToGivens() =
        GivensBuilder { givens -> givens.put("actionName", actionName) }
}