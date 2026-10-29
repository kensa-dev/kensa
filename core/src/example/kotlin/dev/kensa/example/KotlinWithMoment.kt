package dev.kensa.example

import dev.kensa.Action
import dev.kensa.Moment

class KotlinWithMoment {

    private val theTriggerWasSent = Moment()

    private val theColour = "red"

    fun markedAction() {
        whenever(someAction().marking(theTriggerWasSent))
    }

    fun markingWithoutAMoment() {
        whenever(aBrush().marking(theColour))
    }

    private fun someAction() = Action<Any> { }

    private fun aBrush() = Brush()

    private fun whenever(action: Action<Any>) {}

    class Brush {
        fun marking(colour: String) = Action<Any> { }
    }
}
