package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.StateCollector
import dev.kensa.WithFixturesSuite
import dev.kensa.fixture.FixtureRegistry.registerFixtures
import dev.kensa.fixture.KotlinTestFixtures
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test

// One shared interface: test files only import this
interface WithKotlinTestFixtures : WithFixturesSuite<KotlinTestFixtures> {
    override val fixturesObject get() = KotlinTestFixtures
}

class KotlinWithFixtureSuiteTest : KotlinExampleTest(), WithKotlinTestFixtures, WithHamkrest {

    init {
        registerFixtures(KotlinTestFixtures)
    }

    @Test
    fun `can access fixtures from a suite using block syntax`() {
        then(theStringFixture(), equalTo(fixtures { StringFixture }))
    }

    private fun theStringFixture() = StateCollector { fixtures { StringFixture } }
}
