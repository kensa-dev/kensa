// Snippet source for kensa.dev/docs/api/fixtures.md — Kotlin examples
package apidocs

import com.natpryce.hamkrest.equalTo
import dev.kensa.StateCollector
import dev.kensa.WithFixturesSuite
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.fixture
import dev.kensa.fixture.parameterFixture
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

// --- Co-locating fixtures with a test ---

class OrderPricingTest : KensaTest, WithHamkrest {

    init {
        registerFixtures(Companion)
    }

    @Test
    fun rendersFixturesDeclaredInTheCompanion() {
        then(theGreeting(), equalTo(fixtures[Greeting]))
    }

    private fun theGreeting() = StateCollector { "Hello ${fixtures[FirstName]}" }

    companion object : FixtureContainer {
        val FirstName = fixture("FirstName") { "Jane" }
        val Greeting = fixture("Greeting", FirstName) { name -> "Hello $name" }
    }
}

// --- Parameter-derived fixtures ---

object GreetingFixtures : FixtureContainer {
    val Greeting = parameterFixture("Greeting", from = "userName") { name: String ->
        "Hello, ${name.replaceFirstChar(Char::uppercase)}"
    }
}

class GreetingTest : KensaTest, WithHamkrest {

    init {
        registerFixtures(GreetingFixtures)
    }

    @ParameterizedTest
    @ValueSource(strings = ["alice", "bob"])
    fun `greets the user`(userName: String) {
        then(theBanner(), equalTo(fixtures[GreetingFixtures.Greeting]))
    }

    private fun theBanner() = StateCollector { "Hello, Alice" }
}

// --- WithFixturesSuite ---

object TelecomsFixtures : FixtureContainer {
    val AccountNumber = fixture("Account Number") { "ACC-123" }
}

interface WithTelecomsFixtures : WithFixturesSuite<TelecomsFixtures> {
    override val fixturesObject get() = TelecomsFixtures
}

class FeasibilityServiceTest : KensaTest, WithTelecomsFixtures, WithHamkrest {
    @Test
    fun `can check feasibility`() {
        then(theAccountNumber(), equalTo(fixtures { AccountNumber }))
    }

    private fun theAccountNumber() = StateCollector { fixtures { AccountNumber } }
}
