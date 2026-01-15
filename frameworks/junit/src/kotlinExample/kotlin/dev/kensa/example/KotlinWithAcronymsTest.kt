package dev.kensa.example

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

    fun aMethodNameWithFttpAcronym(): dev.kensa.GivensBuilder = _root_ide_package_.dev.kensa.GivensBuilder { }

    fun aMethodNameWithFubarAcronym(): dev.kensa.GivensBuilder = _root_ide_package_.dev.kensa.GivensBuilder {}
}