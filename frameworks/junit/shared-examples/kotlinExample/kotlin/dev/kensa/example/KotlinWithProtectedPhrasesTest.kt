package dev.kensa.example

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.Kensa.konfigure
import dev.kensa.StateCollector
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.sentence.ProtectedPhrase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinWithProtectedPhrasesTest : KotlinExampleTest(), WithHamkrest {
    @BeforeEach
    fun setUp() {
        konfigure {
            protectedPhrases(
                ProtectedPhrase("cat"),
                ProtectedPhrase("dog"),
                ProtectedPhrase("city"),
                ProtectedPhrase("holiday"),
            )
        }
    }

    @Test
    fun passingTest() {
        given(aCatCanDriveABus())

        whenever(theOtherCatsAndDogsGetOnTheBus())

        then(theyTravelToDifferentCities(), forHolidays())
    }

    fun aCatCanDriveABus() = Action<GivensContext> {}

    fun theOtherCatsAndDogsGetOnTheBus() = Action<ActionContext> {}

    fun theyTravelToDifferentCities() = StateCollector { "" }

    fun forHolidays() = object : Matcher<String> {
        override fun invoke(actual: String): MatchResult {
            return MatchResult.Match
        }

        override val description: String
            get() = "Meh"
    }
}