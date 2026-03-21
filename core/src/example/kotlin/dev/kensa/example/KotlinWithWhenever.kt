package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext

class KotlinWithWhenever {

    fun simpleTest() {
        whenever(someActionIsPerformed())
    }

    private fun whenever(action: Action<ActionContext>) = Unit
    private fun someActionIsPerformed() = Action<ActionContext> { }
}
