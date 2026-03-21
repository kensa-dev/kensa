package dev.kensa.example

import dev.kensa.StateCollector
import dev.kensa.DummyAssert.Companion.assertThat

class KotlinWithVariousLiterals {
    fun literalTest() {
        assertThat(theNullResult()).isEqualTo(null)
        assertThat(theHexResult()).isEqualTo(0x123)
        assertThat(theBooleanResult()).isEqualTo(true)
        assertThat(theCharacterResult()).isEqualTo('a')
    }

    private fun theNullResult() = StateCollector { null }
    private fun theHexResult() = StateCollector { 0x123 }
    private fun theBooleanResult() = StateCollector { true }
    private fun theCharacterResult() = StateCollector { 'a' }
}
