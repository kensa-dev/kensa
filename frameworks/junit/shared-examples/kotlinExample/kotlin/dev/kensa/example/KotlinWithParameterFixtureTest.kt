package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.StateCollector
import dev.kensa.example.ParameterFixtures.greeting
import dev.kensa.fixture.FixtureContainer
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.parameterFixture
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotlinWithParameterFixtureTest : KotlinExampleTest(), WithHamkrest {

    init {
        registerFixtures(ParameterFixtures)
    }

    @ParameterizedTest
    @ValueSource(strings = ["alice", "bob"])
    fun greetsTheUser(userName: String) {
        then(theGreeting(), equalTo(fixtures[greeting]))
    }

    private fun theGreeting() = StateCollector { fixtures[greeting] }
}

object ParameterFixtures : FixtureContainer {
    val greeting = parameterFixture("greeting", from = "userName") { name: String -> "Hello, $name" }
}
