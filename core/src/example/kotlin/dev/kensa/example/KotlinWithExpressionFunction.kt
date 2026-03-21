package dev.kensa.example

import dev.kensa.Action
import dev.kensa.GivensContext

class KotlinWithExpressionFunction {

    fun simpleTest() {
        given(someActionNameIsAddedToGivens())
    }

    private fun someActionNameIsAddedToGivens() = Action<GivensContext> { }

    private fun given(action: Action<GivensContext>) = Unit
}
