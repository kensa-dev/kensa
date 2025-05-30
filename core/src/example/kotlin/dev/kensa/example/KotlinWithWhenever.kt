package dev.kensa.example

import dev.kensa.ActionUnderTest

class KotlinWithWhenever {

    fun simpleTest() {
        whenever(someActionIsPerformed())
    }

    private fun whenever(action: ActionUnderTest) = Unit
    private fun someActionIsPerformed() = ActionUnderTest { _, _, _ -> }
}