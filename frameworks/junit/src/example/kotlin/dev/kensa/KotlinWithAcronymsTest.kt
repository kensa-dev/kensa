package dev.kensa

import dev.kensa.Kensa.konfigure
import dev.kensa.junit.KensaTest
import dev.kensa.sentence.Acronym.Companion.of
import dev.kensa.state.Givens
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinWithAcronymsTest : KensaTest {
    @BeforeEach
    fun setUp() {
        konfigure {
            acronyms(
                of("FTTP", "Fibre To The Premises"),
                of("FUBAR", "F***** up beyond all recognition")
            )
        }
    }

    @Test
    fun passingTest() {
        given(aMethodNameWithFttpAcronym())

        and(aMethodNameWithFubarAcronym())
    }

    fun aMethodNameWithFttpAcronym(): GivensBuilder = GivensBuilder { givens: Givens -> }

    fun aMethodNameWithFubarAcronym(): GivensBuilder = GivensBuilder { givens: Givens -> }
}