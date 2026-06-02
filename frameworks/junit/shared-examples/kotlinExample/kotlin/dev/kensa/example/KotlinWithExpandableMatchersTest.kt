package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.fixture
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test

class KotlinWithExpandableMatchersTest : KotlinExampleTest(), WithHamkrest {

    init {
        registerFixtures(Companion)
    }

    @RenderedValue
    private val aFirstName = "John"

    @RenderedValue
    private val aLastName = "Smith"

    @Test
    fun matchesTheCorrectDetails() {
        whenever(theCorrectDetails())

        whenever(theFixtureBackedDetails())
    }

    @ExpandableSentence
    private fun theCorrectDetails(): Action<ActionContext> = matchers(aFirstName of "John", aLastName of "Smith")

    @ExpandableSentence
    private fun theFixtureBackedDetails(): Action<ActionContext> {
        return matchers(
            /*+ Ignore */
            anIgnoredMatcher(),
            aFirstName of fixtures(MatcherFixture),
        )
    }

    private fun matchers(vararg matchers: Matcher): Action<ActionContext> = Action { }
    private fun anIgnoredMatcher(): Matcher = Matcher()
    private infix fun String.of(value: String): Matcher = Matcher()

    class Matcher

    companion object : FixtureContainer {
        val MatcherFixture = fixture("KotlinMatcherFixture") { "aFixtureValue" }
    }
}
