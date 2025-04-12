package dev.kensa.example

import dev.kensa.StateExtractor
import dev.kensa.DummyAssert.Companion.assertThat

class KotlinWithVariousLiterals {
    fun literalTest() {
        assertThat(theNullResult()).isEqualTo(null)
        assertThat(theHexResult()).isEqualTo(0x123)
        assertThat(theBooleanResult()).isEqualTo(true)
        assertThat(theCharacterResult()).isEqualTo('a')
    }

    private fun theNullResult() = StateExtractor { null }
    private fun theHexResult() = StateExtractor { 0x123 }
    private fun theBooleanResult() = StateExtractor { true }
    private fun theCharacterResult() = StateExtractor { 'a' }
}