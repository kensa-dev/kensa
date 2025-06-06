package dev.kensa.example

import dev.kensa.GivensBuilderWithFixtures

class KotlinWithExpressionFunction {

    fun simpleTest() {
        given(someActionNameIsAddedToGivens())
    }

    private fun someActionNameIsAddedToGivens() = GivensBuilderWithFixtures { _, _ -> }

    private fun given(givensBuilder: GivensBuilderWithFixtures) = Unit
}