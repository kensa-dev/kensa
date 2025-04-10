package dev.kensa.example

import dev.kensa.StateExtractor
import dev.kensa.kotlin.KotlinKensaTest
import dev.kensa.kotlin.WithKotest
import io.kotest.matchers.be
import org.junit.jupiter.api.Test

class KotlinTestWithVariousLiterals : KotlinKensaTest, WithKotest {
    @Test
    fun literalTest() {
        then(theNullResult(), be(null))
        then(theHexResult(), be(0x123))
        then(theBooleanResult(), be<Boolean>(true))
        then(theCharacterResult(), be<Char>('a'))
    }

    private fun theNullResult() = StateExtractor { null }
    private fun theHexResult() = StateExtractor { 0x123 }
    private fun theBooleanResult() = StateExtractor { true }
    private fun theCharacterResult() = StateExtractor { 'a' }
}