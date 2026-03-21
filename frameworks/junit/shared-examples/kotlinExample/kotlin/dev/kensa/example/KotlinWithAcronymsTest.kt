package dev.kensa.example

import dev.kensa.Action
import dev.kensa.GivensContext
import dev.kensa.Kensa.konfigure
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.sentence.Acronym.Companion.of
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinWithAcronymsTest : KotlinExampleTest(), WithHamkrest {

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

    fun aMethodNameWithFttpAcronym(): Action<GivensContext> = Action { }

    fun aMethodNameWithFubarAcronym(): Action<GivensContext> = Action { }
}
