package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.StateCollector
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.fixture
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test

class KotlinWithCompanionFixturesTest : KotlinExampleTest(), WithHamkrest {

    init {
        registerFixtures(Companion)
    }

    @Test
    fun rendersFixturesDeclaredInTheCompanion() {
        then(theGreeting(), equalTo(fixtures[Greeting]))
    }

    private fun theGreeting() = StateCollector { _ -> "Hello ${fixtures[FirstName]}" }

    companion object : FixtureContainer {
        val FirstName = fixture("CompanionFirstName") { "Jane" }
        val Greeting = fixture("CompanionGreeting", FirstName) { name -> "Hello $name" }
    }
}
