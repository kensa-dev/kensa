package dev.kensa.example

import dev.kensa.GivensBuilder

class KotlinWithExpressionFunction {

    fun simpleTest() {
        given(someActionNameIsAddedToGivens())
    }

    private fun someActionNameIsAddedToGivens() = GivensBuilder { _, _ -> }

    private fun given(givensBuilder: GivensBuilder) = Unit
}